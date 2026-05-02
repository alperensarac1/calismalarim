# app/core/database.py

from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# SQLite veritabanı dosyası
# Bu dosya proje klasöründe otomatik oluşur: onlinetaksi.db
DATABASE_URL = "sqlite:///./onlinetaksi.db"

# SQLite özel ayarı:
# aynı thread dışında kullanımda hata vermemesi için gerekir
engine = create_engine(
    DATABASE_URL,
    connect_args={"check_same_thread": False},
    echo=True
)

SessionLocal = sessionmaker(
    autoflush=False,
    autocommit=False,
    bind=engine
)

Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()