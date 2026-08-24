from __future__ import annotations

import asyncio

from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any

from fastapi import WebSocket
from starlette.websockets import WebSocketState


def utc_now() -> datetime:
    """
    UTC zaman dilimindeki güncel zamanı döndürür.
    """

    return datetime.now(timezone.utc)


@dataclass(slots=True)
class DeviceWebSocketConnection:
    """
    Aktif bir mobil Authenticator WebSocket bağlantısını
    temsil eder.

    Android ve iOS için aynı bağlantı modeli kullanılır.
    """

    device_public_id: str

    external_user_id: str

    installation_id: str

    platform: str

    websocket: WebSocket

    connected_at: datetime

    last_seen_at: datetime


class WebSocketConnectionManager:
    """
    Authenticator cihazlarının aktif WebSocket
    bağlantılarını yöneten sınıftır.

    Bağlantılar bellekte tutulur. Uygulama yeniden
    başlatıldığında aktif bağlantılar sıfırlanır.

    İlk geliştirme sürümü için bu yaklaşım yeterlidir.
    Birden fazla Python sunucu örneği kullanılacağı zaman
    Redis Pub/Sub gibi merkezi bir yapı eklenebilir.
    """

    def __init__(self) -> None:
        # device_public_id -> bağlantı
        self._connections: dict[
            str,
            DeviceWebSocketConnection,
        ] = {}

        # Aynı anda bağlantı ekleme, silme ve mesaj
        # gönderme işlemlerinin çakışmasını önler.
        self._lock = asyncio.Lock()

    async def connect(
            self,
            *,
            device_public_id: str,
            external_user_id: str,
            installation_id: str,
            platform: str,
            websocket: WebSocket,
    ) -> DeviceWebSocketConnection:
        """
        Daha önce kabul edilmiş WebSocket bağlantısını
        kayıtlı Authenticator cihazıyla eşleştirir.

        WebSocket.accept() işlemi router içerisinde yapılır.
        Aynı cihazın önceden açık bağlantısı varsa eski
        bağlantı kapatılır ve yeni bağlantı kullanılır.
        """

        now = utc_now()

        connection = DeviceWebSocketConnection(
            device_public_id=device_public_id,
            external_user_id=external_user_id,
            installation_id=installation_id,
            platform=platform,
            websocket=websocket,
            connected_at=now,
            last_seen_at=now,
        )

        previous_connection: (
                DeviceWebSocketConnection | None
        ) = None

        async with self._lock:
            previous_connection = self._connections.get(
                device_public_id,
            )

            self._connections[
                device_public_id
            ] = connection

        if (
                previous_connection is not None
                and previous_connection.websocket
                is not websocket
        ):
            await self._safe_close(
                previous_connection.websocket,
                code=4001,
                reason=(
                    "Aynı cihazdan yeni bağlantı açıldı."
                ),
            )

        return connection

    async def disconnect(
        self,
        device_public_id: str,
        websocket: WebSocket | None = None,
    ) -> None:
        """
        Cihazın WebSocket bağlantısını listeden çıkarır.

        websocket parametresi verilirse yalnızca kayıtlı
        bağlantı aynı nesneyse silinir. Böylece eski bir
        bağlantının kapanması yeni bağlantıyı yanlışlıkla
        silemez.
        """

        async with self._lock:
            existing_connection = (
                self._connections.get(
                    device_public_id,
                )
            )

            if existing_connection is None:
                return

            if (
                websocket is not None
                and existing_connection.websocket
                is not websocket
            ):
                return

            self._connections.pop(
                device_public_id,
                None,
            )

    async def update_last_seen(
        self,
        device_public_id: str,
    ) -> None:
        """
        Bağlantının son görülme zamanını günceller.
        """

        async with self._lock:
            connection = self._connections.get(
                device_public_id,
            )

            if connection is None:
                return

            connection.last_seen_at = utc_now()

    async def is_connected(
        self,
        device_public_id: str,
    ) -> bool:
        """
        Belirtilen cihazın aktif WebSocket bağlantısı
        olup olmadığını döndürür.
        """

        async with self._lock:
            connection = self._connections.get(
                device_public_id,
            )

        if connection is None:
            return False

        return self._is_socket_connected(
            connection.websocket,
        )

    async def get_connection(
        self,
        device_public_id: str,
    ) -> DeviceWebSocketConnection | None:
        """
        Cihazın aktif bağlantısını döndürür.
        """

        async with self._lock:
            connection = self._connections.get(
                device_public_id,
            )

        if connection is None:
            return None

        if not self._is_socket_connected(
            connection.websocket,
        ):
            await self.disconnect(
                device_public_id,
                connection.websocket,
            )

            return None

        return connection

    async def send_json_to_device(
        self,
        device_public_id: str,
        message: dict[str, Any],
    ) -> bool:
        """
        Belirtilen cihaza JSON mesaj gönderir.

        Mesaj başarıyla gönderilirse True,
        bağlantı yoksa veya gönderim başarısızsa False döner.
        """

        connection = await self.get_connection(
            device_public_id,
        )

        if connection is None:
            return False

        try:
            await connection.websocket.send_json(
                message,
            )

            await self.update_last_seen(
                device_public_id,
            )

            return True

        except Exception:
            await self.disconnect(
                device_public_id,
                connection.websocket,
            )

            await self._safe_close(
                connection.websocket,
                code=1011,
                reason=(
                    "Mesaj gönderimi sırasında "
                    "bağlantı hatası oluştu."
                ),
            )

            return False

    async def send_text_to_device(
        self,
        device_public_id: str,
        message: str,
    ) -> bool:
        """
        Belirtilen cihaza düz metin mesaj gönderir.
        """

        connection = await self.get_connection(
            device_public_id,
        )

        if connection is None:
            return False

        try:
            await connection.websocket.send_text(
                message,
            )

            await self.update_last_seen(
                device_public_id,
            )

            return True

        except Exception:
            await self.disconnect(
                device_public_id,
                connection.websocket,
            )

            await self._safe_close(
                connection.websocket,
                code=1011,
                reason=(
                    "Mesaj gönderimi sırasında "
                    "bağlantı hatası oluştu."
                ),
            )

            return False

    async def send_to_user_devices(
        self,
        external_user_id: str,
        message: dict[str, Any],
    ) -> list[str]:
        """
        Kullanıcının bağlı olan bütün cihazlarına
        aynı JSON mesajını gönderir.

        Mesajın başarıyla gönderildiği cihaz public ID
        değerlerini döndürür.
        """

        async with self._lock:
            target_device_ids = [
                connection.device_public_id
                for connection
                in self._connections.values()
                if (
                    connection.external_user_id
                    == external_user_id
                )
            ]

        successful_device_ids: list[str] = []

        for device_public_id in target_device_ids:
            is_sent = await self.send_json_to_device(
                device_public_id,
                message,
            )

            if is_sent:
                successful_device_ids.append(
                    device_public_id,
                )

        return successful_device_ids

    async def broadcast(
        self,
        message: dict[str, Any],
    ) -> list[str]:
        """
        Bağlı bütün Authenticator cihazlarına mesaj gönderir.

        Bu yöntem yalnızca yönetim, bakım veya test amacıyla
        kullanılmalıdır.
        """

        async with self._lock:
            target_device_ids = list(
                self._connections.keys(),
            )

        successful_device_ids: list[str] = []

        for device_public_id in target_device_ids:
            is_sent = await self.send_json_to_device(
                device_public_id,
                message,
            )

            if is_sent:
                successful_device_ids.append(
                    device_public_id,
                )

        return successful_device_ids

    async def get_connected_devices(
        self,
    ) -> list[dict[str, Any]]:
        """
        Aktif bağlantıları güvenli özet veri olarak döndürür.

        WebSocket nesnesi response içine eklenmez.
        """

        async with self._lock:
            connections = list(
                self._connections.values(),
            )

        items: list[dict[str, Any]] = []

        for connection in connections:
            if not self._is_socket_connected(
                connection.websocket,
            ):
                continue

            items.append(
                {
                    "device_public_id": (
                        connection.device_public_id
                    ),
                    "external_user_id": (
                        connection.external_user_id
                    ),
                    "installation_id": (
                        connection.installation_id
                    ),
                    "platform": connection.platform,
                    "connected_at": (
                        connection.connected_at.isoformat()
                    ),
                    "last_seen_at": (
                        connection.last_seen_at.isoformat()
                    ),
                }
            )

        return items

    async def close_all(
        self,
        *,
        code: int = 1001,
        reason: str = "Sunucu kapatılıyor.",
    ) -> None:
        """
        Sunucu kapanırken bütün WebSocket
        bağlantılarını güvenli şekilde kapatır.
        """

        async with self._lock:
            connections = list(
                self._connections.values(),
            )

            self._connections.clear()

        for connection in connections:
            await self._safe_close(
                connection.websocket,
                code=code,
                reason=reason,
            )

    @staticmethod
    def _is_socket_connected(
        websocket: WebSocket,
    ) -> bool:
        """
        WebSocket bağlantısının açık olup olmadığını
        Starlette durumları üzerinden kontrol eder.
        """

        return (
            websocket.application_state
            == WebSocketState.CONNECTED
            and websocket.client_state
            == WebSocketState.CONNECTED
        )

    @staticmethod
    async def _safe_close(
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
            # Bağlantı zaten kapanmışsa veya ağ hatası
            # oluşmuşsa ek işlem yapmıyoruz.
            pass


websocket_manager = WebSocketConnectionManager()