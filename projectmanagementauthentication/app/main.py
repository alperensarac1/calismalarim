from __future__ import annotations

from contextlib import asynccontextmanager
from datetime import datetime, timezone

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import get_settings
from app.database import (
    check_database_connection,
    create_database_tables,
)
from app.routers.authentication_logs import (
    router as authentication_logs_router,
)
from app.routers.challenge_verification import (
    router as challenge_verification_router,
)
from app.routers.challenges import (
    router as challenges_router,
)
from app.routers.devices import (
    router as devices_router,
)
from app.routers.websockets import (
    router as websockets_router,
)
from app.websocket_manager import websocket_manager


# =========================================================
# UYGULAMA AYARLARI
# =========================================================


settings = get_settings()


# =========================================================
# YARDIMCI FONKSİYONLAR
# =========================================================


def utc_now() -> datetime:
    """
    UTC zaman dilimindeki güncel zamanı döndürür.
    """

    return datetime.now(
        timezone.utc,
    )


def get_allowed_cors_origins() -> list[str]:
    """
    Tarayıcıdan Python Authenticator servisine erişmesine
    izin verilen origin listesini oluşturur.

    Hem yeni cors_allowed_origins ayarını hem de eski
    frontend_origin ayarını destekler.

    Origin örnekleri:

    - http://localhost:5173
    - http://127.0.0.1:5173
    - http://10.203.83.58:5173

    Tarayıcı açısından bunların her biri farklı bir
    origin kabul edilir.
    """

    origins: list[str] = []

    # config.py içerisinde yeni eklediğimiz origin
    # listesini alıyoruz.
    configured_origins = getattr(
        settings,
        "cors_allowed_origins",
        [],
    )

    if isinstance(
        configured_origins,
        list,
    ):
        origins.extend(
            configured_origins,
        )

    # Eski frontend_origin ayarını kullanan kodlarla
    # geriye dönük uyumluluğu koruyoruz.
    frontend_origin = getattr(
        settings,
        "frontend_origin",
        None,
    )

    if isinstance(
        frontend_origin,
        str,
    ):
        origins.append(
            frontend_origin,
        )

    # Geliştirme ortamında kullanılan temel adresler.
    origins.extend(
        [
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://10.203.83.58:5173",
        ],
    )

    # Boş değerleri kaldırıyoruz, sondaki slash işaretini
    # temizliyoruz ve tekrar eden originleri siliyoruz.
    normalized_origins = [
        origin.strip().rstrip("/")
        for origin in origins
        if isinstance(origin, str)
        and origin.strip()
    ]

    return list(
        dict.fromkeys(
            normalized_origins,
        ),
    )


# =========================================================
# UYGULAMA YAŞAM DÖNGÜSÜ
# =========================================================


@asynccontextmanager
async def lifespan(
    _app: FastAPI,
):
    """
    FastAPI uygulamasının başlangıç ve kapanış
    işlemlerini yönetir.

    Uygulama başlarken:

    1. SQLite bağlantısı kontrol edilir.
    2. Henüz bulunmayan tablolar oluşturulur.

    Uygulama kapanırken:

    1. Aktif WebSocket bağlantıları kapatılır.
    """

    print()
    print(
        "=" * 65,
    )
    print(
        f"{settings.app_name} başlatılıyor...",
    )
    print(
        f"Uygulama sürümü: "
        f"{settings.app_version}",
    )
    print(
        f"Veritabanı: "
        f"{settings.database_url}",
    )
    print(
        "Veritabanı bağlantısı kontrol ediliyor...",
    )

    check_database_connection()

    print(
        "Veritabanı bağlantısı başarılı.",
    )
    print(
        "Veritabanı tabloları hazırlanıyor...",
    )

    create_database_tables()

    print(
        "Veritabanı tabloları hazır.",
    )
    print(
        f"Swagger: "
        f"http://127.0.0.1:{settings.port}/docs",
    )
    print(
        "CORS izin verilen originler:",
    )

    for origin in get_allowed_cors_origins():
        print(
            f" - {origin}",
        )

    print(
        "=" * 65,
    )
    print()

    yield

    print()
    print(
        "Aktif WebSocket bağlantıları kapatılıyor...",
    )

    await websocket_manager.close_all(
        code=1001,
        reason=(
            "Authenticator servisi kapatılıyor."
        ),
    )

    print(
        f"{settings.app_name} kapatılıyor...",
    )


# =========================================================
# FASTAPI UYGULAMASI
# =========================================================


app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description=(
        "Android, iOS ve diğer platformlardan bağımsız "
        "çalışan Authenticator servisidir."
    ),
    debug=settings.debug,
    lifespan=lifespan,
)


# =========================================================
# CORS AYARLARI
# =========================================================


# CORS middleware routerlardan önce eklenir.
#
# Böylece React uygulamasının OPTIONS preflight
# istekleri dahil bütün HTTP isteklerine gerekli CORS
# başlıkları eklenir.
app.add_middleware(
    CORSMiddleware,

    # İzin verilen React frontend adresleri.
    allow_origins=get_allowed_cors_origins(),

    # Authorization headerı ve ileride kullanılabilecek
    # cookie tabanlı işlemler için gereklidir.
    allow_credentials=True,

    # GET, POST, PUT, PATCH, DELETE ve OPTIONS dahil
    # bütün HTTP metotlarına izin verir.
    allow_methods=["*"],

    # Authorization ve Content-Type dahil React
    # tarafından gönderilebilecek bütün headerlara
    # izin verir.
    allow_headers=["*"],
)


# =========================================================
# ROUTER KAYITLARI
# =========================================================


app.include_router(
    devices_router,
)

app.include_router(
    challenges_router,
)

app.include_router(
    challenge_verification_router,
)

app.include_router(
    authentication_logs_router,
)

app.include_router(
    websockets_router,
)


# =========================================================
# TEMEL ENDPOINTLER
# =========================================================


@app.get(
    "/",
    tags=["System"],
    summary="Servis ana bilgilerini getirir",
)
def root() -> dict[str, object]:
    """
    Authenticator servisinin çalışıp çalışmadığını
    hızlıca kontrol etmek için kullanılan endpointtir.
    """

    return {
        "success": True,
        "message": (
            "Platform Independent Authenticator Service "
            "çalışıyor."
        ),
        "data": {
            "app_name": settings.app_name,
            "app_version": settings.app_version,
            "environment": (
                "development"
                if settings.debug
                else "production"
            ),
            "documentation": "/docs",
            "server_time_utc": (
                utc_now().isoformat()
            ),
            "cors_allowed_origins": (
                get_allowed_cors_origins()
            ),
        },
        "errors": {},
    }


@app.get(
    "/health",
    tags=["System"],
    summary="Servis sağlık kontrolü",
)
def health_check() -> dict[str, object]:
    """
    Python servisi ve SQLite bağlantısı için
    basit sağlık kontrolü gerçekleştirir.
    """

    try:
        check_database_connection()

        return {
            "success": True,
            "message": (
                "Authenticator servisi sağlıklı."
            ),
            "data": {
                "status": "healthy",
                "database": "connected",
                "server_time_utc": (
                    utc_now().isoformat()
                ),
            },
            "errors": {},
        }

    except Exception as exception:
        return {
            "success": False,
            "message": (
                "Authenticator servisinde sağlık "
                "kontrolü başarısız."
            ),
            "data": {
                "status": "unhealthy",
                "database": "disconnected",
                "server_time_utc": (
                    utc_now().isoformat()
                ),
            },
            "errors": {
                "database": [
                    str(
                        exception,
                    ),
                ],
            },
        }