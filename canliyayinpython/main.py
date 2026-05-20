import asyncio
import json
import sqlite3
import uuid
from datetime import datetime

import websockets

DB_NAME = "live_stream.db"
HOST = "0.0.0.0"
PORT = 8765

rooms_runtime = {}
lobby_clients = set()
# rooms_runtime = {
#   "room_id": {
#       "broadcaster": websocket,
#       "viewers": set()
#   }
# }


def now():
    return datetime.now().strftime("%Y-%m-%d %H:%M:%S")


def db_connect():
    return sqlite3.connect(DB_NAME)


def init_db():
    conn = db_connect()
    cur = conn.cursor()

    cur.execute("""
    CREATE TABLE IF NOT EXISTS rooms (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        broadcaster_name TEXT NOT NULL,
        is_live INTEGER NOT NULL DEFAULT 1,
        created_at TEXT NOT NULL,
        ended_at TEXT
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS participants (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        room_id TEXT NOT NULL,
        username TEXT NOT NULL,
        role TEXT NOT NULL,
        joined_at TEXT NOT NULL,
        left_at TEXT,
        FOREIGN KEY(room_id) REFERENCES rooms(id)
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS chat_messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        room_id TEXT NOT NULL,
        username TEXT NOT NULL,
        message TEXT NOT NULL,
        created_at TEXT NOT NULL,
        FOREIGN KEY(room_id) REFERENCES rooms(id)
    )
    """)

    conn.commit()
    conn.close()


def create_room_db(room_id, title, broadcaster_name):
    conn = db_connect()
    cur = conn.cursor()

    cur.execute("""
    INSERT INTO rooms(id, title, broadcaster_name, is_live, created_at)
    VALUES (?, ?, ?, 1, ?)
    """, (room_id, title, broadcaster_name, now()))

    conn.commit()
    conn.close()


def finish_room_db(room_id):
    conn = db_connect()
    cur = conn.cursor()

    cur.execute("""
    UPDATE rooms
    SET is_live = 0, ended_at = ?
    WHERE id = ?
    """, (now(), room_id))

    conn.commit()
    conn.close()


def add_participant_db(room_id, username, role):
    conn = db_connect()
    cur = conn.cursor()

    cur.execute("""
    INSERT INTO participants(room_id, username, role, joined_at)
    VALUES (?, ?, ?, ?)
    """, (room_id, username, role, now()))

    conn.commit()
    conn.close()


def add_chat_message_db(room_id, username, message):
    conn = db_connect()
    cur = conn.cursor()

    cur.execute("""
    INSERT INTO chat_messages(room_id, username, message, created_at)
    VALUES (?, ?, ?, ?)
    """, (room_id, username, message, now()))

    conn.commit()
    conn.close()


def get_live_rooms():
    conn = db_connect()
    cur = conn.cursor()

    cur.execute("""
    SELECT id, title, broadcaster_name, created_at
    FROM rooms
    WHERE is_live = 1
    ORDER BY created_at DESC
    """)

    rows = cur.fetchall()
    conn.close()

    result = []

    for row in rows:
        room_id = row[0]

        viewer_count = 0
        if room_id in rooms_runtime:
            viewer_count = len(rooms_runtime[room_id]["viewers"])

        result.append({
            "room_id": row[0],
            "title": row[1],
            "broadcaster_name": row[2],
            "created_at": row[3],
            "viewer_count": viewer_count
        })

    return result


async def send_json(ws, data):
    await ws.send(json.dumps(data))


async def broadcast_to_room(room_id, data):
    if room_id not in rooms_runtime:
        return

    payload = json.dumps(data)

    clients = set()

    broadcaster = rooms_runtime[room_id].get("broadcaster")
    if broadcaster:
        clients.add(broadcaster)

    clients.update(rooms_runtime[room_id]["viewers"])

    disconnected = set()

    for client in clients:
        try:
            await client.send(payload)
        except:
            disconnected.add(client)

    for client in disconnected:
        rooms_runtime[room_id]["viewers"].discard(client)


async def broadcast_viewer_count(room_id):
    if room_id not in rooms_runtime:
        return

    await broadcast_to_room(room_id, {
        "type": "viewer_count",
        "room_id": room_id,
        "viewer_count": len(rooms_runtime[room_id]["viewers"])
    })


async def broadcast_rooms_list():
    payload = json.dumps({
        "type": "rooms_list",
        "rooms": get_live_rooms()
    })

    all_clients = set()

    # Aktif yayın odasındaki kişiler
    for room in rooms_runtime.values():
        if room.get("broadcaster"):
            all_clients.add(room["broadcaster"])

        all_clients.update(room["viewers"])

    # Oda listesini açık tutan kullanıcılar
    all_clients.update(lobby_clients)

    disconnected = set()

    for client in all_clients:
        try:
            await client.send(payload)
        except:
            disconnected.add(client)

    for client in disconnected:
        lobby_clients.discard(client)

        for room in rooms_runtime.values():
            room["viewers"].discard(client)

async def handle_create_room(ws, data):
    room_id = str(uuid.uuid4())

    title = data.get("title", "Canlı Yayın")
    broadcaster_name = data.get("broadcaster_name", "Yayıncı")

    create_room_db(room_id, title, broadcaster_name)

    rooms_runtime[room_id] = {
        "broadcaster": ws,
        "viewers": set()
    }

    ws.room_id = room_id
    ws.role = "broadcaster"
    ws.username = broadcaster_name

    add_participant_db(room_id, broadcaster_name, "broadcaster")

    await send_json(ws, {
        "type": "room_created",
        "room_id": room_id,
        "title": title,
        "broadcaster_name": broadcaster_name
    })

    await broadcast_rooms_list()
    await broadcast_viewer_count(room_id)

    print(f"Oda oluşturuldu: {title} - {room_id}")


async def handle_get_rooms(ws):
    lobby_clients.add(ws)

    await send_json(ws, {
        "type": "rooms_list",
        "rooms": get_live_rooms()
    })


async def handle_join_room(ws, data):
    room_id = data.get("room_id")
    username = data.get("username", "İzleyici")

    if not room_id or room_id not in rooms_runtime:
        await send_json(ws, {
            "type": "error",
            "message": "Oda bulunamadı veya yayın bitmiş."
        })
        return

    rooms_runtime[room_id]["viewers"].add(ws)

    ws.room_id = room_id
    ws.role = "viewer"
    ws.username = username

    add_participant_db(room_id, username, "viewer")

    await send_json(ws, {
        "type": "joined_room",
        "room_id": room_id,
        "message": "Odaya izleyici olarak katıldın."
    })

    await broadcast_rooms_list()
    await broadcast_viewer_count(room_id)

    print(f"{username} odaya katıldı: {room_id}")


async def handle_video_frame(ws, data):
    room_id = getattr(ws, "room_id", None)
    role = getattr(ws, "role", None)

    if role != "broadcaster":
        return

    if not room_id or room_id not in rooms_runtime:
        return

    frame = data.get("frame")

    if not frame:
        return

    payload = json.dumps({
        "type": "video_frame",
        "room_id": room_id,
        "frame": frame
    })

    viewers = rooms_runtime[room_id]["viewers"]
    disconnected = set()

    for viewer in viewers:
        try:
            await viewer.send(payload)
        except:
            disconnected.add(viewer)

    for viewer in disconnected:
        viewers.discard(viewer)

    if disconnected:
        await broadcast_viewer_count(room_id)
        await broadcast_rooms_list()


async def handle_chat_message(ws, data):
    room_id = getattr(ws, "room_id", None)
    username = getattr(ws, "username", "Kullanıcı")

    message = data.get("message", "").strip()

    if not room_id or room_id not in rooms_runtime:
        await send_json(ws, {
            "type": "error",
            "message": "Chat için bir odaya bağlı olmalısın."
        })
        return

    if not message:
        return

    created_at = now()

    add_chat_message_db(room_id, username, message)

    await broadcast_to_room(room_id, {
        "type": "chat_message",
        "room_id": room_id,
        "username": username,
        "message": message,
        "created_at": created_at
    })


async def cleanup_connection(ws):
    lobby_clients.discard(ws)

    room_id = getattr(ws, "room_id", None)
    role = getattr(ws, "role", None)

    if not room_id:
        return

    if room_id not in rooms_runtime:
        return

    if role == "viewer":
        rooms_runtime[room_id]["viewers"].discard(ws)
        print("İzleyici ayrıldı:", room_id)

        await broadcast_viewer_count(room_id)

    elif role == "broadcaster":
        print("Yayıncı ayrıldı, oda kapatılıyor:", room_id)

        viewers = rooms_runtime[room_id]["viewers"]

        for viewer in list(viewers):
            try:
                await send_json(viewer, {
                    "type": "stream_ended",
                    "room_id": room_id,
                    "message": "Yayın sona erdi."
                })
            except:
                pass

        finish_room_db(room_id)
        del rooms_runtime[room_id]

    await broadcast_rooms_list()


async def handler(ws):
    ws.room_id = None
    ws.role = None
    ws.username = None

    print("Yeni bağlantı geldi")

    try:
        async for message in ws:
            try:
                data = json.loads(message)
            except:
                await send_json(ws, {
                    "type": "error",
                    "message": "Geçersiz JSON formatı."
                })
                continue

            msg_type = data.get("type")

            if msg_type == "create_room":
                await handle_create_room(ws, data)

            elif msg_type == "get_rooms":
                await handle_get_rooms(ws)

            elif msg_type == "join_room":
                await handle_join_room(ws, data)

            elif msg_type == "video_frame":
                await handle_video_frame(ws, data)

            elif msg_type == "chat_message":
                await handle_chat_message(ws, data)

            else:
                await send_json(ws, {
                    "type": "error",
                    "message": f"Bilinmeyen mesaj tipi: {msg_type}"
                })

    except websockets.exceptions.ConnectionClosed:
        print("Bağlantı kapandı")

    finally:
        await cleanup_connection(ws)


async def main():
    init_db()

    print(f"WebSocket sunucusu çalışıyor: ws://{HOST}:{PORT}")

    async with websockets.serve(
        handler,
        HOST,
        PORT,
        max_size=10 * 1024 * 1024
    ):
        await asyncio.Future()


if __name__ == "__main__":
    asyncio.run(main())