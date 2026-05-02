# app/websocket/ws_routes.py

from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from sqlalchemy.orm import Session

from app.core.database import SessionLocal
from app.core.security import decode_access_token
from app.models.user import User
from app.websocket.connection_manager import manager
from app.websocket.events import WsEvent

router = APIRouter()


@router.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()

    token = websocket.query_params.get("token")
    if not token:
        await websocket.send_json({
            "event": WsEvent.AUTH_ERROR,
            "data": {"message": "Token zorunludur."}
        })
        await websocket.close()
        return

    payload = decode_access_token(token)
    if not payload:
        await websocket.send_json({
            "event": WsEvent.AUTH_ERROR,
            "data": {"message": "Geçersiz token."}
        })
        await websocket.close()
        return

    user_id = payload.get("user_id")
    role = payload.get("role")

    if not user_id or not role:
        await websocket.send_json({
            "event": WsEvent.AUTH_ERROR,
            "data": {"message": "Token içeriği eksik."}
        })
        await websocket.close()
        return

    db: Session = SessionLocal()

    try:
        user = db.query(User).filter(User.id == user_id).first()
        if not user:
            await websocket.send_json({
                "event": WsEvent.AUTH_ERROR,
                "data": {"message": "Kullanıcı bulunamadı."}
            })
            await websocket.close()
            return

        await manager.connect(user_id=user.id, role=user.role, websocket=websocket)

        await websocket.send_json({
            "event": WsEvent.AUTH_SUCCESS,
            "data": {
                "user_id": user.id,
                "role": user.role,
                "message": "WebSocket bağlantısı başarılı."
            }
        })

        while True:
            message = await websocket.receive_json()

            event = message.get("event")
            data = message.get("data", {})

            if event == WsEvent.PING:
                await websocket.send_json({
                    "event": WsEvent.PONG,
                    "data": {"message": "pong"}
                })

            else:
                await websocket.send_json({
                    "event": WsEvent.INFO,
                    "data": {
                        "message": f"Bilinmeyen veya henüz işlenmeyen event: {event}",
                        "received_data": data
                    }
                })

    except WebSocketDisconnect:
        manager.disconnect(user_id)

    except Exception as e:
        manager.disconnect(user_id)
        try:
            await websocket.send_json({
                "event": WsEvent.ERROR,
                "data": {"message": str(e)}
            })
        except:
            pass

    finally:
        db.close()