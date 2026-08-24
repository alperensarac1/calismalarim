from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timezone

from sqlalchemy.orm import Session

from app.config import get_settings
from app.models import (
    AuthenticationAttempt,
    AuthenticationChallenge,
    AuthenticationLog,
    AuthenticationResult,
    ChallengeStatus,
    RegisteredDevice,
    utc_now,
)
from app.security import (
    InvalidDeviceSignatureError,
    build_challenge_signing_payload,
    verify_device_signature,
    verify_one_time_code,
)
from app.services.challenge_service import (
    ChallengeAlreadyCompletedError,
    ChallengeExpiredError,
    ChallengeNotFoundError,
    get_challenge_by_public_id,
    is_terminal_status,
    mark_challenge_expired_if_needed,
)
from app.services.location_service import (
    LocationServiceError,
    ResolvedLocation,
    location_service,
)


settings = get_settings()


# =========================================================
# SERVİS HATALARI
# =========================================================


class ChallengeVerificationError(Exception):
    """
    Challenge doğrulama işlemlerinde kullanılan
    temel hata sınıfıdır.
    """

    pass


class InvalidChallengeCodeError(
    ChallengeVerificationError,
):
    """
    Kullanıcı yanlış doğrulama kodu girdiğinde
    yükseltilir.
    """

    pass


class ChallengeLockedError(
    ChallengeVerificationError,
):
    """
    Maksimum yanlış kod denemesi aşıldığında yükseltilir.
    """

    pass


class ChallengeDeviceMismatchError(
    ChallengeVerificationError,
):
    """
    Challenge başka bir cihaza ait olduğunda yükseltilir.
    """

    pass


class MissingDevicePublicKeyError(
    ChallengeVerificationError,
):
    """
    Kayıtlı cihazın public key bilgisi bulunmadığında
    yükseltilir.
    """

    pass


class InvalidChallengeDecisionError(
    ChallengeVerificationError,
):
    """
    Mobil istemciden geçersiz karar gönderildiğinde
    yükseltilir.
    """

    pass


# =========================================================
# SONUÇ MODELLERİ
# =========================================================


@dataclass(frozen=True, slots=True)
class ChallengeVerificationResult:
    """
    Kod doğrulaması veya mobil cihaz kararı sonucudur.
    """

    challenge: AuthenticationChallenge

    attempt: AuthenticationAttempt

    authentication_log: AuthenticationLog

    is_successful: bool


# =========================================================
# TARİH YARDIMCILARI
# =========================================================


def ensure_utc_datetime(
    value: datetime,
) -> datetime:
    """
    SQLite üzerinden timezone bilgisi olmadan dönebilen
    tarihleri UTC timezone-aware hâle getirir.
    """

    if value.tzinfo is None:
        return value.replace(
            tzinfo=timezone.utc,
        )

    return value.astimezone(
        timezone.utc,
    )


# =========================================================
# CHALLENGE KONTROLLERİ
# =========================================================


def get_pending_challenge(
    db: Session,
    *,
    challenge_public_id: str,
) -> AuthenticationChallenge:
    """
    Challenge kaydını getirir ve işlem yapılabilir
    durumda olduğunu doğrular.
    """

    challenge = get_challenge_by_public_id(
        db,
        challenge_public_id,
    )

    if challenge is None:
        raise ChallengeNotFoundError(
            "Doğrulama isteği bulunamadı.",
        )

    expired = mark_challenge_expired_if_needed(
        db,
        challenge,
    )

    if expired:
        raise ChallengeExpiredError(
            "Doğrulama isteğinin süresi dolmuş.",
        )

    if challenge.status == ChallengeStatus.LOCKED:
        raise ChallengeLockedError(
            "Doğrulama isteği maksimum deneme "
            "sayısı aşıldığı için kilitlendi.",
        )

    if is_terminal_status(
        challenge.status,
    ):
        raise ChallengeAlreadyCompletedError(
            "Doğrulama isteği daha önce tamamlanmış.",
        )

    if challenge.status != ChallengeStatus.PENDING:
        raise ChallengeVerificationError(
            "Doğrulama isteği işlem yapılabilir "
            "durumda değil.",
        )

    return challenge


def ensure_challenge_owner(
    challenge: AuthenticationChallenge,
    *,
    external_user_id: str,
) -> None:
    """
    Kod doğrulaması yapan backend kullanıcısının
    challenge sahibi olduğunu kontrol eder.
    """

    if (
        challenge.user.external_user_id
        != external_user_id
    ):
        raise ChallengeVerificationError(
            "Doğrulama isteği bu kullanıcıya ait değil.",
        )


def ensure_challenge_device(
    challenge: AuthenticationChallenge,
    *,
    device: RegisteredDevice,
) -> None:
    """
    Mobil kararı gönderen cihazın challenge hedefi
    olduğunu doğrular.
    """

    if challenge.target_device_id is None:
        raise ChallengeDeviceMismatchError(
            "Doğrulama isteğine hedef cihaz atanmamış.",
        )

    if challenge.target_device_id != device.id:
        raise ChallengeDeviceMismatchError(
            "Bu doğrulama isteği farklı bir "
            "Authenticator cihazına ait.",
        )

    if challenge.user_id != device.user_id:
        raise ChallengeDeviceMismatchError(
            "Cihaz kullanıcısı ile challenge "
            "kullanıcısı eşleşmiyor.",
        )

    if not device.is_active:
        raise ChallengeDeviceMismatchError(
            "Authenticator cihazı aktif değil.",
        )


# =========================================================
# KONUM YARDIMCILARI
# =========================================================


async def resolve_device_location(
    *,
    latitude: float | None,
    longitude: float | None,
) -> ResolvedLocation | None:
    """
    Mobil cihazdan gelen koordinatları şehir, ilçe,
    bölge ve ülke bilgisine dönüştürür.

    Konum servisi kullanılamazsa doğrulama işlemi
    engellenmez ve None döndürülür.
    """

    if (
        latitude is None
        or longitude is None
    ):
        return None

    try:
        return await location_service.resolve_coordinates(
            latitude=latitude,
            longitude=longitude,
        )

    except LocationServiceError:
        # Reverse geocoding işlemi yardımcı bir güvenlik
        # bilgisidir. Dış servis hatası nedeniyle giriş
        # doğrulamasını başarısız yapmıyoruz.
        return None


def get_resolved_location_values(
    resolved_location: ResolvedLocation | None,
) -> tuple[
    str | None,
    str | None,
    str | None,
    str | None,
    str | None,
]:
    """
    ResolvedLocation nesnesinden veritabanında
    saklanacak GPS konum alanlarını çıkarır.
    """

    if resolved_location is None:
        return (
            None,
            None,
            None,
            None,
            None,
        )

    return (
        resolved_location.city,
        resolved_location.district,
        resolved_location.region,
        resolved_location.country,
        resolved_location.country_code,
    )


# =========================================================
# ATTEMPT VE LOG OLUŞTURMA
# =========================================================


def create_authentication_attempt(
    *,
    challenge: AuthenticationChallenge,
    device: RegisteredDevice | None,
    result: AuthenticationResult,
    failure_reason: str | None,
    source_ip: str | None,
    user_agent: str | None,
    latitude: float | None,
    longitude: float | None,
    location_accuracy_meters: float | None,
    location_permission_status: str | None,
    location_captured_at: datetime | None,
    signature: str | None,
    gps_city: str | None,
    gps_district: str | None,
    gps_region: str | None,
    gps_country: str | None,
    gps_country_code: str | None,
) -> AuthenticationAttempt:
    """
    Her kod denemesini veya cihaz kararını ayrı bir
    authentication_attempts kaydı olarak oluşturur.
    """

    return AuthenticationAttempt(
        challenge_id=challenge.id,
        device_id=(
            device.id
            if device is not None
            else None
        ),
        result=result,
        failure_reason=failure_reason,
        source_ip=source_ip,
        user_agent=user_agent,
        latitude=latitude,
        longitude=longitude,
        location_accuracy_meters=(
            location_accuracy_meters
        ),
        location_permission_status=(
            location_permission_status
        ),
        location_captured_at=(
            location_captured_at
        ),
        gps_city=gps_city,
        gps_district=gps_district,
        gps_region=gps_region,
        gps_country=gps_country,
        gps_country_code=gps_country_code,
        signature=signature,
        created_at=utc_now(),
    )


def create_authentication_log(
    *,
    challenge: AuthenticationChallenge,
    device: RegisteredDevice | None,
    result: AuthenticationResult,
    failure_reason: str | None,
    request_ip: str | None,
    device_ip: str | None,
    user_agent: str | None,
    latitude: float | None,
    longitude: float | None,
    location_accuracy_meters: float | None,
    location_permission_status: str | None,
    location_captured_at: datetime | None,
    gps_city: str | None,
    gps_district: str | None,
    gps_region: str | None,
    gps_country: str | None,
    gps_country_code: str | None,
    risk_score: int,
    risk_level: str,
    risk_reasons: str | None,
) -> AuthenticationLog:
    """
    Admin ekranında gösterilecek özet güvenlik logunu
    oluşturur.

    Kullanıcı ve cihaz bilgileri snapshot olarak tutulur.
    Daha sonra kullanıcı veya cihaz bilgileri değişse bile
    geçmiş log aynı kalır.
    """

    user = challenge.user

    return AuthenticationLog(
        user_id=user.id,
        challenge_id=challenge.id,
        device_id=(
            device.id
            if device is not None
            else None
        ),
        result=result,
        method=challenge.method,
        external_user_id_snapshot=(
            user.external_user_id
        ),
        email_snapshot=user.email,
        display_name_snapshot=user.display_name,
        platform_snapshot=(
            device.platform.value
            if device is not None
            else None
        ),
        device_name_snapshot=(
            device.device_name
            if device is not None
            else None
        ),
        device_model_snapshot=(
            device.device_model
            if device is not None
            else None
        ),
        os_name_snapshot=(
            device.os_name
            if device is not None
            else None
        ),
        os_version_snapshot=(
            device.os_version
            if device is not None
            else None
        ),
        request_ip=request_ip,
        device_ip=device_ip,
        user_agent=user_agent,
        latitude=latitude,
        longitude=longitude,
        location_accuracy_meters=(
            location_accuracy_meters
        ),
        location_permission_status=(
            location_permission_status
        ),
        location_captured_at=(
            location_captured_at
        ),
        gps_city=gps_city,
        gps_district=gps_district,
        gps_region=gps_region,
        gps_country=gps_country,
        gps_country_code=gps_country_code,
        risk_score=risk_score,
        risk_level=risk_level,
        risk_reasons=risk_reasons,
        failure_reason=failure_reason,
        created_at=utc_now(),
    )


# =========================================================
# RİSK DEĞERLENDİRMESİ
# =========================================================


def calculate_basic_risk(
    *,
    result: AuthenticationResult,
    latitude: float | None,
    longitude: float | None,
    location_permission_status: str | None,
    device_signature_verified: bool,
) -> tuple[
    int,
    str,
    str | None,
]:
    """
    İlk sürüm için basit risk skoru hesaplar.
    """

    score = 0
    reasons: list[str] = []

    if result != AuthenticationResult.SUCCESS:
        score += 30
        reasons.append(
            "Doğrulama işlemi başarılı olmadı.",
        )

    if not device_signature_verified:
        score += 20
        reasons.append(
            "Cihaz imzası doğrulanmadı.",
        )

    if (
        latitude is None
        or longitude is None
    ):
        score += 5
        reasons.append(
            "Konum bilgisi alınamadı.",
        )

    if location_permission_status in {
        "denied",
        "restricted",
        "unavailable",
    }:
        score += 5
        reasons.append(
            "Konum izni verilmedi veya kullanılamıyor.",
        )

    if score >= 70:
        level = "critical"
    elif score >= 45:
        level = "high"
    elif score >= 20:
        level = "medium"
    else:
        level = "low"

    return (
        score,
        level,
        (
            "\n".join(
                reasons,
            )
            if reasons
            else None
        ),
    )


# =========================================================
# SERVİS
# =========================================================


class ChallengeVerificationService:
    """
    Challenge kod doğrulama ve mobil cihaz kararı
    işlemlerini yönetir.
    """

    def verify_code(
        self,
        db: Session,
        *,
        challenge_public_id: str,
        external_user_id: str,
        code: str,
        source_ip: str | None,
        user_agent: str | None,
    ) -> ChallengeVerificationResult:
        """
        Telefonda gösterilen tek kullanımlık kodu veya
        hobi amaçlı sabit test kodunu doğrular.
        """

        challenge = get_pending_challenge(
            db,
            challenge_public_id=(
                challenge_public_id
            ),
        )

        ensure_challenge_owner(
            challenge,
            external_user_id=external_user_id,
        )

        normalized_code = code.strip()

        # .env veya config.py içindeki sabit test kodu.
        #
        # Örneğin:
        #
        # TEST_CHALLENGE_CODE=987456
        test_code_is_valid = (
            normalized_code
            == settings.test_challenge_code
        )

        # Test kodu girilmemişse normal challenge
        # kodunun hash değeri bulunmalıdır.
        if (
            not test_code_is_valid
            and not challenge.code_hash
        ):
            raise ChallengeVerificationError(
                "Challenge için doğrulama kodu bulunamadı.",
            )

        normal_code_is_valid = False

        if challenge.code_hash:
            normal_code_is_valid = (
                verify_one_time_code(
                    normalized_code,
                    challenge.code_hash,
                )
            )

        # Gerçek tek kullanımlık kod veya sabit test
        # kodundan biri doğruysa işlem başarılıdır.
        code_is_valid = (
            normal_code_is_valid
            or test_code_is_valid
        )

        challenge.attempt_count += 1

        target_device = challenge.target_device

        if code_is_valid:
            now = utc_now()

            challenge.status = (
                ChallengeStatus.APPROVED
            )

            challenge.approved_at = now
            challenge.completed_at = now

            result = AuthenticationResult.SUCCESS
            failure_reason = None

        else:
            result = AuthenticationResult.FAILED

            failure_reason = (
                "Tek kullanımlık doğrulama kodu yanlış."
            )

            if (
                challenge.attempt_count
                >= challenge.max_attempts
            ):
                challenge.status = (
                    ChallengeStatus.LOCKED
                )

                challenge.completed_at = utc_now()

        risk_score, risk_level, risk_reasons = (
            calculate_basic_risk(
                result=result,
                latitude=None,
                longitude=None,
                location_permission_status=None,
                device_signature_verified=False,
            )
        )

        attempt = create_authentication_attempt(
            challenge=challenge,
            device=target_device,
            result=result,
            failure_reason=failure_reason,
            source_ip=source_ip,
            user_agent=user_agent,
            latitude=None,
            longitude=None,
            location_accuracy_meters=None,
            location_permission_status=None,
            location_captured_at=None,
            signature=None,
            gps_city=None,
            gps_district=None,
            gps_region=None,
            gps_country=None,
            gps_country_code=None,
        )

        authentication_log = (
            create_authentication_log(
                challenge=challenge,
                device=target_device,
                result=result,
                failure_reason=failure_reason,
                request_ip=challenge.request_ip,
                device_ip=source_ip,
                user_agent=user_agent,
                latitude=None,
                longitude=None,
                location_accuracy_meters=None,
                location_permission_status=None,
                location_captured_at=None,
                gps_city=None,
                gps_district=None,
                gps_region=None,
                gps_country=None,
                gps_country_code=None,
                risk_score=risk_score,
                risk_level=risk_level,
                risk_reasons=risk_reasons,
            )
        )

        try:
            db.add(
                attempt,
            )

            db.add(
                authentication_log,
            )

            db.commit()

            db.refresh(
                challenge,
            )

            db.refresh(
                attempt,
            )

            db.refresh(
                authentication_log,
            )

        except Exception:
            db.rollback()
            raise

        if not code_is_valid:
            if (
                challenge.status
                == ChallengeStatus.LOCKED
            ):
                raise ChallengeLockedError(
                    "Maksimum yanlış kod denemesi "
                    "aşıldı. Challenge kilitlendi.",
                )

            raise InvalidChallengeCodeError(
                "Doğrulama kodu yanlış.",
            )

        return ChallengeVerificationResult(
            challenge=challenge,
            attempt=attempt,
            authentication_log=(
                authentication_log
            ),
            is_successful=True,
        )

    async def process_device_decision(
        self,
        db: Session,
        *,
        challenge_public_id: str,
        device: RegisteredDevice,
        decision: str,
        signature: str,
        source_ip: str | None,
        user_agent: str | None,
        latitude: float | None,
        longitude: float | None,
        location_accuracy_meters: float | None,
        location_permission_status: str | None,
        location_captured_at: datetime | None,
    ) -> ChallengeVerificationResult:
        """
        Mobil Authenticator uygulamasından gelen
        onay veya ret kararını işler.

        Karar kayıtlı cihazın private key'iyle
        imzalanmış olmalıdır.
        """

        normalized_decision = (
            decision.strip().lower()
        )

        if normalized_decision not in {
            "approve",
            "reject",
        }:
            raise InvalidChallengeDecisionError(
                "Karar approve veya reject olmalıdır.",
            )

        challenge = get_pending_challenge(
            db,
            challenge_public_id=(
                challenge_public_id
            ),
        )

        ensure_challenge_device(
            challenge,
            device=device,
        )

        resolved_location = await resolve_device_location(
            latitude=latitude,
            longitude=longitude,
        )

        (
            gps_city,
            gps_district,
            gps_region,
            gps_country,
            gps_country_code,
        ) = get_resolved_location_values(
            resolved_location,
        )

        if not device.public_key:
            raise MissingDevicePublicKeyError(
                "Kayıtlı cihazın public key "
                "bilgisi bulunamadı.",
            )

        signing_payload = (
            build_challenge_signing_payload(
                challenge_public_id=(
                    challenge.public_id
                ),
                nonce=challenge.nonce,
                external_user_id=(
                    challenge.user.external_user_id
                ),
                installation_id=(
                    device.installation_id
                ),
                decision=normalized_decision,
                expires_at=ensure_utc_datetime(
                    challenge.expires_at,
                ),
            )
        )

        try:
            verify_device_signature(
                public_key_pem=device.public_key,
                payload=signing_payload,
                signature_base64=signature,
            )

        except InvalidDeviceSignatureError as exception:
            result = AuthenticationResult.FAILED

            risk_score, risk_level, risk_reasons = (
                calculate_basic_risk(
                    result=result,
                    latitude=latitude,
                    longitude=longitude,
                    location_permission_status=(
                        location_permission_status
                    ),
                    device_signature_verified=False,
                )
            )

            attempt = create_authentication_attempt(
                challenge=challenge,
                device=device,
                result=result,
                failure_reason=(
                    "Cihaz imzası doğrulanamadı."
                ),
                source_ip=source_ip,
                user_agent=user_agent,
                latitude=latitude,
                longitude=longitude,
                location_accuracy_meters=(
                    location_accuracy_meters
                ),
                location_permission_status=(
                    location_permission_status
                ),
                location_captured_at=(
                    location_captured_at
                ),
                signature=signature,
                gps_city=gps_city,
                gps_district=gps_district,
                gps_region=gps_region,
                gps_country=gps_country,
                gps_country_code=gps_country_code,
            )

            authentication_log = (
                create_authentication_log(
                    challenge=challenge,
                    device=device,
                    result=result,
                    failure_reason=(
                        "Cihaz imzası doğrulanamadı."
                    ),
                    request_ip=challenge.request_ip,
                    device_ip=source_ip,
                    user_agent=user_agent,
                    latitude=latitude,
                    longitude=longitude,
                    location_accuracy_meters=(
                        location_accuracy_meters
                    ),
                    location_permission_status=(
                        location_permission_status
                    ),
                    location_captured_at=(
                        location_captured_at
                    ),
                    gps_city=gps_city,
                    gps_district=gps_district,
                    gps_region=gps_region,
                    gps_country=gps_country,
                    gps_country_code=gps_country_code,
                    risk_score=risk_score,
                    risk_level=risk_level,
                    risk_reasons=risk_reasons,
                )
            )

            try:
                db.add(
                    attempt,
                )

                db.add(
                    authentication_log,
                )

                db.commit()

                db.refresh(
                    attempt,
                )

                db.refresh(
                    authentication_log,
                )

            except Exception:
                db.rollback()
                raise

            raise ChallengeVerificationError(
                "Cihaz imzası doğrulanamadı.",
            ) from exception

        now = utc_now()

        challenge.device_signature_verified = True
        challenge.attempt_count += 1

        if normalized_decision == "approve":
            challenge.status = (
                ChallengeStatus.APPROVED
            )

            challenge.approved_at = now
            challenge.completed_at = now

            result = AuthenticationResult.SUCCESS
            failure_reason = None
            is_successful = True

        else:
            challenge.status = (
                ChallengeStatus.REJECTED
            )

            challenge.rejected_at = now
            challenge.completed_at = now

            result = AuthenticationResult.REJECTED

            failure_reason = (
                "Doğrulama isteği kullanıcı "
                "tarafından reddedildi."
            )

            is_successful = False

        risk_score, risk_level, risk_reasons = (
            calculate_basic_risk(
                result=result,
                latitude=latitude,
                longitude=longitude,
                location_permission_status=(
                    location_permission_status
                ),
                device_signature_verified=True,
            )
        )

        attempt = create_authentication_attempt(
            challenge=challenge,
            device=device,
            result=result,
            failure_reason=failure_reason,
            source_ip=source_ip,
            user_agent=user_agent,
            latitude=latitude,
            longitude=longitude,
            location_accuracy_meters=(
                location_accuracy_meters
            ),
            location_permission_status=(
                location_permission_status
            ),
            location_captured_at=(
                location_captured_at
            ),
            signature=signature,
            gps_city=gps_city,
            gps_district=gps_district,
            gps_region=gps_region,
            gps_country=gps_country,
            gps_country_code=gps_country_code,
        )

        authentication_log = (
            create_authentication_log(
                challenge=challenge,
                device=device,
                result=result,
                failure_reason=failure_reason,
                request_ip=challenge.request_ip,
                device_ip=source_ip,
                user_agent=user_agent,
                latitude=latitude,
                longitude=longitude,
                location_accuracy_meters=(
                    location_accuracy_meters
                ),
                location_permission_status=(
                    location_permission_status
                ),
                location_captured_at=(
                    location_captured_at
                ),
                gps_city=gps_city,
                gps_district=gps_district,
                gps_region=gps_region,
                gps_country=gps_country,
                gps_country_code=gps_country_code,
                risk_score=risk_score,
                risk_level=risk_level,
                risk_reasons=risk_reasons,
            )
        )

        try:
            db.add(
                attempt,
            )

            db.add(
                authentication_log,
            )

            db.commit()

            db.refresh(
                challenge,
            )

            db.refresh(
                attempt,
            )

            db.refresh(
                authentication_log,
            )

        except Exception:
            db.rollback()
            raise

        return ChallengeVerificationResult(
            challenge=challenge,
            attempt=attempt,
            authentication_log=(
                authentication_log
            ),
            is_successful=is_successful,
        )


challenge_verification_service = (
    ChallengeVerificationService()
)