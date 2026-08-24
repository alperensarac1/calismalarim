from __future__ import annotations

from datetime import datetime
from typing import Literal

from pydantic import (
    BaseModel,
    ConfigDict,
    EmailStr,
    Field,
    field_validator,
)

from app.models import (
    AuthenticationMethod,
    AuthenticationResult,
    ChallengeStatus,
    DevicePlatform,
)


# =========================================================
# ORTAK RESPONSE MODELLERİ
# =========================================================


class ApiResponse(BaseModel):
    """
    Bütün endpointlerde kullanılabilecek ortak
    response yapısıdır.

    data alanı endpoint türüne göre farklı bir nesne
    içerebilir.
    """

    success: bool = True

    message: str = "İşlem başarılı."

    data: object | None = None

    errors: dict[str, list[str]] = Field(
        default_factory=dict,
    )


class PaginationMetadata(BaseModel):
    """
    Sayfalı endpointlerin sayfalama bilgilerini
    temsil eder.
    """

    page: int = Field(
        ge=1,
    )

    page_size: int = Field(
        ge=1,
    )

    total_count: int = Field(
        ge=0,
    )

    total_pages: int = Field(
        ge=0,
    )

    has_previous_page: bool

    has_next_page: bool


# =========================================================
# HARİCİ KULLANICI MODELLERİ
# =========================================================


class ExternalUserCreateRequest(BaseModel):
    """
    Ana uygulamadaki kullanıcıyı Authenticator Service
    içerisine kaydetmek için kullanılır.

    Ana uygulamanın bütün kullanıcı bilgileri burada
    tutulmaz. Yalnızca MFA eşleştirmesi için gerekli
    minimum bilgiler alınır.
    """

    external_user_id: str = Field(
        min_length=1,
        max_length=150,
    )

    email: EmailStr | None = None

    display_name: str | None = Field(
        default=None,
        max_length=200,
    )

    mfa_enabled: bool = True

    @field_validator(
        "external_user_id",
        "display_name",
        mode="before",
    )
    @classmethod
    def normalize_optional_text(
        cls,
        value: object,
    ) -> object:
        """
        String alanların başındaki ve sonundaki
        boşlukları temizler.
        """

        if isinstance(value, str):
            normalized_value = value.strip()

            if normalized_value == "":
                return None

            return normalized_value

        return value


class ExternalUserUpdateRequest(BaseModel):
    """
    Authenticator Service içerisindeki kullanıcı
    eşleştirme bilgilerini güncellemek için kullanılır.
    """

    email: EmailStr | None = None

    display_name: str | None = Field(
        default=None,
        max_length=200,
    )

    is_active: bool | None = None

    mfa_enabled: bool | None = None

    @field_validator(
        "display_name",
        mode="before",
    )
    @classmethod
    def normalize_display_name(
        cls,
        value: object,
    ) -> object:
        """
        Görünen ad alanındaki gereksiz boşlukları
        temizler.
        """

        if isinstance(value, str):
            normalized_value = value.strip()

            if normalized_value == "":
                return None

            return normalized_value

        return value


class ExternalUserResponse(BaseModel):
    """
    Authenticator kullanıcısının güvenli
    response modelidir.
    """

    model_config = ConfigDict(
        from_attributes=True,
    )

    public_id: str

    external_user_id: str

    email: EmailStr | None

    display_name: str | None

    is_active: bool

    mfa_enabled: bool

    created_at: datetime

    updated_at: datetime | None


# =========================================================
# CİHAZ KAYIT MODELLERİ
# =========================================================


class DeviceRegistrationRequest(BaseModel):
    """
    Mobil Authenticator uygulamasının cihaz kayıt
    isteğidir.

    Kullanıcı kimliği, e-posta veya ad bilgisi request
    içinden güvenilir kabul edilmez.

    Python servisi backend_access_token değerini mevcut
    .NET backend'in /api/Auth/me endpointine göndererek
    kullanıcıyı doğrular.
    """

    backend_access_token: str = Field(
        min_length=20,
        max_length=10000,
    )

    installation_id: str = Field(
        min_length=16,
        max_length=150,
    )

    platform: DevicePlatform

    device_name: str | None = Field(
        default=None,
        max_length=150,
    )

    device_model: str | None = Field(
        default=None,
        max_length=150,
    )

    manufacturer: str | None = Field(
        default=None,
        max_length=150,
    )

    os_name: str | None = Field(
        default=None,
        max_length=100,
    )

    os_version: str | None = Field(
        default=None,
        max_length=100,
    )

    app_version: str | None = Field(
        default=None,
        max_length=100,
    )

    locale: str | None = Field(
        default=None,
        max_length=50,
    )

    timezone_name: str | None = Field(
        default=None,
        max_length=100,
    )

    # Cihazda oluşturulan private key hiçbir zaman
    # gönderilmez. Yalnızca public key gönderilir.
    public_key: str = Field(
        min_length=100,
        max_length=10000,
    )

    public_key_algorithm: str = Field(
        default="ECDSA_P256_SHA256",
        min_length=1,
        max_length=100,
    )

    # Android Key Attestation veya ileride iOS App Attest
    # verileri için ortak alan.
    key_attestation: str | None = Field(
        default=None,
        max_length=30000,
    )

    push_token: str | None = Field(
        default=None,
        max_length=4000,
    )

    @field_validator(
        "backend_access_token",
        "installation_id",
        "device_name",
        "device_model",
        "manufacturer",
        "os_name",
        "os_version",
        "app_version",
        "locale",
        "timezone_name",
        "public_key",
        "public_key_algorithm",
        "key_attestation",
        "push_token",
        mode="before",
    )
    @classmethod
    def normalize_device_registration_text(
        cls,
        value: object,
    ) -> object:
        """
        Metin alanlarının başındaki ve sonundaki
        boşlukları temizler.
        """

        if not isinstance(value, str):
            return value

        normalized_value = value.strip()

        if normalized_value == "":
            return None

        return normalized_value


class DeviceUpdateRequest(BaseModel):
    """
    Daha önce kaydedilmiş cihazın güncellenebilir
    alanlarını değiştirir.
    """

    device_name: str | None = Field(
        default=None,
        max_length=150,
    )

    device_model: str | None = Field(
        default=None,
        max_length=150,
    )

    manufacturer: str | None = Field(
        default=None,
        max_length=150,
    )

    os_name: str | None = Field(
        default=None,
        max_length=100,
    )

    os_version: str | None = Field(
        default=None,
        max_length=100,
    )

    app_version: str | None = Field(
        default=None,
        max_length=100,
    )

    locale: str | None = Field(
        default=None,
        max_length=50,
    )

    timezone_name: str | None = Field(
        default=None,
        max_length=100,
    )

    public_key: str | None = Field(
        default=None,
        max_length=10000,
    )

    push_token: str | None = Field(
        default=None,
        max_length=4000,
    )

    is_active: bool | None = None


class DeviceHeartbeatRequest(BaseModel):
    """
    Mobil cihazın aktif olduğunu bildiren request
    modelidir.
    """

    installation_id: str = Field(
        min_length=16,
        max_length=150,
    )

    app_version: str | None = Field(
        default=None,
        max_length=100,
    )

    os_version: str | None = Field(
        default=None,
        max_length=100,
    )

    push_token: str | None = Field(
        default=None,
        max_length=4000,
    )


class RegisteredDeviceResponse(BaseModel):
    """
    Kayıtlı Authenticator cihazının güvenli
    response modelidir.
    """

    model_config = ConfigDict(
        from_attributes=True,
    )

    public_id: str

    installation_id: str

    platform: DevicePlatform

    device_name: str | None

    device_model: str | None

    manufacturer: str | None

    os_name: str | None

    os_version: str | None

    app_version: str | None

    locale: str | None

    timezone_name: str | None

    key_algorithm: str | None

    public_key_fingerprint: str | None

    key_created_at: datetime | None

    key_attestation_verified: bool

    is_active: bool

    registered_ip: str | None

    last_ip: str | None

    registered_at: datetime

    last_seen_at: datetime | None

    revoked_at: datetime | None


class DeviceRegistrationResponse(BaseModel):
    """
    Cihaz kaydı tamamlandığında mobil uygulamaya
    döndürülecek modeldir.
    """

    device: RegisteredDeviceResponse

    device_access_token: str

    token_type: Literal["bearer"] = "bearer"

    expires_at: datetime


# =========================================================
# CHALLENGE MODELLERİ
# =========================================================


class CreateChallengeRequest(BaseModel):
    """
    Yeni Authenticator doğrulama isteği oluşturur.

    Kullanıcı kimliği request gövdesinden alınmaz.
    Endpoint'e gönderilen mevcut backend access tokenı
    /api/Auth/me üzerinden doğrulanır.
    """

    method: AuthenticationMethod = (
        AuthenticationMethod.ONE_TIME_CODE
    )

    target_device_public_id: str | None = Field(
        default=None,
        min_length=36,
        max_length=36,
    )

    request_correlation_id: str | None = Field(
        default=None,
        max_length=100,
    )

    request_origin: str | None = Field(
        default="ProjectManagement Web",
        max_length=500,
    )


class CancelChallengeRequest(BaseModel):
    """
    Bekleyen doğrulama isteğini iptal ederken
    gönderilebilecek açıklamadır.
    """

    reason: str | None = Field(
        default=None,
        max_length=500,
    )


class ChallengeResponse(BaseModel):
    """
    Challenge oluşturma ve görüntüleme response
    modelidir.
    """

    public_id: str

    method: AuthenticationMethod

    status: ChallengeStatus

    target_device_public_id: str | None

    target_device_name: str | None

    attempt_count: int

    max_attempts: int

    created_at: datetime

    expires_at: datetime

    completed_at: datetime | None

    delivered_to_device: bool


class CreateChallengeResponse(BaseModel):
    """
    Yeni challenge oluşturma işleminin sonucudur.
    """

    challenge: ChallengeResponse

    expires_in_seconds: int

    polling_interval_seconds: int = 2


class VerifyChallengeCodeRequest(BaseModel):
    """
    Kullanıcının web ekranına yazdığı tek kullanımlık
    kodu doğrulamak için kullanılır.
    """

    code: str = Field(
        min_length=6,
        max_length=12,
        pattern=r"^[0-9]+$",
    )


class ChallengeDecisionRequest(BaseModel):
    """
    Mobil Authenticator cihazının challenge için
    verdiği onay veya ret kararını temsil eder.

    Karar, cihazın private key'iyle imzalanmalıdır.
    Private key hiçbir zaman cihazdan dışarı çıkmaz.
    """

    decision: Literal[
        "approve",
        "reject",
    ]

    installation_id: str = Field(
        min_length=16,
        max_length=150,
    )

    # Challenge imzası Base64 biçiminde gönderilir.
    signature: str = Field(
        min_length=20,
        max_length=10000,
    )

    latitude: float | None = Field(
        default=None,
        ge=-90,
        le=90,
    )

    longitude: float | None = Field(
        default=None,
        ge=-180,
        le=180,
    )

    location_accuracy_meters: float | None = Field(
        default=None,
        ge=0,
        le=100000,
    )

    location_permission_status: Literal[
        "granted_precise",
        "granted_approximate",
        "denied",
        "restricted",
        "not_requested",
        "unavailable",
    ] | None = None

    # Konumun mobil cihaz tarafından alındığı UTC zaman.
    location_captured_at: datetime | None = None


class ChallengeVerificationResponse(BaseModel):
    """
    Kod doğrulama veya mobil cihaz kararı sonucudur.
    """

    challenge_public_id: str

    status: ChallengeStatus

    result: AuthenticationResult

    is_successful: bool

    attempt_count: int

    max_attempts: int

    device_signature_verified: bool

    completed_at: datetime | None

    failure_reason: str | None = None

    risk_score: int

    risk_level: str


class ChallengeStatusResponse(BaseModel):
    """
    Web uygulamasının challenge durumunu sorgularken
    kullanacağı response modelidir.
    """

    challenge_public_id: str

    status: ChallengeStatus

    method: AuthenticationMethod

    is_completed: bool

    is_successful: bool

    attempt_count: int

    max_attempts: int

    expires_at: datetime

    completed_at: datetime | None

    failure_reason: str | None = None


# =========================================================
# WEBSOCKET MESAJ MODELLERİ
# =========================================================


class WebSocketAuthenticateMessage(BaseModel):
    """
    Mobil istemcinin WebSocket bağlantısı açıldığında
    göndereceği ilk kimlik doğrulama mesajıdır.
    """

    type: Literal["authenticate"] = "authenticate"

    installation_id: str = Field(
        min_length=16,
        max_length=150,
    )

    device_access_token: str = Field(
        min_length=20,
        max_length=4000,
    )


class WebSocketHeartbeatMessage(BaseModel):
    """
    WebSocket bağlantısını canlı tutmak için mobil
    istemcinin göndereceği mesajdır.
    """

    type: Literal["heartbeat"] = "heartbeat"

    sent_at: datetime


class WebSocketChallengeMessage(BaseModel):
    """
    Python sunucusunun mobil uygulamaya göndereceği
    doğrulama isteğidir.

    nonce alanı mobil cihazın challenge kararını
    imzalarken kullanılır.
    """

    type: Literal["authentication_challenge"] = (
        "authentication_challenge"
    )

    challenge_public_id: str

    method: AuthenticationMethod

    nonce: str

    external_user_id: str

    display_name: str | None

    email: EmailStr | None

    device_public_id: str

    request_ip: str | None

    request_origin: str | None

    created_at: datetime

    expires_at: datetime

    one_time_code: str | None = None


class WebSocketChallengeCancelledMessage(BaseModel):
    """
    Challenge iptal edildiğinde mobil istemciye
    gönderilecek mesajdır.
    """

    type: Literal["challenge_cancelled"] = (
        "challenge_cancelled"
    )

    challenge_public_id: str

    reason: str | None = None


class WebSocketServerMessage(BaseModel):
    """
    Basit WebSocket bilgi veya hata mesajıdır.
    """

    type: Literal[
        "connected",
        "authenticated",
        "heartbeat_ack",
        "error",
    ]

    message: str

    sent_at: datetime


# =========================================================
# DOĞRULAMA DENEMESİ MODELLERİ
# =========================================================


class AuthenticationAttemptResponse(BaseModel):
    """
    Bir challenge için yapılan doğrulama denemesinin
    response modelidir.
    """

    model_config = ConfigDict(
        from_attributes=True,
    )

    public_id: str

    result: AuthenticationResult

    failure_reason: str | None

    source_ip: str | None

    user_agent: str | None

    latitude: float | None

    longitude: float | None

    location_accuracy_meters: float | None

    location_permission_status: str | None

    location_captured_at: datetime | None

    gps_city: str | None

    gps_district: str | None

    gps_region: str | None

    gps_country: str | None

    gps_country_code: str | None

    ip_city: str | None

    ip_region: str | None

    ip_country: str | None

    ip_country_code: str | None

    location_distance_km: float | None

    location_mismatch: bool

    created_at: datetime


# =========================================================
# ADMIN LOG MODELLERİ
# =========================================================


class AuthenticationLogQuery(BaseModel):
    """
    Admin log listeleme endpointinin filtre modelidir.

    Bu model doğrudan request body olarak değil,
    endpoint query parametrelerinin mantıksal karşılığı
    olarak kullanılabilir.
    """

    page: int = Field(
        default=1,
        ge=1,
    )

    page_size: int = Field(
        default=20,
        ge=1,
        le=100,
    )

    external_user_id: str | None = Field(
        default=None,
        max_length=150,
    )

    email: EmailStr | None = None

    result: AuthenticationResult | None = None

    method: AuthenticationMethod | None = None

    platform: DevicePlatform | None = None

    risk_level: Literal[
        "low",
        "medium",
        "high",
        "critical",
    ] | None = None

    request_ip: str | None = Field(
        default=None,
        max_length=64,
    )

    has_location: bool | None = None

    start_date: datetime | None = None

    end_date: datetime | None = None


class AuthenticationLogResponse(BaseModel):
    """
    Admin panelindeki güvenlik logu satır modelidir.

    Log tablosundaki e-posta geçmiş bir snapshot
    değeridir. İç sistemlerde .local gibi adresler
    bulunabileceği için burada EmailStr yerine normal
    string kullanılır.
    """

    model_config = ConfigDict(
        from_attributes=True,
    )

    public_id: str

    external_user_id_snapshot: str

    email_snapshot: str | None

    display_name_snapshot: str | None

    result: AuthenticationResult

    method: AuthenticationMethod

    platform_snapshot: str | None

    device_name_snapshot: str | None

    device_model_snapshot: str | None

    os_name_snapshot: str | None

    os_version_snapshot: str | None

    request_ip: str | None

    device_ip: str | None

    latitude: float | None

    longitude: float | None

    location_accuracy_meters: float | None

    location_permission_status: str | None

    location_captured_at: datetime | None

    gps_city: str | None

    gps_district: str | None

    gps_region: str | None

    gps_country: str | None

    gps_country_code: str | None

    ip_city: str | None

    ip_region: str | None

    ip_country: str | None

    ip_country_code: str | None

    location_distance_km: float | None

    location_mismatch: bool

    risk_score: int

    risk_level: str

    risk_reasons: str | None

    failure_reason: str | None

    created_at: datetime


class AuthenticationLogListResponse(BaseModel):
    """
    Sayfalı admin güvenlik logu response modelidir.
    """

    items: list[AuthenticationLogResponse]

    pagination: PaginationMetadata


# =========================================================
# MFA DURUM VE YÖNETİM MODELLERİ
# =========================================================


class MfaStatusResponse(BaseModel):
    """
    Bir kullanıcının MFA durumunu göstermek için
    kullanılır.
    """

    external_user_id: str

    mfa_enabled: bool

    active_device_count: int

    has_registered_device: bool


class ChangeMfaStatusRequest(BaseModel):
    """
    Kullanıcının MFA özelliğini açıp kapatmak için
    kullanılır.

    Bu endpoint yalnızca güvenilir servis veya yetkili
    admin tarafından çağrılmalıdır.
    """

    enabled: bool


class RevokeDeviceRequest(BaseModel):
    """
    Cihazı devre dışı bırakırken isteğe bağlı açıklama
    göndermek için kullanılır.
    """

    reason: str | None = Field(
        default=None,
        max_length=500,
    )