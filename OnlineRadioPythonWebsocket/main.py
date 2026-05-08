import asyncio
import json
import os
import shutil
import sqlite3
import threading
import time
import tkinter as tk
from tkinter import filedialog, simpledialog, messagebox

from aiohttp import web
import websockets


DB_NAME = "radio.db"
MUSIC_DIR = "musics"

# Kendi bilgisayar IP adresini yaz.
# Android cihaz ile bilgisayar aynı Wi-Fi'da olmalı.
HOST_IP = "10.0.2.2"

HTTP_PORT = 8000
WS_PORT = 8765

connected_clients = set()

# Hangi websocket hangi odada?
client_rooms = {}


def init_db():
    os.makedirs(MUSIC_DIR, exist_ok=True)

    conn = sqlite3.connect(DB_NAME)
    cur = conn.cursor()

    cur.execute("""
    CREATE TABLE IF NOT EXISTS rooms (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        room_name TEXT NOT NULL,
        created_at TEXT DEFAULT CURRENT_TIMESTAMP
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS musics (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        file_name TEXT NOT NULL,
        created_at TEXT DEFAULT CURRENT_TIMESTAMP
    )
    """)

    cur.execute("""
    CREATE TABLE IF NOT EXISTS room_playback (
        room_id INTEGER PRIMARY KEY,
        music_id INTEGER NOT NULL,
        started_at REAL NOT NULL,
        is_playing INTEGER DEFAULT 1,
        FOREIGN KEY(room_id) REFERENCES rooms(id),
        FOREIGN KEY(music_id) REFERENCES musics(id)
    )
    """)

    conn.commit()
    conn.close()


def db_query(query, params=(), fetch=False):
    conn = sqlite3.connect(DB_NAME)
    cur = conn.cursor()
    cur.execute(query, params)

    result = None
    if fetch:
        result = cur.fetchall()

    conn.commit()
    conn.close()
    return result


def get_listener_count(room_id):
    count = 0

    for r_id in client_rooms.values():
        if r_id == room_id:
            count += 1

    return count


def get_rooms():
    rows = db_query("""
    SELECT 
        rooms.id,
        rooms.room_name,
        musics.title,
        room_playback.is_playing
    FROM rooms
    LEFT JOIN room_playback ON rooms.id = room_playback.room_id
    LEFT JOIN musics ON room_playback.music_id = musics.id
    ORDER BY rooms.id DESC
    """, fetch=True)

    result = []

    for row in rows:
        room_id = row[0]

        result.append({
            "id": room_id,
            "roomName": row[1],
            "currentMusic": row[2],
            "isPlaying": bool(row[3]) if row[3] is not None else False,
            "listenerCount": get_listener_count(room_id)
        })

    return result


def create_room(room_name):
    db_query(
        "INSERT INTO rooms(room_name) VALUES(?)",
        (room_name,)
    )


def save_music(file_path):
    original_name = os.path.basename(file_path)

    # Dosya adını çakışmasın diye zaman damgasıyla kaydediyoruz.
    safe_name = f"{int(time.time())}_{original_name}"

    target_path = os.path.join(MUSIC_DIR, safe_name)
    shutil.copy(file_path, target_path)

    title = os.path.splitext(original_name)[0]

    conn = sqlite3.connect(DB_NAME)
    cur = conn.cursor()

    cur.execute(
        "INSERT INTO musics(title, file_name) VALUES(?, ?)",
        (title, safe_name)
    )

    music_id = cur.lastrowid

    conn.commit()
    conn.close()

    return music_id, title, safe_name


def start_music_for_room(room_id, music_id):
    db_query("""
    INSERT OR REPLACE INTO room_playback(room_id, music_id, started_at, is_playing)
    VALUES(?, ?, ?, 1)
    """, (room_id, music_id, time.time()))


def get_room_playback(room_id):
    rows = db_query("""
    SELECT 
        musics.title,
        musics.file_name,
        room_playback.started_at,
        room_playback.is_playing
    FROM room_playback
    INNER JOIN musics ON room_playback.music_id = musics.id
    WHERE room_playback.room_id = ?
    """, (room_id,), fetch=True)

    if not rows:
        return None

    title, file_name, started_at, is_playing = rows[0]

    position_seconds = max(0, time.time() - started_at)

    return {
        "title": title,
        "musicUrl": f"http://{HOST_IP}:{HTTP_PORT}/musics/{file_name}",
        "positionSeconds": position_seconds,
        "isPlaying": bool(is_playing),
        "serverTime": time.time()
    }


async def broadcast(message):
    disconnected = []

    for client in connected_clients:
        try:
            await client.send(json.dumps(message))
        except:
            disconnected.append(client)

    for client in disconnected:
        connected_clients.discard(client)
        client_rooms.pop(client, None)


async def broadcast_rooms():
    await broadcast({
        "type": "ROOM_LIST",
        "rooms": get_rooms()
    })


async def websocket_handler(websocket):
    connected_clients.add(websocket)

    try:
        async for raw_message in websocket:
            data = json.loads(raw_message)
            msg_type = data.get("type")

            if msg_type == "GET_ROOMS":
                await websocket.send(json.dumps({
                    "type": "ROOM_LIST",
                    "rooms": get_rooms()
                }))

            elif msg_type == "JOIN_ROOM":
                room_id = int(data.get("roomId"))
                client_rooms[websocket] = room_id

                await broadcast_rooms()

                playback = get_room_playback(room_id)

                if playback:
                    await websocket.send(json.dumps({
                        "type": "PLAYBACK_STATE",
                        "roomId": room_id,
                        **playback
                    }))
                else:
                    await websocket.send(json.dumps({
                        "type": "NO_MUSIC",
                        "roomId": room_id,
                        "message": "Bu odada şu an müzik yok."
                    }))

            elif msg_type == "SYNC_REQUEST":
                room_id = int(data.get("roomId"))
                playback = get_room_playback(room_id)

                if playback:
                    await websocket.send(json.dumps({
                        "type": "PLAYBACK_STATE",
                        "roomId": room_id,
                        **playback
                    }))

    finally:
        connected_clients.discard(websocket)
        client_rooms.pop(websocket, None)
        await broadcast_rooms()


async def http_server():
    app = web.Application()
    app.router.add_static("/musics/", path=MUSIC_DIR, name="musics")

    runner = web.AppRunner(app)
    await runner.setup()

    site = web.TCPSite(runner, "0.0.0.0", HTTP_PORT)
    await site.start()


async def websocket_server():
    async with websockets.serve(websocket_handler, "0.0.0.0", WS_PORT):
        await asyncio.Future()


async def start_servers():
    await http_server()
    await websocket_server()


def run_server_loop():
    asyncio.run(start_servers())


def run_async_task(coro):
    asyncio.run(coro)


class RadioAdminApp:
    def __init__(self, root):
        self.root = root
        self.root.title("SyncRadio Admin Panel")
        self.root.geometry("750x500")

        self.selected_room_id = None

        self.title_label = tk.Label(
            root,
            text="SyncRadio Python Admin",
            font=("Arial", 20, "bold")
        )
        self.title_label.pack(pady=10)

        self.info_label = tk.Label(
            root,
            text=f"HTTP: http://{HOST_IP}:{HTTP_PORT}  |  WS: ws://{HOST_IP}:{WS_PORT}",
            font=("Arial", 10)
        )
        self.info_label.pack(pady=5)

        self.room_list = tk.Listbox(root, height=15, font=("Arial", 12))
        self.room_list.pack(fill=tk.BOTH, expand=True, padx=20, pady=10)
        self.room_list.bind("<<ListboxSelect>>", self.on_room_selected)

        self.selected_label = tk.Label(
            root,
            text="Seçili oda: yok",
            font=("Arial", 12, "bold")
        )
        self.selected_label.pack(pady=5)

        button_frame = tk.Frame(root)
        button_frame.pack(pady=15)

        tk.Button(
            button_frame,
            text="Oda Oluştur",
            width=18,
            command=self.create_room_ui
        ).grid(row=0, column=0, padx=8)

        tk.Button(
            button_frame,
            text="MP3 Seç ve Çal",
            width=18,
            command=self.select_music_ui
        ).grid(row=0, column=1, padx=8)

        tk.Button(
            button_frame,
            text="Yenile",
            width=18,
            command=self.load_rooms
        ).grid(row=0, column=2, padx=8)

        self.load_rooms()

    def load_rooms(self):
        self.room_list.delete(0, tk.END)

        rooms = get_rooms()

        for room in rooms:
            music = room["currentMusic"] or "Müzik yok"
            listener_count = room["listenerCount"]

            text = f'{room["id"]} - {room["roomName"]} | {music} | Dinleyici: {listener_count}'
            self.room_list.insert(tk.END, text)

    def on_room_selected(self, event):
        selected = self.room_list.curselection()

        if not selected:
            return

        selected_text = self.room_list.get(selected[0])
        room_id = int(selected_text.split(" - ")[0])
        self.selected_room_id = room_id

        self.selected_label.config(
            text=f"Seçili oda ID: {room_id}"
        )

    def create_room_ui(self):
        room_name = simpledialog.askstring(
            "Oda Oluştur",
            "Oda adını gir:"
        )

        if not room_name:
            return

        create_room(room_name)

        run_async_task(broadcast_rooms())

        self.load_rooms()

    def select_music_ui(self):
        if self.selected_room_id is None:
            messagebox.showwarning(
                "Uyarı",
                "Önce listeden bir oda seçmelisin."
            )
            return

        file_path = filedialog.askopenfilename(
            title="MP3 dosyası seç",
            filetypes=[("MP3 files", "*.mp3")]
        )

        if not file_path:
            return

        music_id, title, file_name = save_music(file_path)

        start_music_for_room(
            room_id=self.selected_room_id,
            music_id=music_id
        )

        playback_message = {
            "type": "PLAYBACK_STATE",
            "roomId": self.selected_room_id,
            "title": title,
            "musicUrl": f"http://{HOST_IP}:{HTTP_PORT}/musics/{file_name}",
            "positionSeconds": 0,
            "isPlaying": True,
            "serverTime": time.time()
        }

        run_async_task(broadcast(playback_message))
        run_async_task(broadcast_rooms())

        self.load_rooms()

        messagebox.showinfo(
            "Başlatıldı",
            f"{title} seçili odada yayına başladı."
        )


if __name__ == "__main__":
    init_db()

    server_thread = threading.Thread(
        target=run_server_loop,
        daemon=True
    )
    server_thread.start()

    root = tk.Tk()
    app = RadioAdminApp(root)
    root.mainloop()