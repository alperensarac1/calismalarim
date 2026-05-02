# app/websocket/connection_manager.py

from fastapi import WebSocket
from typing import Dict, Any


class ConnectionManager:
    def __init__(self):
        # user_id -> websocket
        self.active_connections: Dict[int, WebSocket] = {}

        # user_id -> role
        self.user_roles: Dict[int, str] = {}

    async def connect(self, user_id: int, role: str, websocket: WebSocket):
        # Eğer aynı kullanıcı yeniden bağlandıysa eski bağlantıyı ez
        self.active_connections[user_id] = websocket
        self.user_roles[user_id] = role

    def disconnect(self, user_id: int):
        if user_id in self.active_connections:
            del self.active_connections[user_id]

        if user_id in self.user_roles:
            del self.user_roles[user_id]

    async def send_to_user(self, user_id: int, event: str, data: dict):
        websocket = self.active_connections.get(user_id)
        if websocket:
            await websocket.send_json({
                "event": event,
                "data": data
            })

    async def broadcast_to_role(self, role: str, event: str, data: dict):
        for user_id, websocket in self.active_connections.items():
            if self.user_roles.get(user_id) == role:
                await websocket.send_json({
                    "event": event,
                    "data": data
                })

    async def broadcast_to_online_drivers(self, event: str, data: dict):
        for user_id, websocket in self.active_connections.items():
            if self.user_roles.get(user_id) == "driver":
                await websocket.send_json({
                    "event": event,
                    "data": data
                })

    def is_user_online(self, user_id: int) -> bool:
        return user_id in self.active_connections


manager = ConnectionManager()