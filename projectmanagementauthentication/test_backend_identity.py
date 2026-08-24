import asyncio
import os

import httpx


BACKEND_URL = (
    "http://127.0.0.1:8080/api/Auth/me"
)

# Test sırasında tokenı ortam değişkeninden okuyacağız.
#
# Böylece PyCharm Run ekranında input bekleme
# problemiyle karşılaşmayacağız.
ACCESS_TOKEN = os.getenv(
    "BACKEND_ACCESS_TOKEN",
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJQcm9qZWN0TWFuYWdlbWVudC5DbGllbnQiLCJpc3MiOiJQcm9qZWN0TWFuYWdlbWVudC5BcGkiLCJleHAiOjE3ODU0OTIyNjcsImlhdCI6MTc4NTQ5MDQ2NywibmJmIjoxNzg1NDkwNDY3LCJzdWIiOiIxIiwianRpIjoiYjJiZDVjZWItNTg5ZS00OTQ1LTkxZGYtMWZjZWY0ZjEyYmUxIiwiZW1haWwiOiJhZG1pbkBwcm9qZWN0bWFuYWdlbWVudC5sb2NhbCIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL25hbWVpZGVudGlmaWVyIjoiMSIsImh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3dzLzIwMDUvMDUvaWRlbnRpdHkvY2xhaW1zL25hbWUiOiJTeXN0ZW0gQWRtaW5pc3RyYXRvciIsImh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vd3MvMjAwOC8wNi9pZGVudGl0eS9jbGFpbXMvcm9sZSI6IkFkbWluIiwidG9rZW5fdmVyc2lvbiI6IjIifQ.68Zx3ABBJz8Ny9l77RQAN-QU_ewOERUIwKYCG_KQc3o",
).strip()


async def main() -> None:
    print("Test başlatıldı.", flush=True)

    if not ACCESS_TOKEN:
        print(
            "BACKEND_ACCESS_TOKEN ortam değişkeni bulunamadı.",
            flush=True,
        )
        print(
            "PyCharm Run Configuration içine tokenı ekle.",
            flush=True,
        )
        return

    print(
        f"İstek gönderilecek adres: {BACKEND_URL}",
        flush=True,
    )

    print(
        f"Token uzunluğu: {len(ACCESS_TOKEN)}",
        flush=True,
    )

    print(
        "Backend isteği gönderiliyor...",
        flush=True,
    )

    timeout = httpx.Timeout(
        connect=5.0,
        read=10.0,
        write=10.0,
        pool=5.0,
    )

    try:
        async with httpx.AsyncClient(
            timeout=timeout,
            trust_env=False,
        ) as client:
            response = await client.get(
                BACKEND_URL,
                headers={
                    "Authorization": (
                        f"Bearer {ACCESS_TOKEN}"
                    ),
                    "Accept": "application/json",
                },
            )

        print(
            f"HTTP durum kodu: {response.status_code}",
            flush=True,
        )

        print(
            "Backend cevabı:",
            flush=True,
        )

        print(
            response.text,
            flush=True,
        )

    except httpx.ConnectTimeout:
        print(
            "Backend bağlantısı zaman aşımına uğradı.",
            flush=True,
        )

    except httpx.ReadTimeout:
        print(
            "Backend bağlantıyı kabul etti fakat "
            "zamanında cevap vermedi.",
            flush=True,
        )

    except httpx.ConnectError as exception:
        print(
            f"Backend bağlantı hatası: {exception}",
            flush=True,
        )

    except httpx.RequestError as exception:
        print(
            f"HTTP istek hatası: {exception}",
            flush=True,
        )

    except Exception as exception:
        print(
            "Beklenmeyen hata oluştu:",
            flush=True,
        )

        print(
            f"{type(exception).__name__}: {exception}",
            flush=True,
        )


if __name__ == "__main__":
    asyncio.run(
        main(),
    )