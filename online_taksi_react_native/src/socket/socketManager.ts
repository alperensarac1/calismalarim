import { Constants } from "../core/constants";
import { SessionManager } from "../core/sessionManager";

export class SocketManager {
    private socket: WebSocket | null = null;

    onConnected?: () => void;
    onDisconnected?: () => void;
    onMessage?: (message: string) => void;
    onError?: (error: string) => void;

    async connect() {
        const token = await SessionManager.getToken();

        if (!token) {
            this.onError?.("Token bulunamadı");
            return;
        }

        this.socket = new WebSocket(`${Constants.WS_URL}?token=${token}`);

        this.socket.onopen = () => {
            this.onConnected?.();
        };

        this.socket.onmessage = (event) => {
            this.onMessage?.(String(event.data));
        };

        this.socket.onerror = () => {
            this.onError?.("Socket hatası");
        };

        this.socket.onclose = () => {
            this.onDisconnected?.();
        };
    }

    sendPing() {
        this.socket?.send(
            JSON.stringify({
                event: "PING",
                data: {},
            })
        );
    }

    disconnect() {
        this.socket?.close();
        this.socket = null;
        this.onDisconnected?.();
    }
}