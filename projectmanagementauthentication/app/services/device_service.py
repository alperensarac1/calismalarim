from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime

from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from app.models import (
    ExternalUser,
    RegisteredDevice,
    utc_now,
)
from app.schemas import DeviceRegistrationRequest
from app.security import (
    InvalidPublicKeyError,
    calculate_public_key_fingerprint,
    create_device_access_token,
    load_ec_public_key,
)
from app.services.backend_identity_service import (
    BackendIdentityError,
    VerifiedBackendUser,
    backend_identity_service,
)


class DeviceServiceError(Exception):
    """
    Cihaz kayıt ve yönetim işlemlerinde kullanılan
    temel servis hatasıdır.
    """

    pass


class DeviceRegistrationError(DeviceServiceError):
    """
    Mobil cihaz kaydı tamamlanamadığında yükseltilir.
    """

    pass


class DeviceNotFoundError(DeviceServiceError):
    """
    İstenen kayıtlı cihaz bulunamadığında yükseltilir.
    """

    pass


class DeviceOwnershipError(DeviceServiceError):
    """
    Cihaz belirtilen kullanıcıya ait olmadığında
    yükseltilir.
    """

    pass


class InactiveDeviceError(DeviceServiceError):
    """
    Devre dışı bırakılmış cihaz kullanılmaya
    çalışıldığında yükseltilir.
    """

    pass


class UnsupportedKeyAlgorithmError(
    DeviceRegistrationError,
):
    """
    Mobil cihaz desteklenmeyen bir anahtar
    algoritması gönderdiğinde yükseltilir.
    """

    pass


@dataclass(frozen=True, slots=True)
class DeviceRegistrationResult:
    """
    Başarılı cihaz kaydı sonucudur.

    Mobil uygulamaya kayıtlı cihaz bilgisi ve Python
    Authenticator servisine özel access token döndürülür.
    """

    user: ExternalUser

    device: RegisteredDevice

    device_access_token: str

    token_expires_at: datetime

    is_new_user: bool

    is_new_device: bool


@dataclass(frozen=True, slots=True)
class DeviceHeartbeatResult:
    """
    Mobil cihaz heartbeat işleminin sonucudur.
    """

    device: RegisteredDevice

    updated_at: datetime


SUPPORTED_KEY_ALGORITHMS = {
    "ECDSA_P256_SHA256",
}


def normalize_optional_text(
    value: str | None,
) -> str | None:
    """
    Opsiyonel metin alanını temizler.

    Boş değer gelirse None döndürür.
    """

    if value is None:
        return None

    normalized_value = value.strip()

    if not normalized_value:
        return None

    return normalized_value


def normalize_key_algorithm(
    value: str,
) -> str:
    """
    Anahtar algoritması adını standart biçime dönüştürür.
    """

    return value.strip().upper()


def validate_public_key_algorithm(
    algorithm: str,
) -> str:
    """
    Mobil istemcinin gönderdiği public key algoritmasının
    desteklenip desteklenmediğini kontrol eder.
    """

    normalized_algorithm = normalize_key_algorithm(
        algorithm,
    )

    if normalized_algorithm not in (
        SUPPORTED_KEY_ALGORITHMS
    ):
        raise UnsupportedKeyAlgorithmError(
            "Desteklenmeyen public key algoritması: "
            f"{normalized_algorithm}. "
            "Desteklenen algoritma: "
            "ECDSA_P256_SHA256",
        )

    return normalized_algorithm


def get_external_user_by_external_id(
    db: Session,
    external_user_id: str,
) -> ExternalUser | None:
    """
    Ana backend kullanıcı kimliğine göre Python
    Authenticator kullanıcısını getirir.
    """

    statement = select(
        ExternalUser,
    ).where(
        ExternalUser.external_user_id
        == external_user_id,
    )

    return db.scalar(
        statement,
    )


def get_device_by_public_id(
    db: Session,
    device_public_id: str,
) -> RegisteredDevice | None:
    """
    Public UUID değerine göre kayıtlı cihazı getirir.
    """

    statement = select(
        RegisteredDevice,
    ).where(
        RegisteredDevice.public_id
        == device_public_id,
    )

    return db.scalar(
        statement,
    )


def get_device_by_installation(
    db: Session,
    *,
    user_id: int,
    installation_id: str,
) -> RegisteredDevice | None:
    """
    Kullanıcı ve installation_id eşleşmesine göre
    kayıtlı cihazı getirir.
    """

    statement = select(
        RegisteredDevice,
    ).where(
        RegisteredDevice.user_id == user_id,
        RegisteredDevice.installation_id
        == installation_id,
    )

    return db.scalar(
        statement,
    )


def create_or_update_external_user(
    db: Session,
    verified_user: VerifiedBackendUser,
) -> tuple[ExternalUser, bool]:
    """
    Mevcut backend tarafından doğrulanmış kullanıcıyı
    Python Authenticator veritabanında oluşturur
    veya günceller.

    Mobil uygulamanın gönderdiği kullanıcı bilgilerine
    güvenilmez.
    """

    user = get_external_user_by_external_id(
        db,
        verified_user.external_user_id,
    )

    if user is None:
        user = ExternalUser(
            external_user_id=(
                verified_user.external_user_id
            ),
            email=verified_user.email,
            display_name=verified_user.display_name,
            is_active=verified_user.is_active,
            mfa_enabled=True,
        )

        db.add(
            user,
        )

        # Yeni kullanıcının id değerinin oluşturulması
        # için commit yapmadan INSERT çalıştırılır.
        db.flush()

        return user, True

    user.email = verified_user.email
    user.display_name = verified_user.display_name
    user.is_active = verified_user.is_active
    user.updated_at = utc_now()

    return user, False


def create_registered_device(
    *,
    user: ExternalUser,
    request: DeviceRegistrationRequest,
    request_ip: str | None,
    key_algorithm: str,
    public_key_fingerprint: str,
) -> RegisteredDevice:
    """
    Yeni kayıtlı cihaz nesnesini oluşturur.

    Henüz veritabanına commit işlemi yapmaz.
    """

    now = utc_now()

    return RegisteredDevice(
        user_id=user.id,
        installation_id=request.installation_id,
        platform=request.platform,
        device_name=normalize_optional_text(
            request.device_name,
        ),
        device_model=normalize_optional_text(
            request.device_model,
        ),
        manufacturer=normalize_optional_text(
            request.manufacturer,
        ),
        os_name=normalize_optional_text(
            request.os_name,
        ),
        os_version=normalize_optional_text(
            request.os_version,
        ),
        app_version=normalize_optional_text(
            request.app_version,
        ),
        locale=normalize_optional_text(
            request.locale,
        ),
        timezone_name=normalize_optional_text(
            request.timezone_name,
        ),
        public_key=request.public_key.strip(),
        key_algorithm=key_algorithm,
        public_key_fingerprint=(
            public_key_fingerprint
        ),
        key_created_at=now,
        key_attestation=normalize_optional_text(
            request.key_attestation,
        ),
        # İlk sürümde attestation doğrulaması
        # gerçekleştirmiyoruz.
        key_attestation_verified=False,
        push_token=normalize_optional_text(
            request.push_token,
        ),
        is_active=True,
        registered_ip=request_ip,
        last_ip=request_ip,
        registered_at=now,
        last_seen_at=now,
        revoked_at=None,
    )


def update_registered_device(
    device: RegisteredDevice,
    *,
    request: DeviceRegistrationRequest,
    request_ip: str | None,
    key_algorithm: str,
    public_key_fingerprint: str,
) -> None:
    """
    Daha önce kaydedilmiş cihazın güncel bilgilerini
    request üzerinden yeniler.

    Public key değişmişse anahtar oluşturulma zamanı
    da güncellenir.
    """

    now = utc_now()

    public_key_changed = (
        device.public_key_fingerprint
        != public_key_fingerprint
    )

    device.platform = request.platform

    device.device_name = normalize_optional_text(
        request.device_name,
    )

    device.device_model = normalize_optional_text(
        request.device_model,
    )

    device.manufacturer = normalize_optional_text(
        request.manufacturer,
    )

    device.os_name = normalize_optional_text(
        request.os_name,
    )

    device.os_version = normalize_optional_text(
        request.os_version,
    )

    device.app_version = normalize_optional_text(
        request.app_version,
    )

    device.locale = normalize_optional_text(
        request.locale,
    )

    device.timezone_name = normalize_optional_text(
        request.timezone_name,
    )

    device.public_key = request.public_key.strip()

    device.key_algorithm = key_algorithm

    device.public_key_fingerprint = (
        public_key_fingerprint
    )

    device.key_attestation = normalize_optional_text(
        request.key_attestation,
    )

    device.push_token = normalize_optional_text(
        request.push_token,
    )

    device.is_active = True
    device.revoked_at = None
    device.last_ip = request_ip
    device.last_seen_at = now

    if public_key_changed:
        device.key_created_at = now

        # Public key değiştiğinde önceki attestation sonucu
        # artık yeni anahtarı temsil etmez.
        device.key_attestation_verified = False


class DeviceService:
    """
    Platform bağımsız Authenticator cihazlarının
    kayıt ve yönetim işlemlerini yürütür.

    Android ve iOS istemcileri aynı servis akışını kullanır.
    """

    async def register_device(
        self,
        db: Session,
        *,
        request: DeviceRegistrationRequest,
        request_ip: str | None,
    ) -> DeviceRegistrationResult:
        """
        Mobil cihazı Authenticator servisine kaydeder.

        İşlem sırası:

        1. Backend access token doğrulanır.
        2. Public key biçimi doğrulanır.
        3. Python kullanıcısı oluşturulur veya güncellenir.
        4. Cihaz oluşturulur veya güncellenir.
        5. Cihaza özel JWT üretilir.
        """

        try:
            verified_user = await (
                backend_identity_service
                .verify_access_token(
                    request.backend_access_token,
                )
            )

        except BackendIdentityError as exception:
            raise DeviceRegistrationError(
                "Backend kullanıcı doğrulaması "
                f"başarısız: {exception}",
            ) from exception

        if not verified_user.is_active:
            raise DeviceRegistrationError(
                "Aktif olmayan kullanıcı için cihaz "
                "kaydı oluşturulamaz.",
            )

        key_algorithm = (
            validate_public_key_algorithm(
                request.public_key_algorithm,
            )
        )

        try:
            # Public key'in geçerli ECDSA P-256 anahtarı
            # olduğu burada doğrulanır.
            load_ec_public_key(
                request.public_key,
            )

            public_key_fingerprint = (
                calculate_public_key_fingerprint(
                    request.public_key,
                )
            )

        except InvalidPublicKeyError as exception:
            raise DeviceRegistrationError(
                f"Public key doğrulanamadı: {exception}",
            ) from exception

        try:
            user, is_new_user = (
                create_or_update_external_user(
                    db,
                    verified_user,
                )
            )

            existing_device = (
                get_device_by_installation(
                    db,
                    user_id=user.id,
                    installation_id=(
                        request.installation_id
                    ),
                )
            )

            is_new_device = (
                existing_device is None
            )

            if existing_device is None:
                device = create_registered_device(
                    user=user,
                    request=request,
                    request_ip=request_ip,
                    key_algorithm=key_algorithm,
                    public_key_fingerprint=(
                        public_key_fingerprint
                    ),
                )

                db.add(
                    device,
                )

                db.flush()

            else:
                device = existing_device

                update_registered_device(
                    device,
                    request=request,
                    request_ip=request_ip,
                    key_algorithm=key_algorithm,
                    public_key_fingerprint=(
                        public_key_fingerprint
                    ),
                )

                db.flush()

            device_access_token, token_expires_at = (
                create_device_access_token(
                    device_public_id=device.public_id,
                    external_user_id=(
                        user.external_user_id
                    ),
                    installation_id=(
                        device.installation_id
                    ),
                    platform=device.platform.value,
                )
            )

            db.commit()

            db.refresh(
                user,
            )

            db.refresh(
                device,
            )

            return DeviceRegistrationResult(
                user=user,
                device=device,
                device_access_token=(
                    device_access_token
                ),
                token_expires_at=token_expires_at,
                is_new_user=is_new_user,
                is_new_device=is_new_device,
            )

        except IntegrityError as exception:
            db.rollback()

            raise DeviceRegistrationError(
                "Cihaz veya kullanıcı kaydı sırasında "
                "benzersiz alan çakışması oluştu.",
            ) from exception

        except Exception:
            db.rollback()
            raise

    def heartbeat(
        self,
        db: Session,
        *,
        device_public_id: str,
        installation_id: str,
        request_ip: str | None,
        app_version: str | None = None,
        os_version: str | None = None,
        push_token: str | None = None,
    ) -> DeviceHeartbeatResult:
        """
        Mobil cihazın aktif olduğunu bildirir.

        WebSocket bağlantısı dışında periyodik HTTP
        heartbeat endpointinde de kullanılabilir.
        """

        device = get_device_by_public_id(
            db,
            device_public_id,
        )

        if device is None:
            raise DeviceNotFoundError(
                "Kayıtlı cihaz bulunamadı.",
            )

        if (
            device.installation_id
            != installation_id
        ):
            raise DeviceOwnershipError(
                "Cihaz tokenı ile installation_id "
                "eşleşmiyor.",
            )

        if not device.is_active:
            raise InactiveDeviceError(
                "Cihaz devre dışı bırakılmış.",
            )

        now = utc_now()

        device.last_seen_at = now
        device.last_ip = request_ip

        if app_version is not None:
            device.app_version = (
                normalize_optional_text(
                    app_version,
                )
            )

        if os_version is not None:
            device.os_version = (
                normalize_optional_text(
                    os_version,
                )
            )

        if push_token is not None:
            device.push_token = (
                normalize_optional_text(
                    push_token,
                )
            )

        try:
            db.commit()

            db.refresh(
                device,
            )

        except Exception:
            db.rollback()
            raise

        return DeviceHeartbeatResult(
            device=device,
            updated_at=now,
        )

    def revoke_device(
        self,
        db: Session,
        *,
        device_public_id: str,
    ) -> RegisteredDevice:
        """
        Cihazı kalıcı olarak silmeden devre dışı bırakır.

        Güvenlik logları ve geçmiş kayıtlar korunur.
        """

        device = get_device_by_public_id(
            db,
            device_public_id,
        )

        if device is None:
            raise DeviceNotFoundError(
                "Devre dışı bırakılacak cihaz bulunamadı.",
            )

        now = utc_now()

        device.is_active = False
        device.revoked_at = now
        device.last_seen_at = now

        # Bildirim tokenı artık kullanılmamalıdır.
        device.push_token = None

        try:
            db.commit()

            db.refresh(
                device,
            )

        except Exception:
            db.rollback()
            raise

        return device

    def activate_device(
        self,
        db: Session,
        *,
        device_public_id: str,
    ) -> RegisteredDevice:
        """
        Daha önce devre dışı bırakılmış cihazı yeniden
        aktifleştirir.

        Üretim ortamında yeniden aktifleştirme yerine
        public key ile yeniden kayıt tercih edilebilir.
        """

        device = get_device_by_public_id(
            db,
            device_public_id,
        )

        if device is None:
            raise DeviceNotFoundError(
                "Aktifleştirilecek cihaz bulunamadı.",
            )

        device.is_active = True
        device.revoked_at = None
        device.last_seen_at = utc_now()

        try:
            db.commit()

            db.refresh(
                device,
            )

        except Exception:
            db.rollback()
            raise

        return device

    def get_user_devices(
        self,
        db: Session,
        *,
        external_user_id: str,
        include_inactive: bool = False,
    ) -> list[RegisteredDevice]:
        """
        Bir kullanıcının kayıtlı Authenticator
        cihazlarını listeler.
        """

        user = get_external_user_by_external_id(
            db,
            external_user_id,
        )

        if user is None:
            return []

        statement = select(
            RegisteredDevice,
        ).where(
            RegisteredDevice.user_id == user.id,
        )

        if not include_inactive:
            statement = statement.where(
                RegisteredDevice.is_active.is_(
                    True,
                ),
            )

        statement = statement.order_by(
            RegisteredDevice.registered_at.desc(),
        )

        return list(
            db.scalars(
                statement,
            ).all(),
        )


device_service = DeviceService()