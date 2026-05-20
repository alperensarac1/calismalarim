type LiveSocketCallbacks = {
    onConnected?: () => void;
    onMessage?: (message: string) => void;
    onError?: (error: string) => void;
    onDisconnected?: () => void;
};

export class LiveSocketService {
    private socket: WebSocket | null = null;
    private readonly serverUrl: string;
    private readonly callbacks: LiveSocketCallbacks;

    constructor(serverUrl: string, callbacks: LiveSocketCallbacks) {
        this.serverUrl = serverUrl;
        this.callbacks = callbacks;
    }

    connect() {
        this.socket = new WebSocket(this.serverUrl);

        this.socket.onopen = () => {
            this.callbacks.onConnected?.();
        };

        this.socket.onmessage = event => {
            if (typeof event.data === "string") {
                this.callbacks.onMessage?.(event.data);
            }
        };

        this.socket.onerror = () => {
            this.callbacks.onError?.("WebSocket bağlantı hatası");
        };

        this.socket.onclose = () => {
            this.callbacks.onDisconnected?.();
        };
    }

    sendJson(data: object) {
        if (!this.socket) return;

        if (this.socket.readyState !== WebSocket.OPEN) return;

        this.socket.send(JSON.stringify(data));
    }

    disconnect() {
        if (!this.socket) return;

        this.socket.close();
        this.socket = null;
    }
}