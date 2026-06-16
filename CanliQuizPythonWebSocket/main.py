# -*- coding: utf-8 -*-

"""
CANLI QUIZ WEBSOCKET SERVER

Bu dosya Android uygulamasından gelen WebSocket bağlantılarını yönetir.

Sunucunun görevleri:
1. Oda oluşturmak
2. Kullanıcıları odalara almak
3. Oda sahibinin soru eklemesini sağlamak
4. Quizi başlatmak
5. Soruları herkese sırayla göndermek
6. Cevapları almak
7. Hızlı doğru cevaplara daha fazla puan vermek
8. Tüm sorular bitince ilk 3 kazananı herkese göndermek

Çalıştırmak için:
    python server.py

Gerekli kütüphane:
    pip install websockets
"""

import asyncio
import json
import random
import time
from typing import Dict, List, Any, Optional

import websockets


# ============================================================
# GLOBAL ODA LİSTESİ
# ============================================================
# Tüm odaları burada tutacağız.
# Gerçek projede bu veriler Redis, PostgreSQL veya MySQL gibi kalıcı sistemlere alınabilir.
rooms: Dict[str, Dict[str, Any]] = {}


# ============================================================
# YARDIMCI FONKSİYONLAR
# ============================================================

def generate_room_code() -> str:
    """
    6 haneli rastgele oda kodu üretir.

    Örnek:
        483921
        105934
        774201

    Eğer üretilen kod daha önce oluşturulmuşsa yeni kod üretir.
    """
    while True:
        code = str(random.randint(100000, 999999))

        if code not in rooms:
            return code


def create_response(message_type: str, **kwargs) -> str:
    """
    Android tarafına gönderilecek standart JSON cevabını üretir.

    Kullanım:
        create_response("room_created", room_code="123456")

    Çıktı:
        {
            "type": "room_created",
            "room_code": "123456"
        }
    """
    data = {
        "type": message_type
    }

    data.update(kwargs)

    return json.dumps(data, ensure_ascii=False)


async def send_to_socket(socket, message_type: str, **kwargs):
    """
    Tek bir kullanıcıya mesaj gönderir.
    """
    try:
        await socket.send(create_response(message_type, **kwargs))
    except Exception as e:
        print("Mesaj gönderilemedi:", e)


async def broadcast_to_room(room_code: str, message_type: str, **kwargs):
    """
    Belirli bir odadaki tüm kullanıcılara mesaj gönderir.

    Örnek:
        broadcast_to_room("123456", "player_joined", username="Alp")
    """
    room = rooms.get(room_code)

    if room is None:
        return

    disconnected_users = []

    for username, player_data in room["players"].items():
        socket = player_data.get("socket")

        try:
            await socket.send(create_response(message_type, **kwargs))
        except Exception:
            disconnected_users.append(username)

    # Kopan kullanıcıları oda listesinden temizliyoruz.
    for username in disconnected_users:
        room["players"].pop(username, None)


def get_scoreboard(room_code: str) -> List[Dict[str, Any]]:
    """
    Odanın puan tablosunu yüksek puandan düşük puana göre döndürür.
    """
    room = rooms.get(room_code)

    if room is None:
        return []

    scoreboard = []

    for username, player_data in room["players"].items():
        scoreboard.append({
            "username": username,
            "score": player_data.get("score", 0)
        })

    scoreboard.sort(key=lambda item: item["score"], reverse=True)

    return scoreboard


def get_top_three(room_code: str) -> List[Dict[str, Any]]:
    """
    İlk 3 oyuncuyu döndürür.
    """
    scoreboard = get_scoreboard(room_code)
    return scoreboard[:3]


# ============================================================
# ODA OLUŞTURMA
# ============================================================

async def handle_create_room(socket, data: Dict[str, Any]):
    """
    Oda oluşturma işlemi.

    Android'den beklenen mesaj:

    {
        "type": "create_room",
        "username": "Alp",
        "question_time": 20
    }
    """

    username = data.get("username")
    question_time = int(data.get("question_time", 20))

    if not username:
        await send_to_socket(socket, "error", message="Kullanıcı adı boş olamaz.")
        return

    if question_time < 5:
        await send_to_socket(socket, "error", message="Soru süresi en az 5 saniye olmalıdır.")
        return

    room_code = generate_room_code()

    rooms[room_code] = {
        # Odanın sahibi yani quizi yöneten kullanıcı
        "owner": username,

        # Soru başına süre
        "question_time": question_time,

        # Odaya eklenen sorular burada tutulacak
        "questions": [],

        # Odaya bağlı oyuncular
        "players": {
            username: {
                "socket": socket,
                "score": 0,
                "is_owner": True
            }
        },

        # Şu an hangi sorudayız?
        "current_question_index": -1,

        # Aktif soru başladı mı?
        "is_quiz_started": False,

        # Aktif soru başlangıç zamanı
        "question_started_at": None,

        # Aktif soruya kimler cevap verdi?
        "answered_users": set(),

        # Quiz döngüsünü tekrar başlatmayı önlemek için kullanılacak
        "quiz_task": None
    }

    print(f"Oda oluşturuldu: {room_code} - Sahip: {username}")

    await send_to_socket(
        socket,
        "room_created",
        room_code=room_code,
        username=username,
        question_time=question_time,
        message="Oda başarıyla oluşturuldu."
    )


# ============================================================
# ODAYA KATILMA
# ============================================================

async def handle_join_room(socket, data: Dict[str, Any]):
    """
    Odaya katılma işlemi.

    Android'den beklenen mesaj:

    {
        "type": "join_room",
        "room_code": "123456",
        "username": "Mehmet"
    }
    """

    room_code = data.get("room_code")
    username = data.get("username")

    if not room_code or not username:
        await send_to_socket(socket, "error", message="Oda kodu ve kullanıcı adı zorunludur.")
        return

    room = rooms.get(room_code)

    if room is None:
        await send_to_socket(socket, "error", message="Böyle bir oda bulunamadı.")
        return

    if room["is_quiz_started"]:
        await send_to_socket(socket, "error", message="Quiz başladıktan sonra odaya girilemez.")
        return

    if username in room["players"]:
        await send_to_socket(socket, "error", message="Bu kullanıcı adı odada zaten kullanılıyor.")
        return

    room["players"][username] = {
        "socket": socket,
        "score": 0,
        "is_owner": False
    }

    print(f"{username} odaya katıldı: {room_code}")

    await send_to_socket(
        socket,
        "room_joined",
        room_code=room_code,
        username=username,
        question_time=room["question_time"],
        owner=room["owner"],
        message="Odaya başarıyla katıldın."
    )

    await broadcast_to_room(
        room_code,
        "player_list_updated",
        players=list(room["players"].keys()),
        scoreboard=get_scoreboard(room_code)
    )


# ============================================================
# SORU EKLEME
# ============================================================

async def handle_add_question(socket, data: Dict[str, Any]):
    """
    Oda sahibinin soru eklemesini sağlar.

    Android'den beklenen mesaj:

    {
        "type": "add_question",
        "room_code": "123456",
        "question_text": "Python nedir?",
        "options": ["Dil", "Veritabanı", "Editör", "İşletim sistemi"],
        "correct_index": 0
    }
    """

    room_code = data.get("room_code")
    question_text = data.get("question_text")
    options = data.get("options", [])
    correct_index = data.get("correct_index")

    room = rooms.get(room_code)

    if room is None:
        await send_to_socket(socket, "error", message="Oda bulunamadı.")
        return

    # Sadece oda sahibi soru ekleyebilsin diye socket kontrolü yapıyoruz.
    owner_username = room["owner"]
    owner_socket = room["players"][owner_username]["socket"]

    if socket != owner_socket:
        await send_to_socket(socket, "error", message="Sadece oda sahibi soru ekleyebilir.")
        return

    if room["is_quiz_started"]:
        await send_to_socket(socket, "error", message="Quiz başladıktan sonra soru eklenemez.")
        return

    if not question_text:
        await send_to_socket(socket, "error", message="Soru metni boş olamaz.")
        return

    if not isinstance(options, list) or len(options) < 2:
        await send_to_socket(socket, "error", message="En az 2 seçenek olmalıdır.")
        return

    if correct_index is None:
        await send_to_socket(socket, "error", message="Doğru cevap index değeri gönderilmelidir.")
        return

    correct_index = int(correct_index)

    if correct_index < 0 or correct_index >= len(options):
        await send_to_socket(socket, "error", message="Doğru cevap index değeri geçersiz.")
        return

    question = {
        "question_text": question_text,
        "options": options,
        "correct_index": correct_index
    }

    room["questions"].append(question)

    await send_to_socket(
        socket,
        "question_added",
        room_code=room_code,
        question_count=len(room["questions"]),
        message="Soru başarıyla eklendi."
    )

    await broadcast_to_room(
        room_code,
        "room_question_count_updated",
        question_count=len(room["questions"])
    )


# ============================================================
# QUIZ BAŞLATMA
# ============================================================

async def handle_start_quiz(socket, data: Dict[str, Any]):
    """
    Quiz başlatma işlemi.

    Android'den beklenen mesaj:

    {
        "type": "start_quiz",
        "room_code": "123456"
    }
    """

    room_code = data.get("room_code")
    room = rooms.get(room_code)

    if room is None:
        await send_to_socket(socket, "error", message="Oda bulunamadı.")
        return

    owner_username = room["owner"]
    owner_socket = room["players"][owner_username]["socket"]

    if socket != owner_socket:
        await send_to_socket(socket, "error", message="Sadece oda sahibi quizi başlatabilir.")
        return

    if len(room["questions"]) == 0:
        await send_to_socket(socket, "error", message="Quiz başlatmak için en az 1 soru eklemelisin.")
        return

    if room["is_quiz_started"]:
        await send_to_socket(socket, "error", message="Quiz zaten başladı.")
        return

    room["is_quiz_started"] = True
    room["current_question_index"] = -1

    await broadcast_to_room(
        room_code,
        "quiz_started",
        message="Quiz başladı."
    )

    # Quiz akışını ayrı bir async task olarak başlatıyoruz.
    room["quiz_task"] = asyncio.create_task(run_quiz(room_code))


async def run_quiz(room_code: str):
    """
    Quiz akışını yöneten ana döngü.

    Akış:
    1. Sonraki soruya geç
    2. Soruyu herkese gönder
    3. Soru süresi kadar bekle
    4. Süre bitti mesajı gönder
    5. 5 saniye bekle
    6. Yeni soruya geç
    7. Sorular bitince kazananları gönder
    """

    room = rooms.get(room_code)

    if room is None:
        return

    total_questions = len(room["questions"])

    for index in range(total_questions):
        room["current_question_index"] = index
        room["question_started_at"] = time.time()
        room["answered_users"] = set()

        question = room["questions"][index]

        # Android'e doğru cevabı göndermiyoruz.
        # Sadece soru ve seçenekleri gönderiyoruz.
        await broadcast_to_room(
            room_code,
            "new_question",
            question_number=index + 1,
            total_questions=total_questions,
            question_text=question["question_text"],
            options=question["options"],
            question_time=room["question_time"],
            scoreboard=get_scoreboard(room_code)
        )

        # Soru süresi kadar bekle.
        await asyncio.sleep(room["question_time"])

        # Süre bitti mesajı gönder.
        await broadcast_to_room(
            room_code,
            "time_up",
            message="Süre bitti.",
            correct_index=question["correct_index"],
            scoreboard=get_scoreboard(room_code)
        )

        # Yeni soruya geçmeden önce 5 saniye bekle.
        await asyncio.sleep(5)

    # Tüm sorular bitti.
    winners = get_top_three(room_code)

    await broadcast_to_room(
        room_code,
        "quiz_finished",
        message="Quiz bitti. İlk üç kişi kazandı.",
        winners=winners,
        scoreboard=get_scoreboard(room_code)
    )

    room["is_quiz_started"] = False


# ============================================================
# CEVAP GÖNDERME
# ============================================================

async def handle_submit_answer(socket, data: Dict[str, Any]):
    """
    Kullanıcının cevap göndermesini sağlar.

    Android'den beklenen mesaj:

    {
        "type": "submit_answer",
        "room_code": "123456",
        "username": "Mehmet",
        "answer_index": 1
    }
    """

    room_code = data.get("room_code")
    username = data.get("username")
    answer_index = data.get("answer_index")

    room = rooms.get(room_code)

    if room is None:
        await send_to_socket(socket, "error", message="Oda bulunamadı.")
        return

    if not room["is_quiz_started"]:
        await send_to_socket(socket, "error", message="Quiz henüz başlamadı.")
        return

    if username not in room["players"]:
        await send_to_socket(socket, "error", message="Bu kullanıcı odada yok.")
        return

    # Bir kullanıcı aynı soruya sadece bir defa cevap verebilsin.
    if username in room["answered_users"]:
        await send_to_socket(socket, "answer_rejected", message="Bu soruya zaten cevap verdin.")
        return

    current_index = room["current_question_index"]

    if current_index < 0 or current_index >= len(room["questions"]):
        await send_to_socket(socket, "error", message="Aktif soru bulunamadı.")
        return

    question = room["questions"][current_index]

    answer_index = int(answer_index)

    # Kullanıcı cevap verdi olarak işaretlenir.
    room["answered_users"].add(username)

    now = time.time()
    question_started_at = room["question_started_at"]
    elapsed_time = now - question_started_at

    total_time = room["question_time"]
    remaining_time = max(total_time - elapsed_time, 0)

    is_correct = answer_index == question["correct_index"]

    earned_score = 0

    if is_correct:
        # Hızlı cevap daha yüksek puan verir.
        earned_score = int((remaining_time / total_time) * 1000)

        # Çok geç doğru cevap verilirse bile minimum 1 puan verelim.
        earned_score = max(earned_score, 1)

        room["players"][username]["score"] += earned_score

    await send_to_socket(
        socket,
        "answer_result",
        is_correct=is_correct,
        earned_score=earned_score,
        total_score=room["players"][username]["score"]
    )

    # Her cevaptan sonra tüm oyunculara güncel puan tablosu gönderilir.
    await broadcast_to_room(
        room_code,
        "scoreboard_updated",
        scoreboard=get_scoreboard(room_code)
    )


# ============================================================
# BAĞLANTI KOPUNCA TEMİZLEME
# ============================================================

async def remove_disconnected_socket(socket):
    """
    Kullanıcı bağlantısı kopunca onu odadan temizler.

    Not:
    Eğer oda sahibi çıkarsa şu an oda tamamen silinmiyor.
    İstersen sonraki aşamada:
        - oda sahibi çıkarsa oda kapansın
        - ya da başka biri oda sahibi olsun
    gibi mantıklar ekleyebiliriz.
    """

    for room_code, room in list(rooms.items()):
        disconnected_username: Optional[str] = None

        for username, player_data in room["players"].items():
            if player_data.get("socket") == socket:
                disconnected_username = username
                break

        if disconnected_username:
            room["players"].pop(disconnected_username, None)

            await broadcast_to_room(
                room_code,
                "player_list_updated",
                players=list(room["players"].keys()),
                scoreboard=get_scoreboard(room_code)
            )

            print(f"{disconnected_username} odadan ayrıldı: {room_code}")

            # Eğer oda boş kaldıysa odayı silelim.
            if len(room["players"]) == 0:
                rooms.pop(room_code, None)
                print(f"Oda silindi çünkü boş kaldı: {room_code}")

            break


# ============================================================
# GELEN MESAJLARI YÖNETEN ANA ROUTER
# ============================================================

async def handle_message(socket, raw_message: str):
    """
    Android'den gelen her mesaj önce buraya düşer.

    Mesajın type alanına göre ilgili fonksiyona yönlendiririz.
    """

    try:
        data = json.loads(raw_message)
    except json.JSONDecodeError:
        await send_to_socket(socket, "error", message="Geçersiz JSON formatı.")
        return

    message_type = data.get("type")

    if message_type == "create_room":
        await handle_create_room(socket, data)

    elif message_type == "join_room":
        await handle_join_room(socket, data)

    elif message_type == "add_question":
        await handle_add_question(socket, data)

    elif message_type == "start_quiz":
        await handle_start_quiz(socket, data)

    elif message_type == "submit_answer":
        await handle_submit_answer(socket, data)

    else:
        await send_to_socket(
            socket,
            "error",
            message=f"Bilinmeyen mesaj tipi: {message_type}"
        )


# ============================================================
# HER WEBSOCKET BAĞLANTISINI YÖNETEN FONKSİYON
# ============================================================

async def websocket_handler(socket):
    """
    Her Android cihaz WebSocket'e bağlandığında bu fonksiyon çalışır.

    Bu fonksiyon bağlantı açık kaldığı sürece Android'den gelen mesajları dinler.
    """

    print("Yeni bağlantı geldi.")

    try:
        async for message in socket:
            print("Gelen mesaj:", message)
            await handle_message(socket, message)

    except websockets.exceptions.ConnectionClosed:
        print("Bağlantı kapandı.")

    except Exception as e:
        print("Beklenmeyen hata:", e)

    finally:
        await remove_disconnected_socket(socket)


# ============================================================
# SERVER BAŞLATMA
# ============================================================

async def main():
    """
    WebSocket sunucusunu başlatır.

    host="0.0.0.0"
        Aynı ağdaki Android cihazlar bu sunucuya bağlanabilsin diye kullanıyoruz.

    port=8765
        Android tarafında ws://IP_ADRESI:8765 şeklinde bağlanacağız.
    """

    host = "0.0.0.0"
    port = 8765

    print(f"Canlı Quiz WebSocket sunucusu çalışıyor: ws://{host}:{port}")

    async with websockets.serve(websocket_handler, host, port):
        await asyncio.Future()


if __name__ == "__main__":
    asyncio.run(main())