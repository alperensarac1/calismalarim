from __future__ import annotations

from dataclasses import dataclass
from datetime import (
    datetime,
    timedelta,
    timezone,
)

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.config import get_settings
from app.models import (
    AuthenticationChallenge,
    AuthenticationMethod,
    ChallengeStatus,
    ExternalUser,
    RegisteredDevice,
    utc_now,
)
from app.security import (
    generate_one_time_code,
    generate_random_token,
    hash_one_time_code,
)
from app.websocket_manager import websocket_manager


settings = get_settings()


# =========================================================
# SERVİS HATALARI
# =========================================================


class ChallengeServiceError(Exception):
    """
    Challenge işlemlerinde kullanılan temel hata sınıfıdır.
    """

    pass


class ChallengeUserNotFoundError(
    ChallengeServiceError,
):
    """
    Authenticator veritabanında kullanıcı bulunamadığında
    yükseltilir.
    """

    pass


class ChallengeDeviceNotFoundError(
    ChallengeServiceError,
):
    """
    Kullanıcıya ait aktif cihaz bulunamadığında yükseltilir.
    """

    pass


class ChallengeNotFoundError(
    ChallengeServiceError,
):
    """
    İstenen challenge kaydı bulunamadığında yükseltilir.
    """

    pass


class ChallengeExpiredError(
    ChallengeServiceError,
):
    """
    Challenge süresi dolduğunda yükseltilir.
    """

    pass


class ChallengeAlreadyCompletedError(
    ChallengeServiceError,
):
    """
    Daha önce tamamlanmış challenge üzerinde yeniden
    işlem yapılmak istendiğinde yükseltilir.
    """

    pass


class ChallengeDeliveryError(
    ChallengeServiceError,
):
    """
    Challenge bağlı cihaza gönderilemediğinde yükseltilir.
    """

    pass


# =========================================================
# SONUÇ MODELLERİ
# =========================================================


@dataclass(frozen=True, slots=True)
class ChallengeCreationResult:
    """
    Yeni challenge oluşturma işleminin sonucudur.

    Demo challenge oluşturulduğunda target_device
    None olabilir.
    """

    challenge: AuthenticationChallenge

    target_device: RegisteredDevice | None

    delivered_to_device: bool

    expires_in_seconds: int


@dataclass(frozen=True, slots=True)
class ChallengeStatusResult:
    """
    Challenge durum sorgulama sonucudur.
    """

    challenge: AuthenticationChallenge

    is_completed: bool

    is_successful: bool

    failure_reason: str | None


# =========================================================
# GENEL YARDIMCI FONKSİYONLAR
# =========================================================


def ensure_utc_datetime(
    value: datetime,
) -> datetime:
    """
    Tarih değerinin UTC ve timezone-aware olmasını sağlar.

    SQLite bazı durumlarda timezone bilgisini geri
    yüklemeyebilir. Bu yardımcı fonksiyon bu farkı giderir.
    """

    if value.tzinfo is None:
        return value.replace(
            tzinfo=timezone.utc,
        )

    return value.astimezone(
        timezone.utc,
    )


def normalize_optional_string(
    value: str | None,
) -> str | None:
    """
    Opsiyonel string değerini temizler.

    None veya boş string gelirse None döndürür.
    """

    if value is None:
        return None

    normalized_value = value.strip()

    return normalized_value or None


# =========================================================
# KULLANICI İŞLEMLERİ
# =========================================================


def get_external_user(
    db: Session,
    external_user_id: str,
) -> ExternalUser | None:
    """
    Ana backend kullanıcı ID değerine göre Authenticator
    kullanıcısını getirir.
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


def create_external_user(
    db: Session,
    *,
    external_user_id: str,
    email: str | None,
    display_name: str | None,
    is_active: bool,
) -> ExternalUser:
    """
    Doğrulanmış .NET kullanıcı bilgisinden yeni
    Authenticator kullanıcı kaydı oluşturur.

    Bu işlem yalnızca AUTO_CREATE_EXTERNAL_USER ayarı
    açık olduğunda çağrılmalıdır.
    """

    user = ExternalUser(
        external_user_id=external_user_id,
        email=normalize_optional_string(
            email,
        ),
        display_name=normalize_optional_string(
            display_name,
        ),
        is_active=is_active,
        mfa_enabled=True,
        created_at=utc_now(),
        updated_at=None,
    )

    try:
        db.add(
            user,
        )

        db.commit()

        db.refresh(
            user,
        )

    except Exception:
        db.rollback()
        raise

    return user


def synchronize_external_user(
    db: Session,
    *,
    user: ExternalUser,
    email: str | None,
    display_name: str | None,
    is_active: bool,
) -> ExternalUser:
    """
    Authenticator kullanıcısının temel bilgilerini
    güncel .NET kullanıcı bilgileriyle eşitler.

    Değişiklik yoksa gereksiz commit yapılmaz.
    """

    normalized_email = normalize_optional_string(
        email,
    )

    normalized_display_name = normalize_optional_string(
        display_name,
    )

    has_changes = False


    if user.email != normalized_email:
        user.email = normalized_email
        has_changes = True


    if user.display_name != normalized_display_name:
        user.display_name = normalized_display_name
        has_changes = True


    if user.is_active != is_active:
        user.is_active = is_active
        has_changes = True


    if not has_changes:
        return user


    user.updated_at = utc_now()

    try:
        db.commit()

        db.refresh(
            user,
        )

    except Exception:
        db.rollback()
        raise

    return user


def resolve_or_create_external_user(
    db: Session,
    *,
    external_user_id: str,
    email: str | None,
    display_name: str | None,
    is_active: bool,
) -> ExternalUser:
    """
    Authenticator kullanıcısını bulur.

    Kullanıcı yoksa ve otomatik oluşturma açıksa yeni
    ExternalUser kaydı oluşturur.

    Kullanıcı zaten varsa temel profil bilgilerini
    güncel .NET cevabıyla eşitler.
    """

    normalized_external_user_id = (
        external_user_id.strip()
    )

    if not normalized_external_user_id:
        raise ChallengeServiceError(
            "Backend kullanıcı kimliği boş olamaz.",
        )


    user = get_external_user(
        db,
        normalized_external_user_id,
    )


    if user is None:
        if not settings.auto_create_external_user:
            raise ChallengeUserNotFoundError(
                "Kullanıcı Authenticator servisine "
                "henüz kayıtlı değil.",
            )

        user = create_external_user(
            db,
            external_user_id=(
                normalized_external_user_id
            ),
            email=email,
            display_name=display_name,
            is_active=is_active,
        )

        return user


    return synchronize_external_user(
        db,
        user=user,
        email=email,
        display_name=display_name,
        is_active=is_active,
    )


# =========================================================
# CHALLENGE SORGULARI
# =========================================================


def get_challenge_by_public_id(
    db: Session,
    challenge_public_id: str,
) -> AuthenticationChallenge | None:
    """
    Challenge public UUID değerine göre kayıt getirir.
    """

    statement = select(
        AuthenticationChallenge,
    ).where(
        AuthenticationChallenge.public_id
        == challenge_public_id,
    )

    return db.scalar(
        statement,
    )


# =========================================================
# CİHAZ SORGULARI
# =========================================================


def get_device_by_public_id(
    db: Session,
    device_public_id: str,
) -> RegisteredDevice | None:
    """
    Public UUID değerine göre aktif cihaz getirir.
    """

    statement = select(
        RegisteredDevice,
    ).where(
        RegisteredDevice.public_id
        == device_public_id,
        RegisteredDevice.is_active.is_(
            True,
        ),
    )

    return db.scalar(
        statement,
    )


def get_default_active_device(
    db: Session,
    user_id: int,
) -> RegisteredDevice | None:
    """
    Kullanıcının en son görülen aktif cihazını seçer.

    Birden fazla cihaz varsa önce en son görülen,
    eşitlik durumunda en son kaydedilen cihaz seçilir.
    """

    statement = (
        select(
            RegisteredDevice,
        )
        .where(
            RegisteredDevice.user_id == user_id,
            RegisteredDevice.is_active.is_(
                True,
            ),
        )
        .order_by(
            RegisteredDevice.last_seen_at.desc(),
            RegisteredDevice.registered_at.desc(),
        )
        .limit(1)
    )

    return db.scalar(
        statement,
    )


def resolve_target_device(
    db: Session,
    *,
    user: ExternalUser,
    target_device_public_id: str | None,
) -> RegisteredDevice | None:
    """
    Challenge'ın gönderileceği cihazı belirler.

    Belirli bir cihaz public ID değeri gönderilmişse:

    - Cihaz bulunmalıdır.
    - Aktif olmalıdır.
    - Kullanıcıya ait olmalıdır.

    Belirli cihaz gönderilmemişse kullanıcının varsayılan
    aktif cihazı aranır.

    Cihaz bulunamazsa:

    - Demo modu açıksa None döndürülür.
    - Demo modu kapalıysa hata yükseltilir.
    """

    normalized_device_public_id = (
        normalize_optional_string(
            target_device_public_id,
        )
    )


    if normalized_device_public_id:
        device = get_device_by_public_id(
            db,
            normalized_device_public_id,
        )

        if device is None:
            raise ChallengeDeviceNotFoundError(
                "Belirtilen Authenticator cihazı "
                "bulunamadı veya aktif değil.",
            )

        if device.user_id != user.id:
            raise ChallengeDeviceNotFoundError(
                "Belirtilen cihaz kullanıcıya ait değil.",
            )

        return device


    device = get_default_active_device(
        db,
        user.id,
    )


    if device is not None:
        return device


    if settings.allow_challenge_without_device:
        return None


    raise ChallengeDeviceNotFoundError(
        "Kullanıcıya ait aktif Authenticator "
        "cihazı bulunamadı.",
    )


# =========================================================
# CHALLENGE DURUM YARDIMCILARI
# =========================================================


def is_terminal_status(
    status: ChallengeStatus,
) -> bool:
    """
    Challenge durumunun tamamlanmış bir durum olup
    olmadığını belirtir.
    """

    return status in {
        ChallengeStatus.APPROVED,
        ChallengeStatus.REJECTED,
        ChallengeStatus.EXPIRED,
        ChallengeStatus.LOCKED,
        ChallengeStatus.CANCELLED,
    }


def mark_challenge_expired_if_needed(
    db: Session,
    challenge: AuthenticationChallenge,
) -> bool:
    """
    Challenge süresi geçtiyse durumunu expired yapar.

    Güncelleme yapıldıysa True döndürür.
    """

    if challenge.status != ChallengeStatus.PENDING:
        return False


    expires_at = ensure_utc_datetime(
        challenge.expires_at,
    )


    if utc_now() < expires_at:
        return False


    challenge.status = ChallengeStatus.EXPIRED
    challenge.completed_at = utc_now()


    try:
        db.commit()

        db.refresh(
            challenge,
        )

    except Exception:
        db.rollback()
        raise


    return True


# =========================================================
# WEBSOCKET MESAJI
# =========================================================


def build_websocket_challenge_message(
    *,
    challenge: AuthenticationChallenge,
    user: ExternalUser,
    device: RegisteredDevice,
    one_time_code: str,
) -> dict[str, object]:
    """
    Mobil uygulamaya gönderilecek WebSocket mesajını
    oluşturur.

    Tek kullanımlık kod yalnızca cihaz mesajında bulunur.
    SQLite veritabanında düz metin olarak saklanmaz.
    """

    return {
        "type":
            "authentication_challenge",

        "challenge_public_id":
            challenge.public_id,

        "method":
            challenge.method.value,

        "nonce":
            challenge.nonce,

        "external_user_id":
            user.external_user_id,

        "display_name":
            user.display_name,

        "email":
            user.email,

        "device_public_id":
            device.public_id,

        "request_ip":
            challenge.request_ip,

        "request_origin":
            challenge.request_origin,

        "expires_at":
            ensure_utc_datetime(
                challenge.expires_at,
            ).isoformat(),

        "created_at":
            ensure_utc_datetime(
                challenge.created_at,
            ).isoformat(),

        "one_time_code":
            one_time_code,
    }


# =========================================================
# CHALLENGE SERVİSİ
# =========================================================


class ChallengeService:
    """
    Authenticator doğrulama isteklerinin oluşturulması,
    cihaza gönderilmesi ve durumlarının yönetilmesinden
    sorumludur.
    """

    async def create_challenge(
        self,
        db: Session,
        *,
        external_user_id: str,
        email: str | None,
        display_name: str | None,
        is_active: bool,
        method: AuthenticationMethod,
        target_device_public_id: str | None,
        request_ip: str | None,
        forwarded_ip: str | None,
        user_agent: str | None,
        request_origin: str | None,
        request_correlation_id: str | None,
    ) -> ChallengeCreationResult:
        """
        Yeni bir doğrulama challenge'ı oluşturur.

        Normal akış:

        1. Kullanıcı Authenticator veritabanında bulunur.
        2. Aktif mobil cihaz seçilir.
        3. Challenge veritabanına kaydedilir.
        4. WebSocket üzerinden mobil cihaza gönderilir.

        Demo akışı:

        1. Kullanıcı yoksa otomatik oluşturulur.
        2. Mobil cihaz yoksa target_device_id None olur.
        3. Challenge yine pending olarak kaydedilir.
        4. React kod giriş ekranını gösterir.
        5. 987456 test kodu Python servisinde doğrulanır.
        """

        # -------------------------------------------------
        # 1. KULLANICIYI BUL VEYA OLUŞTUR
        # -------------------------------------------------

        user = resolve_or_create_external_user(
            db,
            external_user_id=external_user_id,
            email=email,
            display_name=display_name,
            is_active=is_active,
        )


        if not user.is_active:
            raise ChallengeServiceError(
                "Authenticator kullanıcısı aktif değil.",
            )


        if not user.mfa_enabled:
            raise ChallengeServiceError(
                "Kullanıcı için MFA devre dışı.",
            )


        # -------------------------------------------------
        # 2. DOĞRULAMA YÖNTEMİNİ KONTROL ET
        # -------------------------------------------------

        if method not in {
            AuthenticationMethod.ONE_TIME_CODE,
            AuthenticationMethod.MOBILE_APPROVAL,
            AuthenticationMethod.DEVICE_SIGNATURE,
        }:
            raise ChallengeServiceError(
                "Desteklenmeyen doğrulama yöntemi.",
            )


        # -------------------------------------------------
        # 3. HEDEF CİHAZI BELİRLE
        # -------------------------------------------------

        target_device = resolve_target_device(
            db,
            user=user,
            target_device_public_id=(
                target_device_public_id
            ),
        )

        if (
            target_device is not None
            and not target_device.public_key
        ):
            if settings.allow_challenge_without_device:
                target_device = None
            else:
                raise ChallengeServiceError(
                    "Hedef cihazın public key bilgisi "
                    "bulunmuyor.",
                )


        # -------------------------------------------------
        # 4. KOD VE NONCE ÜRET
        # -------------------------------------------------

        one_time_code = generate_one_time_code(
            length=6,
        )


        code_hash = hash_one_time_code(
            one_time_code,
        )


        nonce = generate_random_token(
            byte_length=32,
        )


        now = utc_now()


        expires_at = now + timedelta(
            seconds=(
                settings.challenge_expire_seconds
            ),
        )


        # -------------------------------------------------
        # 5. CHALLENGE KAYDI OLUŞTUR
        # -------------------------------------------------

        challenge = AuthenticationChallenge(
            user_id=user.id,


            target_device_id=(
                target_device.id
                if target_device is not None
                else None
            ),

            method=method,

            status=ChallengeStatus.PENDING,


            code_hash=code_hash,

            nonce=nonce,

            device_signature_verified=False,

            attempt_count=0,

            max_attempts=(
                settings.challenge_max_attempts
            ),

            request_ip=request_ip,

            forwarded_ip=forwarded_ip,

            user_agent=user_agent,

            request_origin=request_origin,

            request_correlation_id=(
                request_correlation_id
            ),

            created_at=now,

            expires_at=expires_at,

            approved_at=None,

            rejected_at=None,

            completed_at=None,
        )


        try:
            db.add(
                challenge,
            )

            db.commit()

            db.refresh(
                challenge,
            )

        except Exception:
            db.rollback()
            raise


        # -------------------------------------------------
        # 6. CİHAZSIZ DEMO CHALLENGE
        # -------------------------------------------------

        if target_device is None:
            return ChallengeCreationResult(
                challenge=challenge,

                target_device=None,

                delivered_to_device=False,

                expires_in_seconds=(
                    settings.challenge_expire_seconds
                ),
            )


        # -------------------------------------------------
        # 7. MOBİL CİHAZA WEBSOCKET MESAJI GÖNDER
        # -------------------------------------------------

        websocket_message = (
            build_websocket_challenge_message(
                challenge=challenge,
                user=user,
                device=target_device,
                one_time_code=one_time_code,
            )
        )


        delivered_to_device = (
            await websocket_manager
            .send_json_to_device(
                target_device.public_id,
                websocket_message,
            )
        )


        # -------------------------------------------------
        # 8. WEBSOCKET TESLİM SONUCU
        # -------------------------------------------------

        if not delivered_to_device:

            if settings.allow_challenge_without_device:
                return ChallengeCreationResult(
                    challenge=challenge,

                    target_device=target_device,

                    delivered_to_device=False,

                    expires_in_seconds=(
                        settings.challenge_expire_seconds
                    ),
                )



            raise ChallengeDeliveryError(
                "Authenticator cihazına WebSocket "
                "üzerinden ulaşılamadı. Mobil "
                "uygulamanın açık ve bağlı olduğundan "
                "emin olun.",
            )


        return ChallengeCreationResult(
            challenge=challenge,

            target_device=target_device,

            delivered_to_device=True,

            expires_in_seconds=(
                settings.challenge_expire_seconds
            ),
        )


    def get_challenge_status(
        self,
        db: Session,
        *,
        challenge_public_id: str,
    ) -> ChallengeStatusResult:
        """
        Challenge'ın güncel durumunu döndürür.

        Pending bir challenge'ın süresi geçmişse önce
        expired durumuna geçirilir.
        """

        challenge = get_challenge_by_public_id(
            db,
            challenge_public_id,
        )


        if challenge is None:
            raise ChallengeNotFoundError(
                "Doğrulama isteği bulunamadı.",
            )


        mark_challenge_expired_if_needed(
            db,
            challenge,
        )


        is_completed = is_terminal_status(
            challenge.status,
        )


        is_successful = (
            challenge.status
            == ChallengeStatus.APPROVED
        )


        failure_reason: str | None = None


        if (
            challenge.status
            == ChallengeStatus.REJECTED
        ):
            failure_reason = (
                "Doğrulama isteği kullanıcı "
                "tarafından reddedildi."
            )

        elif (
            challenge.status
            == ChallengeStatus.EXPIRED
        ):
            failure_reason = (
                "Doğrulama isteğinin süresi doldu."
            )

        elif (
            challenge.status
            == ChallengeStatus.LOCKED
        ):
            failure_reason = (
                "Maksimum doğrulama denemesi aşıldı."
            )

        elif (
            challenge.status
            == ChallengeStatus.CANCELLED
        ):
            failure_reason = (
                "Doğrulama isteği iptal edildi."
            )


        return ChallengeStatusResult(
            challenge=challenge,

            is_completed=is_completed,

            is_successful=is_successful,

            failure_reason=failure_reason,
        )


    async def cancel_challenge(
        self,
        db: Session,
        *,
        challenge_public_id: str,
        reason: str | None = None,
    ) -> AuthenticationChallenge:
        """
        Bekleyen challenge'ı iptal eder.

        Challenge'ın bağlı bir cihazı varsa iptal bilgisi
        WebSocket üzerinden cihaza da gönderilir.

        Demo challenge'da target_device None olabilir.
        """

        challenge = get_challenge_by_public_id(
            db,
            challenge_public_id,
        )


        if challenge is None:
            raise ChallengeNotFoundError(
                "İptal edilecek doğrulama isteği "
                "bulunamadı.",
            )


        mark_challenge_expired_if_needed(
            db,
            challenge,
        )


        if is_terminal_status(
            challenge.status,
        ):
            raise ChallengeAlreadyCompletedError(
                "Tamamlanmış doğrulama isteği "
                "iptal edilemez.",
            )


        challenge.status = (
            ChallengeStatus.CANCELLED
        )

        challenge.completed_at = utc_now()


        try:
            db.commit()

            db.refresh(
                challenge,
            )

        except Exception:
            db.rollback()
            raise



        if challenge.target_device is not None:
            await websocket_manager.send_json_to_device(
                challenge.target_device.public_id,
                {
                    "type":
                        "challenge_cancelled",

                    "challenge_public_id":
                        challenge.public_id,

                    "reason":
                        reason,

                    "sent_at":
                        utc_now().isoformat(),
                },
            )


        return challenge


challenge_service = ChallengeService()