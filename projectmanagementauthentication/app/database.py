from collections.abc import Generator

from sqlalchemy import create_engine, event
from sqlalchemy.engine import Engine
from sqlalchemy.orm import (
    DeclarativeBase,
    Session,
    sessionmaker,
)

from app.config import get_settings


settings = get_settings()


class Base(DeclarativeBase):
    """
    Bütün SQLAlchemy modellerinin miras alacağı
    temel sınıftır.

    Örnek:

    class RegisteredDevice(Base):
        ...
    """

    pass


# SQLite aynı bağlantının farklı thread'lerde kullanılmasına
# varsayılan olarak izin vermez.
#
# FastAPI istekleri farklı worker/thread'lerde çalışabileceği için
# check_same_thread=False ayarını kullanıyoruz.
connect_args: dict[str, object] = {}

if settings.database_url.startswith("sqlite"):
    connect_args = {
        "check_same_thread": False,
    }


engine = create_engine(
    settings.database_url,

    # Geliştirme sırasında SQL sorgularını görmek istersek
    # DEBUG=true olduğunda terminale yazdırılır.
    echo=settings.debug,

    connect_args=connect_args,

    # Bağlantının kullanılmadan önce geçerli olup olmadığını
    # kontrol eder.
    pool_pre_ping=True,
)


SessionLocal = sessionmaker(
    bind=engine,

    # commit işleminden sonra nesnelerin alanlarının
    # tekrar okunabilmesini kolaylaştırır.
    expire_on_commit=False,

    # Session sınıfını açıkça belirtiyoruz.
    class_=Session,
)


@event.listens_for(Engine, "connect")
def enable_sqlite_foreign_keys(
    dbapi_connection,
    _connection_record,
) -> None:
    """
    SQLite foreign key kontrollerini aktif eder.

    SQLite, foreign key tanımlarını desteklese de bu özellik
    varsayılan olarak kapalı olabilir.

    Bu event yalnızca SQLite bağlantılarında PRAGMA komutunu
    çalıştırır.
    """

    if not settings.database_url.startswith(
        "sqlite",
    ):
        return

    cursor = dbapi_connection.cursor()

    try:
        cursor.execute(
            "PRAGMA foreign_keys=ON",
        )

        # Write-Ahead Logging, aynı anda okuma ve yazma
        # işlemlerinde SQLite performansını iyileştirebilir.
        cursor.execute(
            "PRAGMA journal_mode=WAL",
        )

        # Geçici sorgu verilerini bellekte tutar.
        cursor.execute(
            "PRAGMA temp_store=MEMORY",
        )

        # SQLite'ın kilitli veritabanı için kısa süre
        # beklemesini sağlar.
        cursor.execute(
            "PRAGMA busy_timeout=5000",
        )
    finally:
        cursor.close()


def get_db() -> Generator[
    Session,
    None,
    None,
]:
    """
    FastAPI endpointlerinde kullanılacak database dependency'sidir.

    Her HTTP isteği için yeni bir session oluşturulur.
    Endpoint tamamlandığında session güvenli şekilde kapatılır.

    Kullanım:

    @router.get("/devices")
    def get_devices(
        db: Session = Depends(get_db),
    ):
        ...
    """

    database_session = SessionLocal()

    try:
        yield database_session
    finally:
        database_session.close()


def create_database_tables() -> None:
    """
    Import edilmiş bütün SQLAlchemy modellerinin
    tablolarını veritabanında oluşturur.

    Bu fonksiyon yalnızca mevcut olmayan tabloları oluşturur.
    Var olan tabloları silmez veya migration uygulamaz.

    İlk geliştirme aşamasında yeterlidir.
    Production ortamında Alembic migration kullanacağız.
    """

    # Modellerin Base.metadata içerisine kaydolması için
    # models modülünün import edilmesi gerekir.
    #
    # Import burada yapılır; böylece database.py ile models.py
    # arasında oluşabilecek circular import problemi azaltılır.
    from app import models  # noqa: F401

    Base.metadata.create_all(
        bind=engine,
    )


def check_database_connection() -> None:
    """
    Uygulama başlatılırken veritabanı bağlantısını test eder.

    Bağlantı kurulamazsa hata yükselir ve uygulama
    sessizce hatalı durumda çalışmaya devam etmez.
    """

    with engine.connect() as connection:
        connection.exec_driver_sql(
            "SELECT 1",
        )