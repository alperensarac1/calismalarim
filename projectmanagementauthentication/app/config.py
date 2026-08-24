from functools import lru_cache
from pathlib import Path

from pydantic import Field
from pydantic_settings import (
    BaseSettings,
    SettingsConfigDict,
)


# =========================================================
# PROJE KLASÖRLERİ
# =========================================================


# Bu dosya:
#
# Projectmanagementauthenticator/app/config.py
#
# konumunda bulunduğu için iki üst klasör proje köküdür.
BASE_DIR = Path(
    __file__,
).resolve().parent.parent


# SQLite veritabanı ve ileride üretilecek diğer kalıcı
# dosyalar bu klasör altında tutulacaktır.
DATA_DIR = BASE_DIR / "data"


# Klasör mevcut değilse uygulama başlarken otomatik
# olarak oluşturulur.
DATA_DIR.mkdir(
    parents=True,
    exist_ok=True,
)


# =========================================================
# UYGULAMA AYARLARI
# =========================================================


class Settings(BaseSettings):
    """
    Authenticator servisinin merkezi ayarlarını
    temsil eder.

    Ayarlar şu öncelik sırasıyla okunur:

    1. İşletim sistemi ortam değişkenleri
    2. Proje kökündeki .env dosyası
    3. Bu sınıfta tanımlanan varsayılan değerler
    """

    # =====================================================
    # TEMEL UYGULAMA BİLGİLERİ
    # =====================================================

    app_name: str = (
        "Platform Independent Authenticator Service"
    )

    app_version: str = "1.0.0"

    debug: bool = True

    host: str = "0.0.0.0"

    port: int = Field(
        default=8090,
        ge=1,
        le=65535,
    )

    # =====================================================
    # VERİTABANI AYARLARI
    # =====================================================

    database_url: str = (
        f"sqlite:///{DATA_DIR / 'authenticator.db'}"
    )

    # =====================================================
    # JWT VE GÜVENLİK AYARLARI
    # =====================================================

    secret_key: str = Field(
        default=(
            "CHANGE-THIS-DEVELOPMENT-SECRET-KEY"
        ),
        min_length=32,
    )

    jwt_algorithm: str = "HS256"

    access_token_expire_minutes: int = Field(
        default=30,
        ge=1,
    )

    device_token_expire_days: int = Field(
        default=365,
        ge=1,
    )

    service_api_key: str = Field(
        default=(
            "CHANGE-THIS-SERVICE-API-KEY"
        ),
        min_length=24,
    )

    # =====================================================
    # CHALLENGE AYARLARI
    # =====================================================

    challenge_expire_seconds: int = Field(
        default=120,
        ge=30,
        le=900,
    )

    challenge_max_attempts: int = Field(
        default=5,
        ge=1,
        le=20,
    )

    test_challenge_code: str = Field(
        default="987456",
        min_length=6,
        max_length=12,
        pattern=r"^[0-9]+$",
    )

    # =====================================================
    # DEMO MODU
    # =====================================================

    allow_challenge_without_device: bool = True

    auto_create_external_user: bool = True

    # =====================================================
    # FRONTEND VE CORS AYARLARI
    # =====================================================

    # Tarayıcıdan Python Authenticator servisine istek
    # göndermesine izin verilecek frontend originleri.
    #
    # Origin; protokol, alan adı ve port birleşimidir.
    #
    # Örneğin:
    #
    # http://localhost:5173
    #
    # ile:
    #
    # http://127.0.0.1:5173
    #
    # tarayıcı açısından farklı originlerdir.
    cors_allowed_origins: list[str] = [
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://10.203.83.58:5173",
    ]

    # Eski kodlarda frontend_origin kullanılıyorsa
    # uyumluluğu korumak için bırakılmıştır.
    frontend_origin: str = (
        "http://localhost:5173"
    )

    # =====================================================
    # MEVCUT .NET BACKEND AYARLARI
    # =====================================================

    main_backend_base_url: str = (
        "http://127.0.0.1:8080"
    )

    main_backend_current_user_path: str = (
        "/api/Auth/me"
    )

    main_backend_timeout_seconds: float = Field(
        default=10.0,
        gt=0,
        le=60,
    )

    verify_backend_access_token: bool = True

    # =====================================================
    # REVERSE GEOCODING AYARLARI
    # =====================================================

    reverse_geocoding_enabled: bool = True

    reverse_geocoding_user_agent: str = Field(
        default=(
            "projectmanagement-authenticator/1.0"
        ),
        min_length=3,
        max_length=200,
    )

    reverse_geocoding_timeout_seconds: float = Field(
        default=8.0,
        gt=0,
        le=60,
    )

    reverse_geocoding_cache_size: int = Field(
        default=500,
        ge=1,
        le=10000,
    )

    # =====================================================
    # PYDANTIC SETTINGS YAPILANDIRMASI
    # =====================================================

    model_config = SettingsConfigDict(
        env_file=BASE_DIR / ".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        extra="ignore",
    )


# =========================================================
# AYAR NESNESİ
# =========================================================


@lru_cache
def get_settings() -> Settings:
    """
    Settings nesnesini uygulama süresince yalnızca
    bir kez oluşturur.
    """

    return Settings()