import { SocketListener } from "../models/types";

/*
  Expo Go içinde WebSocket doğrudan desteklenir.
  Ekstra kütüphane kurmamıza gerek yok.

  Android Emulator:
  ws://10.0.2.2:8765

  iOS Simulator:
  ws://127.0.0.1:8765

  Fiziksel telefon:
  ws://BILGISAYAR_IP_ADRESI:8765
*/

const SERVER_URL = "ws://10.0.2.2:8765";

class WebSocketManagerClass {
    private socket: WebSocket | null = null;
    private listener: SocketListener | null = null;
    private connected = false;

    setListener(listener: SocketListener) {
        this.listener = listener;
    }

    removeListener(listener: SocketListener) {
        if (this.listener === listener) {
            this.listener = null;
        }
    }

    isConnected(): boolean {
        return this.connected && this.socket !== null;
    }

    connect() {
        if (this.isConnected()) {
            this.listener?.onOpen?.();
            return;
        }

        this.socket = new WebSocket(SERVER_URL);

        this.socket.onopen = () => {
            this.connected = true;
            this.listener?.onOpen?.();
        };

        this.socket.onmessage = (event) => {
            this.listener?.onMessage?.(String(event.data));
        };

        this.socket.onerror = () => {
            this.connected = false;
            this.listener?.onError?.("WebSocket bağlantı hatası");
        };

        this.socket.onclose = () => {
            this.connected = false;
            this.listener?.onClose?.();
        };
    }

    send(message: string) {
        if (!this.socket || !this.connected) {
            this.listener?.onError?.("WebSocket bağlı değil.");
            return;
        }

        this.socket.send(message);
    }

    disconnect() {
        this.connected = false;

        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
    }
}

export const WebSocketManager = new WebSocketManagerClass();