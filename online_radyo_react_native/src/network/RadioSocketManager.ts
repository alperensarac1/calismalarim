type MessageCallback = (message: string) => void;
type ErrorCallback = (error: string) => void;
type ConnectedCallback = () => void;

class RadioSocketManager {
    private static instance: RadioSocketManager;

    private socket: WebSocket | null = null;

    // Python server çalışan bilgisayar IP adresi
    private readonly serverUrl = "ws://192.168.1.10:8765";

    public onConnected?: ConnectedCallback;
    public onMessage?: MessageCallback;
    public onError?: ErrorCallback;

    private constructor() {}

    static getInstance(): RadioSocketManager {
        if (!RadioSocketManager.instance) {
            RadioSocketManager.instance = new RadioSocketManager();
        }

        return RadioSocketManager.instance;
    }

    connect() {
        if (this.socket) return;

        this.socket = new WebSocket(this.serverUrl);

        this.socket.onopen = () => {
            this.onConnected?.();
        };

        this.socket.onmessage = event => {
            this.onMessage?.(String(event.data));
        };

        this.socket.onerror = () => {
            this.onError?.("WebSocket bağlantı hatası");
        };

        this.socket.onclose = () => {
            this.socket = null;
        };
    }

    private send(data: object) {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
            return;
        }

        this.socket.send(JSON.stringify(data));
    }

    getRooms() {
        this.send({
            type: "GET_ROOMS",
        });
    }

    joinRoom(roomId: number) {
        this.send({
            type: "JOIN_ROOM",
            roomId,
        });
    }

    requestSync(roomId: number) {
        this.send({
            type: "SYNC_REQUEST",
            roomId,
        });
    }

    close() {
        this.socket?.close();
        this.socket = null;
    }
}

export default RadioSocketManager.getInstance();