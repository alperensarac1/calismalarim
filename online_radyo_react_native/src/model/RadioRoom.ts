export type RadioRoom = {
    id: number;
    roomName: string;
    currentMusic: string | null;
    isPlaying: boolean;
    listenerCount: number;
};