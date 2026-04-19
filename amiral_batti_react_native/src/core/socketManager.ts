import { AppConfig } from "../config/appConfig";

export interface SocketEventListener {
    onConnected(): void;
    onDisconnected(): void;
    onMessage(message: string): void;
    onError(errorMessage: string): void;
}

class SocketManager {
    private static _instance: SocketManager;
    private socket: WebSocket | null = null;
    private listener: SocketEventListener | null = null;
    private connected = false;

    private constructor() {}

    static get instance(): SocketManager {
        if (!SocketManager._instance) {
            SocketManager._instance = new SocketManager();
        }
        return SocketManager._instance;
    }

    setListener(listener: SocketEventListener | null) {
        this.listener = listener;
    }

    clearListener(owner: SocketEventListener) {
        if (this.listener === owner) {
            this.listener = null;
        }
    }

    isConnected() {
        return this.connected;
    }

    connect() {
        if (this.connected && this.socket) {
            this.listener?.onConnected();
            return;
        }

        try {
            this.socket = new WebSocket(AppConfig.webSocketUrl);

            this.socket.onopen = () => {
                this.connected = true;
                this.listener?.onConnected();
            };

            this.socket.onmessage = (event) => {
                this.listener?.onMessage(String(event.data));
            };

            this.socket.onerror = () => {
                this.connected = false;
                this.listener?.onError("WebSocket bağlantı hatası");
            };

            this.socket.onclose = () => {
                this.connected = false;
                this.listener?.onDisconnected();
            };
        } catch (error) {
            this.connected = false;
            this.listener?.onError(
                error instanceof Error ? error.message : "Bilinmeyen bağlantı hatası"
            );
        }
    }

    sendText(text: string) {
        if (this.socket && this.connected) {
            this.socket.send(text);
        } else {
            this.listener?.onError("Socket bağlı değil");
        }
    }

    sendMap(data: Record<string, unknown>) {
        this.sendText(JSON.stringify(data));
    }

    disconnect() {
        this.socket?.close();
        this.socket = null;
        this.connected = false;
    }
}

export const socketManager = SocketManager.instance;
