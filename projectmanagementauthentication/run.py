import uvicorn

from app.config import get_settings


settings = get_settings()


if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",

        # 0.0.0.0 sayesinde aynı ağdaki fiziksel
        # Android veya iOS cihazları sunucuya ulaşabilir.
        host=settings.host,

        port=settings.port,

        # Geliştirme sırasında dosya değişikliklerinde
        # sunucu otomatik yeniden başlatılır.
        reload=settings.debug,

        log_level=(
            "debug"
            if settings.debug
            else "info"
        ),
    )