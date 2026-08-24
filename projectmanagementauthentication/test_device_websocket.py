import asyncio
import json
import os
from datetime import datetime, timezone

import websockets
from websockets.exceptions import (
    ConnectionClosed,
)


WEBSOCKET_URL = (
    "ws://127.0.0.1:8090/ws/device"
)

DEVICE_ACCESS_TOKEN = os.getenv(
    "AUTHENTICATOR_DEVICE_TOKEN",
    "",
).strip()

INSTALLATION_ID = os.getenv(
    "AUTHENTICATOR_INSTALLATION_ID",
    (
        "6f8a8c7a-7d14-4abe-"
        "9e68-e2b69434bfd4"
    ),
).strip()


def utc_now_iso() -> str:
    """
    Güncel UTC zamanını ISO biçiminde döndürür.
    """

    return datetime.now(
        timezone.utc,
    ).isoformat()


async def heartbeat_loop(
    websocket,
) -> None:
    """
    Bağlantı açıkken her 20 saniyede bir
    heartbeat mesajı gönderir.
    """

    while True:
        await asyncio.sleep(
            20,
        )

        await websocket.send(
            json.dumps(
                {
                    "type": "heartbeat",
                    "sent_at": utc_now_iso(),
                }
            )
        )


async def receive_loop(
    websocket,
) -> None:
    """
    Sunucudan gelen bütün WebSocket mesajlarını
    sürekli dinler ve terminale yazdırır.
    """

    async for raw_message in websocket:
        print()
        print("=" * 60)
        print("SUNUCUDAN MESAJ GELDİ")
        print("=" * 60)
        print(raw_message)

        try:
            parsed_message = json.loads(
                raw_message,
            )
        except json.JSONDecodeError:
            continue

        message_type = parsed_message.get(
            "type",
        )

        if (
            message_type
            == "authentication_challenge"
        ):
            print()
            print("YENİ AUTHENTICATOR İSTEĞİ")
            print(
                "Challenge ID:",
                parsed_message.get(
                    "challenge_public_id",
                ),
            )
            print(
                "Kullanıcı:",
                parsed_message.get(
                    "display_name",
                ),
            )
            print(
                "IP:",
                parsed_message.get(
                    "request_ip",
                ),
            )
            print(
                "Kaynak:",
                parsed_message.get(
                    "request_origin",
                ),
            )
            print(
                "Doğrulama kodu:",
                parsed_message.get(
                    "one_time_code",
                ),
            )
            print(
                "Sona erme:",
                parsed_message.get(
                    "expires_at",
                ),
            )


async def main() -> None:
    if not DEVICE_ACCESS_TOKEN:
        print(
            "AUTHENTICATOR_DEVICE_TOKEN bulunamadı."
        )
        return

    if not DEVICE_ACCESS_TOKEN.startswith(
        "eyJ",
    ):
        print(
            "AUTHENTICATOR_DEVICE_TOKEN geçerli "
            "bir JWT gibi görünmüyor."
        )
        return

    print(
        f"WebSocket bağlantısı açılıyor: "
        f"{WEBSOCKET_URL}"
    )

    try:
        async with websockets.connect(
            WEBSOCKET_URL,
            open_timeout=10,
            close_timeout=5,
        ) as websocket:
            await websocket.send(
                json.dumps(
                    {
                        "type": "authenticate",
                        "installation_id": (
                            INSTALLATION_ID
                        ),
                        "device_access_token": (
                            DEVICE_ACCESS_TOKEN
                        ),
                    }
                )
            )

            first_response = (
                await websocket.recv()
            )

            print()
            print(
                "Kimlik doğrulama cevabı:"
            )
            print(
                first_response
            )

            parsed_first_response = json.loads(
                first_response,
            )

            if (
                parsed_first_response.get(
                    "type",
                )
                != "authenticated"
            ):
                print(
                    "Cihaz doğrulanamadı."
                )
                return

            print()
            print(
                "Cihaz WebSocket bağlantısı hazır."
            )
            print(
                "Swagger üzerinden challenge "
                "oluşturabilirsin."
            )
            print(
                "Programı kapatmak için Ctrl+C."
            )

            heartbeat_task = asyncio.create_task(
                heartbeat_loop(
                    websocket,
                )
            )

            receive_task = asyncio.create_task(
                receive_loop(
                    websocket,
                )
            )

            done, pending = await asyncio.wait(
                {
                    heartbeat_task,
                    receive_task,
                },
                return_when=(
                    asyncio.FIRST_EXCEPTION
                ),
            )

            for task in pending:
                task.cancel()

            for task in done:
                exception = task.exception()

                if exception is not None:
                    raise exception

    except ConnectionClosed as exception:
        print()
        print(
            "WebSocket bağlantısı kapandı."
        )
        print(
            f"Kod: {exception.code}"
        )
        print(
            f"Neden: {exception.reason}"
        )

    except KeyboardInterrupt:
        print(
            "WebSocket testi kullanıcı "
            "tarafından durduruldu."
        )

    except Exception as exception:
        print(
            "WebSocket testinde hata oluştu:"
        )
        print(
            f"{type(exception).__name__}: "
            f"{exception}"
        )


if __name__ == "__main__":
    try:
        asyncio.run(
            main(),
        )
    except KeyboardInterrupt:
        print(
            "\nProgram kapatıldı."
        )