from __future__ import annotations

import asyncio

from datetime import datetime, timezone
from typing import Any

from fastapi import (
    APIRouter,
    WebSocket,
    WebSocketDisconnect,
)
from pydantic import ValidationError
from sqlalchemy.orm import Session
from starlette.websockets import WebSocketState

from app.database import SessionLocal
from app.models import RegisteredDevice, utc_now
from app.schemas import (
    WebSocketAuthenticateMessage,
    WebSocketHeartbeatMessage,
)
from app.security import (
    ExpiredDeviceTokenError,
    InvalidDeviceTokenError,
    decode_device_access_token,
)
from app.services.device_service import (
    get_device_by_public_id,
)
from app.websocket_manager import websocket_manager


router = APIRouter(
    tags=["WebSocket"],
)


# Mobil istemcinin bağlantı açıldıktan sonra
# kimlik doğrulama mesajını göndermesi için
# beklenecek maksimum süre.
AUTHENTICATION_TIMEOUT_SECONDS = 15


def current_utc_iso() -> str:
    """
    UTC zamanını ISO 8601 metni olarak döndürür.
    """

    return datetime.now(
        timezone.utc,
    ).isoformat()


async def send_websocket_error(
    websocket: WebSocket,
    *,
    message: str,
    code: str,
) -> None:
    """
    WebSocket istemcisine standart hata mesajı gönderir.
    """

    if (
        websocket.application_state
        != WebSocketState.CONNECTED
    ):
        return

    try:
        await websocket.send_json(
            {
                "type": "error",
                "code": code,
                "message": message,
                "sent_at": current_utc_iso(),
            }
        )
    except Exception:
        pass


async def close_websocket_safely(
    websocket: WebSocket,
    *,
    code: int,
    reason: str,
) -> None:
    """
    WebSocket bağlantısını hata yükseltmeden kapatır.
    """

    try:
        if (
            websocket.application_state
            != WebSocketState.DISCONNECTED
        ):
            await websocket.close(
                code=code,
                reason=reason,
            )
    except Exception:
        pass


def validate_device_from_token(
    db: Session,
    *,
    device_access_token: str,
    installation_id: str,
) -> tuple[
    RegisteredDevice,
    str,
]:
    """
    Cihaz tokenını ve SQLite cihaz kaydını doğrular.

    Dönen ikinci değer, ana backend kullanıcı kimliğidir.
    """

    token_payload = decode_device_access_token(
        device_access_token,
    )

    device = get_device_by_public_id(
        db,
        token_payload.device_public_id,
    )

    if device is None:
        raise InvalidDeviceTokenError(
            "Cihaz kaydı bulunamadı.",
        )

    if not device.is_active:
        raise InvalidDeviceTokenError(
            "Cihaz devre dışı bırakılmış.",
        )

    if (
        device.installation_id
        != installation_id
    ):
        raise InvalidDeviceTokenError(
            "Mesajdaki installation ID kayıtlı "
            "cihazla eşleşmiyor.",
        )

    if (
        device.installation_id
        != token_payload.installation_id
    ):
        raise InvalidDeviceTokenError(
            "Cihaz tokenındaki installation ID "
            "veritabanıyla eşleşmiyor.",
        )

    if (
        device.public_id
        != token_payload.device_public_id
    ):
        raise InvalidDeviceTokenError(
            "Cihaz tokenı yanlış cihaza ait.",
        )

    if (
        device.platform.value
        != token_payload.platform
    ):
        raise InvalidDeviceTokenError(
            "Cihaz platform bilgisi token ile "
            "eşleşmiyor.",
        )

    if (
        device.user.external_user_id
        != token_payload.external_user_id
    ):
        raise InvalidDeviceTokenError(
            "Cihaz kullanıcısı token bilgisiyle "
            "eşleşmiyor.",
        )

    return (
        device,
        token_payload.external_user_id,
    )


def update_device_last_seen(
    db: Session,
    *,
    device: RegisteredDevice,
    source_ip: str | None,
) -> None:
    """
    WebSocket mesajı alan cihazın son görülme ve
    son IP bilgilerini günceller.
    """

    device.last_seen_at = utc_now()
    device.last_ip = source_ip

    try:
        db.commit()
    except Exception:
        db.rollback()
        raise


def get_websocket_ip(
    websocket: WebSocket,
) -> str | None:
    """
    WebSocket bağlantısının kaynak IP adresini döndürür.

    Reverse proxy kullanıldığında X-Forwarded-For
    başlığının ilk değeri dikkate alınır.
    """

    forwarded_for = websocket.headers.get(
        "x-forwarded-for",
    )

    if forwarded_for:
        first_ip = (
            forwarded_for
            .split(",")[0]
            .strip()
        )

        if first_ip:
            return first_ip

    real_ip = websocket.headers.get(
        "x-real-ip",
    )

    if real_ip:
        normalized_real_ip = real_ip.strip()

        if normalized_real_ip:
            return normalized_real_ip

    if websocket.client is None:
        return None

    return websocket.client.host


async def receive_authentication_message(
    websocket: WebSocket,
) -> WebSocketAuthenticateMessage:
    """
    Bağlantının ilk mesajını timeout ile bekler ve
    authenticate mesajına dönüştürür.
    """

    try:
        raw_message: Any = await asyncio.wait_for(
            websocket.receive_json(),
            timeout=(
                AUTHENTICATION_TIMEOUT_SECONDS
            ),
        )

    except asyncio.TimeoutError as exception:
        raise InvalidDeviceTokenError(
            "WebSocket kimlik doğrulama mesajı "
            "zamanında gönderilmedi.",
        ) from exception

    except ValueError as exception:
        raise InvalidDeviceTokenError(
            "WebSocket ilk mesajı geçerli JSON değil.",
        ) from exception

    try:
        return (
            WebSocketAuthenticateMessage
            .model_validate(
                raw_message,
            )
        )

    except ValidationError as exception:
        raise InvalidDeviceTokenError(
            "WebSocket kimlik doğrulama mesajı "
            "geçersiz.",
        ) from exception


async def handle_authenticated_message(
    websocket: WebSocket,
    *,
    message: Any,
    device: RegisteredDevice,
    source_ip: str | None,
    db: Session,
) -> bool:
    """
    Doğrulanmış cihazdan gelen WebSocket mesajını işler.

    True dönerse bağlantı açık tutulur.
    False dönerse bağlantı kapatılır.
    """

    if not isinstance(
        message,
        dict,
    ):
        await send_websocket_error(
            websocket,
            code="invalid_message",
            message=(
                "WebSocket mesajı JSON nesnesi "
                "olmalıdır."
            ),
        )

        return True

    message_type = str(
        message.get(
            "type",
            "",
        )
    ).strip().lower()

    if message_type == "heartbeat":
        try:
            heartbeat_message = (
                WebSocketHeartbeatMessage
                .model_validate(
                    message,
                )
            )

        except ValidationError:
            await send_websocket_error(
                websocket,
                code="invalid_heartbeat",
                message=(
                    "Heartbeat mesajı geçersiz."
                ),
            )

            return True

        update_device_last_seen(
            db,
            device=device,
            source_ip=source_ip,
        )

        await websocket_manager.update_last_seen(
            device.public_id,
        )

        await websocket.send_json(
            {
                "type": "heartbeat_ack",
                "message": (
                    "Heartbeat alındı."
                ),
                "received_at": (
                    heartbeat_message
                    .sent_at
                    .isoformat()
                ),
                "sent_at": current_utc_iso(),
            }
        )

        return True

    if message_type == "disconnect":
        await websocket.send_json(
            {
                "type": "disconnect_ack",
                "message": (
                    "Bağlantı istemci isteğiyle "
                    "kapatılıyor."
                ),
                "sent_at": current_utc_iso(),
            }
        )

        return False

    await send_websocket_error(
        websocket,
        code="unsupported_message_type",
        message=(
            f"Desteklenmeyen mesaj türü: "
            f"{message_type or '(boş)'}"
        ),
    )

    return True


@router.websocket(
    "/ws/device",
    name="Authenticator cihaz WebSocket bağlantısı",
)
async def device_websocket(
    websocket: WebSocket,
) -> None:
    """
    Android ve iOS Authenticator istemcilerinin
    bağlanacağı WebSocket endpointidir.

    Bağlantı açıldıktan sonra ilk mesaj mutlaka:

    {
        "type": "authenticate",
        "installation_id": "...",
        "device_access_token": "..."
    }

    biçiminde olmalıdır.
    """

    await websocket.accept()

    database_session = SessionLocal()

    authenticated_device: (
        RegisteredDevice | None
    ) = None

    source_ip = get_websocket_ip(
        websocket,
    )

    try:
        authentication_message = (
            await receive_authentication_message(
                websocket,
            )
        )

        try:
            (
                authenticated_device,
                external_user_id,
            ) = validate_device_from_token(
                database_session,
                device_access_token=(
                    authentication_message
                    .device_access_token
                ),
                installation_id=(
                    authentication_message
                    .installation_id
                ),
            )

        except ExpiredDeviceTokenError as exception:
            await send_websocket_error(
                websocket,
                code="device_token_expired",
                message=str(
                    exception,
                ),
            )

            await close_websocket_safely(
                websocket,
                code=4003,
                reason=(
                    "Cihaz tokenının süresi dolmuş."
                ),
            )

            return

        except InvalidDeviceTokenError as exception:
            await send_websocket_error(
                websocket,
                code="invalid_device_token",
                message=str(
                    exception,
                ),
            )

            await close_websocket_safely(
                websocket,
                code=4003,
                reason=(
                    "Cihaz kimliği doğrulanamadı."
                ),
            )

            return

        update_device_last_seen(
            database_session,
            device=authenticated_device,
            source_ip=source_ip,
        )

        await websocket_manager.connect(
            device_public_id=(
                authenticated_device.public_id
            ),
            external_user_id=external_user_id,
            installation_id=(
                authenticated_device.installation_id
            ),
            platform=(
                authenticated_device.platform.value
            ),
            websocket=websocket,
        )

        await websocket.send_json(
            {
                "type": "authenticated",
                "message": (
                    "Authenticator cihazı başarıyla "
                    "doğrulandı."
                ),
                "device": {
                    "public_id": (
                        authenticated_device.public_id
                    ),
                    "installation_id": (
                        authenticated_device
                        .installation_id
                    ),
                    "platform": (
                        authenticated_device
                        .platform
                        .value
                    ),
                    "device_name": (
                        authenticated_device.device_name
                    ),
                },
                "connected_at": current_utc_iso(),
            }
        )

        while True:
            try:
                incoming_message = (
                    await websocket.receive_json()
                )

            except ValueError:
                await send_websocket_error(
                    websocket,
                    code="invalid_json",
                    message=(
                        "Gönderilen mesaj geçerli "
                        "JSON değil."
                    ),
                )

                continue

            should_continue = (
                await handle_authenticated_message(
                    websocket,
                    message=incoming_message,
                    device=authenticated_device,
                    source_ip=source_ip,
                    db=database_session,
                )
            )

            if not should_continue:
                break

    except WebSocketDisconnect:
        pass

    except InvalidDeviceTokenError as exception:
        await send_websocket_error(
            websocket,
            code="authentication_failed",
            message=str(
                exception,
            ),
        )

    except Exception:
        await send_websocket_error(
            websocket,
            code="internal_server_error",
            message=(
                "WebSocket bağlantısında beklenmeyen "
                "bir hata oluştu."
            ),
        )

    finally:
        if authenticated_device is not None:
            await websocket_manager.disconnect(
                authenticated_device.public_id,
                websocket,
            )

        database_session.close()

        await close_websocket_safely(
            websocket,
            code=1000,
            reason="WebSocket bağlantısı kapatıldı.",
        )