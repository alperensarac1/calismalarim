from __future__ import annotations

from dataclasses import dataclass

from fastapi import (
    APIRouter,
    Depends,
    HTTPException,
    Request,
    status,
)
from fastapi.security import (
    HTTPAuthorizationCredentials,
    HTTPBearer,
)
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import RegisteredDevice
from app.schemas import (
    ApiResponse,
    DeviceHeartbeatRequest,
    DeviceRegistrationRequest,
    DeviceRegistrationResponse,
    RegisteredDeviceResponse,
)
from app.security import (
    ExpiredDeviceTokenError,
    InvalidDeviceTokenError,
    DeviceTokenPayload,
    decode_device_access_token,
)
from app.services.device_service import (
    DeviceNotFoundError,
    DeviceRegistrationError,
    DeviceServiceError,
    InactiveDeviceError,
    get_device_by_public_id,
    device_service,
)


router = APIRouter(
    prefix="/api/devices",
    tags=["Devices"],
)


# HTTP Authorization header içerisindeki Bearer tokenı okur.
#
# auto_error=False kullanıldığı için token bulunamazsa
# otomatik hata yerine kendi Türkçe hata mesajımızı döndürebiliriz.
bearer_scheme = HTTPBearer(
    auto_error=False,
)


@dataclass(frozen=True, slots=True)
class AuthenticatedDeviceContext:
    """
    Doğrulanmış cihaz tokenı ve veritabanındaki
    cihaz kaydını birlikte taşır.

    Endpointler cihaz tokenındaki ham verilere değil,
    bu doğrulanmış context nesnesine güvenir.
    """

    token_payload: DeviceTokenPayload

    device: RegisteredDevice


def get_request_ip(
    request: Request,
) -> str | None:
    """
    İsteğin IP adresini döndürür.

    Reverse proxy kullanılıyorsa X-Forwarded-For
    başlığındaki ilk IP adresini kullanır.

    Production ortamında X-Forwarded-For yalnızca
    güvenilir reverse proxy üzerinden kabul edilmelidir.
    """

    forwarded_for = request.headers.get(
        "x-forwarded-for",
    )

    if forwarded_for:
        first_forwarded_ip = (
            forwarded_for
            .split(",")[0]
            .strip()
        )

        if first_forwarded_ip:
            return first_forwarded_ip

    real_ip = request.headers.get(
        "x-real-ip",
    )

    if real_ip:
        normalized_real_ip = real_ip.strip()

        if normalized_real_ip:
            return normalized_real_ip

    if request.client is None:
        return None

    return request.client.host


def map_registered_device_response(
    device: RegisteredDevice,
) -> RegisteredDeviceResponse:
    """
    SQLAlchemy RegisteredDevice modelini API
    response modeline dönüştürür.

    public_key, push_token ve key_attestation gibi
    hassas alanlar response içerisinde gönderilmez.
    """

    return RegisteredDeviceResponse(
        public_id=device.public_id,
        installation_id=device.installation_id,
        platform=device.platform,
        device_name=device.device_name,
        device_model=device.device_model,
        manufacturer=device.manufacturer,
        os_name=device.os_name,
        os_version=device.os_version,
        app_version=device.app_version,
        locale=device.locale,
        timezone_name=device.timezone_name,
        key_algorithm=device.key_algorithm,
        public_key_fingerprint=(
            device.public_key_fingerprint
        ),
        key_created_at=device.key_created_at,
        key_attestation_verified=(
            device.key_attestation_verified
        ),
        is_active=device.is_active,
        registered_ip=device.registered_ip,
        last_ip=device.last_ip,
        registered_at=device.registered_at,
        last_seen_at=device.last_seen_at,
        revoked_at=device.revoked_at,
    )


def get_device_access_token(
    credentials: (
        HTTPAuthorizationCredentials | None
    ) = Depends(
        bearer_scheme,
    ),
) -> str:
    """
    Authorization header içerisindeki Bearer
    cihaz tokenını alır.
    """

    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Cihaz access tokenı gönderilmedi."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    if credentials.scheme.lower() != "bearer":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Authorization yöntemi Bearer olmalıdır."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    token = credentials.credentials.strip()

    if not token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Cihaz access tokenı boş olamaz."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    return token


def get_authenticated_device_context(
    device_access_token: str = Depends(
        get_device_access_token,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> AuthenticatedDeviceContext:
    """
    Cihaz JWT'sini ve SQLite cihaz kaydını doğrular.

    Token geçerli olsa bile cihaz veritabanında
    pasif veya iptal edilmişse erişim verilmez.
    """

    try:
        token_payload = decode_device_access_token(
            device_access_token,
        )

    except ExpiredDeviceTokenError as exception:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(
                exception,
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        ) from exception

    except InvalidDeviceTokenError as exception:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(
                exception,
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        ) from exception

    device = get_device_by_public_id(
        db,
        token_payload.device_public_id,
    )

    if device is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Cihaz kaydı bulunamadı."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    if not device.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=(
                "Cihaz devre dışı bırakılmış."
            ),
        )

    if (
        device.installation_id
        != token_payload.installation_id
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Cihaz installation ID bilgisi "
                "token ile eşleşmiyor."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    if (
        str(device.user.external_user_id)
        != token_payload.external_user_id
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Cihaz kullanıcı bilgisi "
                "token ile eşleşmiyor."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    if (
        device.platform.value
        != token_payload.platform
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=(
                "Cihaz platform bilgisi "
                "token ile eşleşmiyor."
            ),
            headers={
                "WWW-Authenticate": "Bearer",
            },
        )

    return AuthenticatedDeviceContext(
        token_payload=token_payload,
        device=device,
    )


@router.post(
    "/register",
    response_model=ApiResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Authenticator cihazını kaydeder",
)
async def register_device(
    request_body: DeviceRegistrationRequest,
    request: Request,
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Android, iOS veya desteklenen diğer platformlardaki
    Authenticator uygulamasını kaydeder.

    Kullanıcının kimliği request içerisindeki bir kullanıcı
    ID değerinden alınmaz.

    backend_access_token mevcut .NET backend'in
    /api/Auth/me endpointine gönderilerek kullanıcı
    güvenilir biçimde doğrulanır.
    """

    request_ip = get_request_ip(
        request,
    )

    try:
        result = await device_service.register_device(
            db,
            request=request_body,
            request_ip=request_ip,
        )

    except DeviceRegistrationError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    except DeviceServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    except Exception as exception:
        raise HTTPException(
            status_code=(
                status.HTTP_500_INTERNAL_SERVER_ERROR
            ),
            detail=(
                "Cihaz kaydı sırasında beklenmeyen "
                "bir hata oluştu."
            ),
        ) from exception

    response_data = DeviceRegistrationResponse(
        device=map_registered_device_response(
            result.device,
        ),
        device_access_token=(
            result.device_access_token
        ),
        token_type="bearer",
        expires_at=result.token_expires_at,
    )

    if result.is_new_device:
        message = (
            "Authenticator cihazı başarıyla kaydedildi."
        )
    else:
        message = (
            "Authenticator cihaz kaydı "
            "başarıyla güncellendi."
        )

    return ApiResponse(
        success=True,
        message=message,
        data=response_data.model_dump(
            mode="json",
        ),
        errors={},
    )


@router.post(
    "/heartbeat",
    response_model=ApiResponse,
    summary="Cihazın aktif olduğunu bildirir",
)
def heartbeat_device(
    request_body: DeviceHeartbeatRequest,
    request: Request,
    context: AuthenticatedDeviceContext = Depends(
        get_authenticated_device_context,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Mobil cihazın hâlâ aktif olduğunu bildirir.

    Mobil uygulama bu endpointi belirli aralıklarla
    veya WebSocket bağlantısından önce çağırabilir.
    """

    if (
        request_body.installation_id
        != context.device.installation_id
    ):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=(
                "Heartbeat installation ID bilgisi "
                "kayıtlı cihazla eşleşmiyor."
            ),
        )

    request_ip = get_request_ip(
        request,
    )

    try:
        result = device_service.heartbeat(
            db,
            device_public_id=(
                context.device.public_id
            ),
            installation_id=(
                request_body.installation_id
            ),
            request_ip=request_ip,
            app_version=request_body.app_version,
            os_version=request_body.os_version,
            push_token=request_body.push_token,
        )

    except DeviceNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception

    except InactiveDeviceError as exception:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=str(
                exception,
            ),
        ) from exception

    except DeviceServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    return ApiResponse(
        success=True,
        message=(
            "Cihaz heartbeat bilgisi güncellendi."
        ),
        data={
            "device": (
                map_registered_device_response(
                    result.device,
                ).model_dump(
                    mode="json",
                )
            ),
            "updated_at": (
                result.updated_at.isoformat()
            ),
        },
        errors={},
    )


@router.get(
    "/me",
    response_model=ApiResponse,
    summary="Doğrulanmış cihaz bilgisini getirir",
)
def get_current_device(
    context: AuthenticatedDeviceContext = Depends(
        get_authenticated_device_context,
    ),
) -> ApiResponse:
    """
    Bearer cihaz tokenına ait kayıtlı cihazı getirir.
    """

    device_response = (
        map_registered_device_response(
            context.device,
        )
    )

    return ApiResponse(
        success=True,
        message=(
            "Cihaz bilgisi başarıyla getirildi."
        ),
        data=device_response.model_dump(
            mode="json",
        ),
        errors={},
    )


@router.get(
    "/my-devices",
    response_model=ApiResponse,
    summary="Kullanıcının cihazlarını listeler",
)
def get_my_devices(
    include_inactive: bool = False,
    context: AuthenticatedDeviceContext = Depends(
        get_authenticated_device_context,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Token sahibinin bütün Authenticator cihazlarını listeler.

    Varsayılan olarak yalnızca aktif cihazlar döndürülür.
    """

    devices = device_service.get_user_devices(
        db,
        external_user_id=(
            context.token_payload.external_user_id
        ),
        include_inactive=include_inactive,
    )

    device_items = [
        map_registered_device_response(
            device,
        ).model_dump(
            mode="json",
        )
        for device in devices
    ]

    return ApiResponse(
        success=True,
        message=(
            "Kullanıcının cihazları "
            "başarıyla getirildi."
        ),
        data={
            "items": device_items,
            "total_count": len(
                device_items,
            ),
        },
        errors={},
    )


@router.delete(
    "/{device_public_id}",
    response_model=ApiResponse,
    summary="Authenticator cihazını devre dışı bırakır",
)
def revoke_device(
    device_public_id: str,
    context: AuthenticatedDeviceContext = Depends(
        get_authenticated_device_context,
    ),
    db: Session = Depends(
        get_db,
    ),
) -> ApiResponse:
    """
    Kullanıcının kendisine ait Authenticator cihazını
    devre dışı bırakır.

    Cihaz veritabanından silinmez. Güvenlik loglarının
    bütünlüğünü korumak için pasif duruma alınır.
    """

    target_device = get_device_by_public_id(
        db,
        device_public_id,
    )

    if target_device is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=(
                "Devre dışı bırakılacak cihaz bulunamadı."
            ),
        )

    if (
        target_device.user_id
        != context.device.user_id
    ):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=(
                "Başka bir kullanıcıya ait cihazı "
                "devre dışı bırakamazsınız."
            ),
        )

    try:
        revoked_device = (
            device_service.revoke_device(
                db,
                device_public_id=device_public_id,
            )
        )

    except DeviceNotFoundError as exception:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=str(
                exception,
            ),
        ) from exception

    except DeviceServiceError as exception:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(
                exception,
            ),
        ) from exception

    return ApiResponse(
        success=True,
        message=(
            "Authenticator cihazı "
            "devre dışı bırakıldı."
        ),
        data=(
            map_registered_device_response(
                revoked_device,
            ).model_dump(
                mode="json",
            )
        ),
        errors={},
    )