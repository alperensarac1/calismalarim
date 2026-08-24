from __future__ import annotations

from dataclasses import dataclass
from typing import Any

import httpx

from app.config import get_settings


settings = get_settings()


class BackendIdentityError(Exception):
    """
    Mevcut .NET backend üzerinden kullanıcı kimliği
    doğrulanamadığında kullanılan temel hata sınıfıdır.
    """

    pass


class InvalidBackendTokenError(BackendIdentityError):
    """
    Access token geçersiz, süresi dolmuş veya yetkisiz
    olduğunda yükseltilir.
    """

    pass


class BackendUnavailableError(BackendIdentityError):
    """
    Mevcut .NET backend'e bağlantı kurulamadığında
    veya backend cevap vermediğinde yükseltilir.
    """

    pass


class InvalidBackendResponseError(BackendIdentityError):
    """
    Backend cevabı beklenen JSON yapısında olmadığında
    yükseltilir.
    """

    pass


@dataclass(frozen=True, slots=True)
class VerifiedBackendUser:
    """
    Mevcut backend tarafından doğrulanmış kullanıcı bilgisi.

    Python servisi cihaz kaydı sırasında yalnızca bu modeldeki
    doğrulanmış kullanıcı bilgilerine güvenecektir.
    """

    external_user_id: str
    email: str | None
    display_name: str | None
    role: str | None
    is_active: bool
    raw_data: dict[str, Any]


def _normalize_optional_string(
    value: Any,
) -> str | None:
    """
    Verilen değeri temizlenmiş string biçimine dönüştürür.

    None veya boş string gelirse None döndürür.
    """

    if value is None:
        return None

    normalized_value = str(value).strip()

    if not normalized_value:
        return None

    return normalized_value


def _first_available_value(
    data: dict[str, Any],
    *keys: str,
) -> Any:
    """
    Bir sözlükte verilen alan adlarından ilk bulunan
    ve boş olmayan değeri döndürür.

    Backend response alanlarının camelCase veya PascalCase
    gelmesi ihtimaline karşı birden fazla isim desteklenir.
    """

    for key in keys:
        if key not in data:
            continue

        value = data[key]

        if value is not None:
            return value

    return None


def _extract_response_data(
    response_body: Any,
) -> dict[str, Any]:
    """
    Backend'in ApiResponse<T> yapısından data alanını çıkarır.

    Beklenen örnek:

    {
        "success": true,
        "message": "İşlem başarılı.",
        "data": {
            "id": 5,
            "email": "admin@example.com"
        }
    }
    """

    if not isinstance(
        response_body,
        dict,
    ):
        raise InvalidBackendResponseError(
            "Backend cevabı JSON nesnesi değil.",
        )

    success_value = _first_available_value(
        response_body,
        "success",
        "Success",
    )

    if success_value is False:
        message = _normalize_optional_string(
            _first_available_value(
                response_body,
                "message",
                "Message",
            ),
        )

        raise InvalidBackendResponseError(
            message
            or "Backend kullanıcı bilgisini döndürmedi.",
        )

    data = _first_available_value(
        response_body,
        "data",
        "Data",
    )

    if not isinstance(
        data,
        dict,
    ):
        raise InvalidBackendResponseError(
            "Backend cevabında geçerli bir data alanı bulunamadı.",
        )

    return data


def _extract_display_name(
    data: dict[str, Any],
) -> str | None:
    """
    Kullanıcının tam adını backend cevabından çıkarır.

    Öncelikle fullName/displayName alanları aranır.
    Bunlar bulunamazsa firstName ve lastName birleştirilir.
    """

    direct_name = _normalize_optional_string(
        _first_available_value(
            data,
            "fullName",
            "FullName",
            "displayName",
            "DisplayName",
            "name",
            "Name",
        ),
    )

    if direct_name:
        return direct_name

    first_name = _normalize_optional_string(
        _first_available_value(
            data,
            "firstName",
            "FirstName",
        ),
    )

    last_name = _normalize_optional_string(
        _first_available_value(
            data,
            "lastName",
            "LastName",
        ),
    )

    name_parts = [
        part
        for part in (
            first_name,
            last_name,
        )
        if part
    ]

    if not name_parts:
        return None

    return " ".join(name_parts)


def _extract_role(
    data: dict[str, Any],
) -> str | None:
    """
    Kullanıcının rol bilgisini backend cevabından çıkarır.

    Rol string olarak veya roles listesi içinde gelebilir.
    Birden fazla rol varsa virgülle birleştirilir.
    """

    direct_role = _normalize_optional_string(
        _first_available_value(
            data,
            "role",
            "Role",
            "userRole",
            "UserRole",
        ),
    )

    if direct_role:
        return direct_role

    roles_value = _first_available_value(
        data,
        "roles",
        "Roles",
    )

    if not isinstance(
        roles_value,
        list,
    ):
        return None

    roles = [
        normalized_role
        for role in roles_value
        if (
            normalized_role
            := _normalize_optional_string(
                role,
            )
        )
    ]

    if not roles:
        return None

    return ",".join(roles)


def _extract_is_active(
    data: dict[str, Any],
) -> bool:
    """
    Backend cevabındaki aktiflik durumunu okur.

    Backend cevapta isActive alanı göndermiyorsa,
    access token ile /api/Auth/me çağrısı başarılı olduğu
    için kullanıcı aktif kabul edilir.
    """

    value = _first_available_value(
        data,
        "isActive",
        "IsActive",
        "active",
        "Active",
    )

    if value is None:
        return True

    if isinstance(
        value,
        bool,
    ):
        return value

    if isinstance(
        value,
        str,
    ):
        return value.strip().lower() in {
            "true",
            "1",
            "yes",
        }

    return bool(value)


def _map_verified_user(
    data: dict[str, Any],
) -> VerifiedBackendUser:
    """
    Backend response verisini platform bağımsız
    VerifiedBackendUser modeline dönüştürür.
    """

    user_id_value = _first_available_value(
        data,
        "id",
        "Id",
        "userId",
        "UserId",
    )

    external_user_id = _normalize_optional_string(
        user_id_value,
    )

    if not external_user_id:
        raise InvalidBackendResponseError(
            "Backend cevabında kullanıcı kimliği bulunamadı.",
        )

    email = _normalize_optional_string(
        _first_available_value(
            data,
            "email",
            "Email",
            "userEmail",
            "UserEmail",
        ),
    )

    return VerifiedBackendUser(
        external_user_id=external_user_id,
        email=email,
        display_name=_extract_display_name(
            data,
        ),
        role=_extract_role(
            data,
        ),
        is_active=_extract_is_active(
            data,
        ),
        raw_data=data,
    )


class BackendIdentityService:
    """
    Mevcut ProjectManagement backend'i üzerinden
    access token doğrulaması yapan servistir.

    Bu servis mevcut backend'de herhangi bir değişiklik
    gerektirmez. Var olan /api/Auth/me endpointini kullanır.
    """

    def __init__(
        self,
        base_url: str | None = None,
        current_user_path: str | None = None,
        timeout_seconds: float | None = None,
    ) -> None:
        self._base_url = (
            base_url
            or settings.main_backend_base_url
        ).rstrip("/")

        self._current_user_path = (
            current_user_path
            or settings.main_backend_current_user_path
        )

        if not self._current_user_path.startswith(
            "/",
        ):
            self._current_user_path = (
                f"/{self._current_user_path}"
            )

        self._timeout_seconds = (
            timeout_seconds
            or settings.main_backend_timeout_seconds
        )

    @property
    def current_user_url(self) -> str:
        """
        Backend aktif kullanıcı endpointinin
        tam adresini döndürür.
        """

        return (
            f"{self._base_url}"
            f"{self._current_user_path}"
        )

    async def verify_access_token(
        self,
        access_token: str,
    ) -> VerifiedBackendUser:
        """
        Access tokenı mevcut backend'in /api/Auth/me
        endpointine göndererek doğrular.

        Token geçerliyse doğrulanmış kullanıcı bilgisi döner.
        """

        normalized_token = access_token.strip()

        if not normalized_token:
            raise InvalidBackendTokenError(
                "Backend access token boş olamaz.",
            )

        headers = {
            "Authorization": (
                f"Bearer {normalized_token}"
            ),
            "Accept": "application/json",
        }

        timeout = httpx.Timeout(
            self._timeout_seconds,
        )

        try:
            async with httpx.AsyncClient(
                timeout=timeout,
            ) as client:
                response = await client.get(
                    self.current_user_url,
                    headers=headers,
                )

        except httpx.TimeoutException as exception:
            raise BackendUnavailableError(
                "Mevcut backend kullanıcı doğrulama "
                "isteğine zamanında cevap vermedi.",
            ) from exception

        except httpx.RequestError as exception:
            raise BackendUnavailableError(
                "Mevcut backend'e bağlantı kurulamadı: "
                f"{exception}",
            ) from exception

        if response.status_code in {
            401,
            403,
        }:
            raise InvalidBackendTokenError(
                "Backend access token geçersiz veya süresi dolmuş.",
            )

        if response.status_code >= 500:
            raise BackendUnavailableError(
                "Mevcut backend kullanıcı doğrulaması "
                "sırasında sunucu hatası döndürdü.",
            )

        if not response.is_success:
            raise InvalidBackendResponseError(
                "Backend kullanıcı doğrulaması başarısız oldu. "
                f"HTTP durum kodu: {response.status_code}",
            )

        try:
            response_body = response.json()
        except ValueError as exception:
            raise InvalidBackendResponseError(
                "Backend geçerli bir JSON cevabı döndürmedi.",
            ) from exception

        response_data = _extract_response_data(
            response_body,
        )

        verified_user = _map_verified_user(
            response_data,
        )

        if not verified_user.is_active:
            raise InvalidBackendTokenError(
                "Kullanıcı hesabı aktif değil.",
            )

        return verified_user


backend_identity_service = BackendIdentityService()