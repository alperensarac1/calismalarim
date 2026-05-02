import asyncio
import websockets
import json

TOKEN = "BURAYA_JWT_TOKEN"

async def main():
    url = f"ws://127.0.0.1:8000/ws?token={TOKEN}"

    async with websockets.connect(url) as websocket:
        print("Bağlandı.")

        # İlk auth cevabını al
        response = await websocket.recv()
        print("Sunucudan:", response)

        # Ping gönder
        await websocket.send(json.dumps({
            "event": "PING",
            "data": {}
        }))

        response = await websocket.recv()
        print("Sunucudan:", response)

        # Sürekli gelen eventleri dinle
        while True:
            message = await websocket.recv()
            print("Yeni event:", message)

asyncio.run(main())