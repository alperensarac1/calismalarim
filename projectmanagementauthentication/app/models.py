from __future__ import annotations

import enum
import uuid

from datetime import datetime, timezone

from sqlalchemy import (
    Boolean,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    Index,
    Integer,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.orm import (
    Mapped,
    mapped_column,
    relationship,
)

from app.database import Base


def utc_now() -> datetime:
    """
    UTC zaman diliminde güncel tarih ve saat değerini döndürür.
    """

    return datetime.now(timezone.utc)


def generate_uuid() -> str:
    """
    UUID4 biçiminde benzersiz bir string üretir.
    """

    return str(uuid.uuid4())


class DevicePlatform(str, enum.Enum):
    """
    Authenticator istemcisinin çalıştığı platform.
    """

    ANDROID = "android"
    IOS = "ios"
    WINDOWS = "windows"
    MACOS = "macos"
    LINUX = "linux"
    OTHER = "other"


class ChallengeStatus(str, enum.Enum):
    """
    Doğrulama isteğinin mevcut durumu.
    """

    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"
    EXPIRED = "expired"
    LOCKED = "locked"
    CANCELLED = "cancelled"


class AuthenticationResult(str, enum.Enum):
    """
    Bir doğrulama girişiminin sonucu.
    """

    SUCCESS = "success"
    FAILED = "failed"
    REJECTED = "rejected"
    EXPIRED = "expired"
    LOCKED = "locked"
    CANCELLED = "cancelled"


class AuthenticationMethod(str, enum.Enum):
    """
    İkinci faktör doğrulama yöntemi.
    """

    ONE_TIME_CODE = "one_time_code"
    MOBILE_APPROVAL = "mobile_approval"
    DEVICE_SIGNATURE = "device_signature"


class ExternalUser(Base):
    """
    Ana uygulamadaki kullanıcı ile Authenticator Service
    arasındaki eşleştirme kaydıdır.

    Ana uygulamanın kullanıcısını tamamen kopyalamaz.
    Yalnızca MFA için gerekli minimum bilgiler tutulur.
    """

    __tablename__ = "external_users"

    id: Mapped[int] = mapped_column(
        Integer,
        primary_key=True,
        autoincrement=True,
    )

    public_id: Mapped[str] = mapped_column(
        String(36),
        unique=True,
        nullable=False,
        default=generate_uuid,
        index=True,
    )

    # Ana uygulamadaki kullanıcı kimliği.
    #
    # String tutulması sayesinde ileride yalnızca sayısal
    # kullanıcı kimliklerine bağlı kalmayız.
    external_user_id: Mapped[str] = mapped_column(
        String(150),
        nullable=False,
        unique=True,
        index=True,
    )

    email: Mapped[str | None] = mapped_column(
        String(320),
        nullable=True,
        index=True,
    )

    display_name: Mapped[str | None] = mapped_column(
        String(200),
        nullable=True,
    )

    is_active: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=True,
    )

    mfa_enabled: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=True,
    )

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
    )

    updated_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
        onupdate=utc_now,
    )

    devices: Mapped[list[RegisteredDevice]] = relationship(
        back_populates="user",
        cascade="all, delete-orphan",
    )

    challenges: Mapped[list[AuthenticationChallenge]] = relationship(
        back_populates="user",
        cascade="all, delete-orphan",
    )

    authentication_logs: Mapped[list[AuthenticationLog]] = relationship(
        back_populates="user",
        cascade="all, delete-orphan",
    )


class RegisteredDevice(Base):
    """
    Android, iOS veya diğer platformlardaki kayıtlı
    Authenticator uygulamasını temsil eder.

    MAC adresi kullanılmaz.

    installation_id uygulamanın ilk kurulumunda ürettiği
    rastgele kimliktir. public_key ise cihazın ürettiği
    anahtar çiftinin açık anahtarıdır.
    """

    __tablename__ = "registered_devices"

    __table_args__ = (
        UniqueConstraint(
            "user_id",
            "installation_id",
            name="uq_registered_device_user_installation",
        ),
        Index(
            "ix_registered_devices_user_active",
            "user_id",
            "is_active",
        ),
    )

    id: Mapped[int] = mapped_column(
        Integer,
        primary_key=True,
        autoincrement=True,
    )

    public_id: Mapped[str] = mapped_column(
        String(36),
        unique=True,
        nullable=False,
        default=generate_uuid,
        index=True,
    )

    user_id: Mapped[int] = mapped_column(
        ForeignKey(
            "external_users.id",
            ondelete="CASCADE",
        ),
        nullable=False,
        index=True,
    )

    installation_id: Mapped[str] = mapped_column(
        String(150),
        nullable=False,
        index=True,
    )

    platform: Mapped[DevicePlatform] = mapped_column(
        Enum(
            DevicePlatform,
            native_enum=False,
            length=20,
        ),
        nullable=False,
        index=True,
    )

    device_name: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    device_model: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    manufacturer: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    os_name: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
    )

    os_version: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
    )

    app_version: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
    )

    locale: Mapped[str | None] = mapped_column(
        String(50),
        nullable=True,
    )

    timezone_name: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
    )

    # PEM veya Base64 biçimindeki açık anahtar.
    public_key: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )
    # Cihazın kullandığı imza algoritması.
    #
    # Android ve iOS için ortak olarak
    # ECDSA P-256 + SHA-256 kullanacağız.
    key_algorithm: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
    )

    # Public key'in SHA-256 parmak izi.
    #
    # Aynı anahtarın tekrar kullanılıp kullanılmadığını
    # anlamak ve admin ekranında göstermek için kullanılır.
    public_key_fingerprint: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
        index=True,
    )

    # Cihaz anahtarının oluşturulduğu zaman.
    key_created_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    # Android Key Attestation veya iOS App Attest gibi
    # doğrulama verileri ileride bu alanda saklanabilir.
    key_attestation: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )

    # Attestation doğrulamasının başarılı olup olmadığını
    # belirtir.
    key_attestation_verified: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=False,
    )
    # Firebase/APNs gibi bildirim servisleri için
    # kullanılabilecek platform bağımsız token alanı.
    push_token: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )

    is_active: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=True,
        index=True,
    )

    registered_ip: Mapped[str | None] = mapped_column(
        String(64),
        nullable=True,
    )

    last_ip: Mapped[str | None] = mapped_column(
        String(64),
        nullable=True,
    )

    registered_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
    )

    last_seen_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    revoked_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    user: Mapped[ExternalUser] = relationship(
        back_populates="devices",
    )

    challenges: Mapped[list[AuthenticationChallenge]] = relationship(
        back_populates="target_device",
    )

    attempts: Mapped[list[AuthenticationAttempt]] = relationship(
        back_populates="device",
    )

    authentication_logs: Mapped[list[AuthenticationLog]] = relationship(
        back_populates="device",
    )


class AuthenticationChallenge(Base):
    """
    Web uygulamasında başlayan tek kullanımlık
    doğrulama isteğini temsil eder.

    Kod düz metin olarak saklanmaz.
    Yalnızca code_hash alanı veritabanına kaydedilir.
    """

    __tablename__ = "authentication_challenges"

    __table_args__ = (
        Index(
            "ix_auth_challenges_user_status",
            "user_id",
            "status",
        ),
        Index(
            "ix_auth_challenges_expires_at",
            "expires_at",
        ),
    )

    id: Mapped[int] = mapped_column(
        Integer,
        primary_key=True,
        autoincrement=True,
    )

    public_id: Mapped[str] = mapped_column(
        String(36),
        unique=True,
        nullable=False,
        default=generate_uuid,
        index=True,
    )

    user_id: Mapped[int] = mapped_column(
        ForeignKey(
            "external_users.id",
            ondelete="CASCADE",
        ),
        nullable=False,
        index=True,
    )

    target_device_id: Mapped[int | None] = mapped_column(
        ForeignKey(
            "registered_devices.id",
            ondelete="SET NULL",
        ),
        nullable=True,
        index=True,
    )

    method: Mapped[AuthenticationMethod] = mapped_column(
        Enum(
            AuthenticationMethod,
            native_enum=False,
            length=40,
        ),
        nullable=False,
        default=AuthenticationMethod.ONE_TIME_CODE,
    )

    status: Mapped[ChallengeStatus] = mapped_column(
        Enum(
            ChallengeStatus,
            native_enum=False,
            length=20,
        ),
        nullable=False,
        default=ChallengeStatus.PENDING,
        index=True,
    )

    code_hash: Mapped[str | None] = mapped_column(
        String(255),
        nullable=True,
    )
    # Her challenge için üretilen tek kullanımlık
    # rastgele değerdir.
    #
    # Mobil cihaz bu nonce değerini challenge verisiyle
    # birlikte imzalar. Aynı imzanın tekrar kullanılmasını
    # engeller.
    nonce: Mapped[str] = mapped_column(
        String(255),
        nullable=False,
        index=True,
    )

    # Mobil cihazdan gelen imzanın doğrulanıp
    # doğrulanmadığını gösterir.
    device_signature_verified: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=False,
    )
    attempt_count: Mapped[int] = mapped_column(
        Integer,
        nullable=False,
        default=0,
    )

    max_attempts: Mapped[int] = mapped_column(
        Integer,
        nullable=False,
        default=5,
    )

    request_ip: Mapped[str | None] = mapped_column(
        String(64),
        nullable=True,
    )

    forwarded_ip: Mapped[str | None] = mapped_column(
        String(255),
        nullable=True,
    )

    user_agent: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )

    request_origin: Mapped[str | None] = mapped_column(
        String(500),
        nullable=True,
    )

    request_correlation_id: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
        index=True,
    )

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
    )

    expires_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        index=True,
    )

    approved_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    rejected_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    completed_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    user: Mapped[ExternalUser] = relationship(
        back_populates="challenges",
    )

    target_device: Mapped[RegisteredDevice | None] = relationship(
        back_populates="challenges",
    )

    attempts: Mapped[list[AuthenticationAttempt]] = relationship(
        back_populates="challenge",
        cascade="all, delete-orphan",
    )

    authentication_logs: Mapped[list[AuthenticationLog]] = relationship(
        back_populates="challenge",
        cascade="all, delete-orphan",
    )


class AuthenticationAttempt(Base):
    """
    Bir challenge için yapılan her doğrulama denemesini saklar.

    Yanlış kod, cihaz reddi, başarılı onay veya süresi dolmuş
    challenge gibi her deneme ayrı kayıt olur.
    """

    __tablename__ = "authentication_attempts"

    __table_args__ = (
        Index(
            "ix_auth_attempts_challenge_created",
            "challenge_id",
            "created_at",
        ),
    )

    id: Mapped[int] = mapped_column(
        Integer,
        primary_key=True,
        autoincrement=True,
    )

    public_id: Mapped[str] = mapped_column(
        String(36),
        unique=True,
        nullable=False,
        default=generate_uuid,
        index=True,
    )

    challenge_id: Mapped[int] = mapped_column(
        ForeignKey(
            "authentication_challenges.id",
            ondelete="CASCADE",
        ),
        nullable=False,
        index=True,
    )

    device_id: Mapped[int | None] = mapped_column(
        ForeignKey(
            "registered_devices.id",
            ondelete="SET NULL",
        ),
        nullable=True,
        index=True,
    )

    result: Mapped[AuthenticationResult] = mapped_column(
        Enum(
            AuthenticationResult,
            native_enum=False,
            length=20,
        ),
        nullable=False,
        index=True,
    )

    failure_reason: Mapped[str | None] = mapped_column(
        String(500),
        nullable=True,
    )

    source_ip: Mapped[str | None] = mapped_column(
        String(64),
        nullable=True,
    )

    user_agent: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )

    latitude: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    longitude: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    location_accuracy_meters: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    location_permission_status: Mapped[str | None] = mapped_column(
        String(50),
        nullable=True,
    )
    # Konumun mobil cihaz tarafından alındığı zaman.
    location_captured_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    # GPS koordinatlarından reverse geocoding ile
    # elde edilen konum bilgileri.
    gps_city: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    gps_district: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    gps_region: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    gps_country: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    gps_country_code: Mapped[str | None] = mapped_column(
        String(10),
        nullable=True,
    )

    # Telefonun bağlantı IP'sinden tahmini olarak
    # elde edilen konum bilgileri.
    ip_city: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    ip_region: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    ip_country: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    ip_country_code: Mapped[str | None] = mapped_column(
        String(10),
        nullable=True,
    )

    # GPS ve IP konumları arasındaki yaklaşık mesafe.
    location_distance_km: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    # GPS ve IP konumları arasında anlamlı bir
    # uyumsuzluk bulunup bulunmadığı.
    location_mismatch: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=False,
    )
    signature: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
        index=True,
    )

    challenge: Mapped[AuthenticationChallenge] = relationship(
        back_populates="attempts",
    )

    device: Mapped[RegisteredDevice | None] = relationship(
        back_populates="attempts",
    )


class AuthenticationLog(Base):
    """
    Admin panelinde görüntülenecek güvenlik olay kaydıdır.

    Challenge ve attempt tabloları operasyonel ayrıntıları
    tutarken bu tablo, admin ekranında hızlı raporlama için
    özetlenmiş güvenlik olaylarını saklar.
    """

    __tablename__ = "authentication_logs"

    __table_args__ = (
        Index(
            "ix_auth_logs_user_created",
            "user_id",
            "created_at",
        ),
        Index(
            "ix_auth_logs_result_created",
            "result",
            "created_at",
        ),
        Index(
            "ix_auth_logs_risk_created",
            "risk_score",
            "created_at",
        ),
    )

    id: Mapped[int] = mapped_column(
        Integer,
        primary_key=True,
        autoincrement=True,
    )

    public_id: Mapped[str] = mapped_column(
        String(36),
        unique=True,
        nullable=False,
        default=generate_uuid,
        index=True,
    )

    user_id: Mapped[int] = mapped_column(
        ForeignKey(
            "external_users.id",
            ondelete="CASCADE",
        ),
        nullable=False,
        index=True,
    )

    challenge_id: Mapped[int | None] = mapped_column(
        ForeignKey(
            "authentication_challenges.id",
            ondelete="SET NULL",
        ),
        nullable=True,
        index=True,
    )

    device_id: Mapped[int | None] = mapped_column(
        ForeignKey(
            "registered_devices.id",
            ondelete="SET NULL",
        ),
        nullable=True,
        index=True,
    )

    result: Mapped[AuthenticationResult] = mapped_column(
        Enum(
            AuthenticationResult,
            native_enum=False,
            length=20,
        ),
        nullable=False,
        index=True,
    )

    method: Mapped[AuthenticationMethod] = mapped_column(
        Enum(
            AuthenticationMethod,
            native_enum=False,
            length=40,
        ),
        nullable=False,
    )

    external_user_id_snapshot: Mapped[str] = mapped_column(
        String(150),
        nullable=False,
        index=True,
    )

    email_snapshot: Mapped[str | None] = mapped_column(
        String(320),
        nullable=True,
        index=True,
    )

    display_name_snapshot: Mapped[str | None] = mapped_column(
        String(200),
        nullable=True,
    )

    platform_snapshot: Mapped[str | None] = mapped_column(
        String(30),
        nullable=True,
    )

    device_name_snapshot: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    device_model_snapshot: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    os_name_snapshot: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
    )

    os_version_snapshot: Mapped[str | None] = mapped_column(
        String(100),
        nullable=True,
    )

    request_ip: Mapped[str | None] = mapped_column(
        String(64),
        nullable=True,
        index=True,
    )

    device_ip: Mapped[str | None] = mapped_column(
        String(64),
        nullable=True,
    )

    user_agent: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )

    latitude: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    longitude: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    location_accuracy_meters: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    location_permission_status: Mapped[str | None] = mapped_column(
        String(50),
        nullable=True,
    )
    location_captured_at: Mapped[datetime | None] = mapped_column(
        DateTime(timezone=True),
        nullable=True,
    )

    gps_city: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
        index=True,
    )

    gps_district: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    gps_region: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    gps_country: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
        index=True,
    )

    gps_country_code: Mapped[str | None] = mapped_column(
        String(10),
        nullable=True,
        index=True,
    )

    ip_city: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
        index=True,
    )

    ip_region: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
    )

    ip_country: Mapped[str | None] = mapped_column(
        String(150),
        nullable=True,
        index=True,
    )

    ip_country_code: Mapped[str | None] = mapped_column(
        String(10),
        nullable=True,
        index=True,
    )

    location_distance_km: Mapped[float | None] = mapped_column(
        Float,
        nullable=True,
    )

    location_mismatch: Mapped[bool] = mapped_column(
        Boolean,
        nullable=False,
        default=False,
        index=True,
    )
    risk_score: Mapped[int] = mapped_column(
        Integer,
        nullable=False,
        default=0,
        index=True,
    )

    risk_level: Mapped[str] = mapped_column(
        String(30),
        nullable=False,
        default="low",
        index=True,
    )

    risk_reasons: Mapped[str | None] = mapped_column(
        Text,
        nullable=True,
    )

    failure_reason: Mapped[str | None] = mapped_column(
        String(500),
        nullable=True,
    )

    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        nullable=False,
        default=utc_now,
        index=True,
    )

    user: Mapped[ExternalUser] = relationship(
        back_populates="authentication_logs",
    )

    challenge: Mapped[AuthenticationChallenge | None] = relationship(
        back_populates="authentication_logs",
    )

    device: Mapped[RegisteredDevice | None] = relationship(
        back_populates="authentication_logs",
    )