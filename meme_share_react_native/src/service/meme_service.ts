import {api} from "./api_client";


export type KullaniciResponse = { success: boolean; message: string; user_id: number };
export type SimpleResponse = { success: boolean; message: string; room_code?: string | null; room_id?: number | null };
export type OdaModel = { room_id: number; room_code: string; created_by: number };
export type GonderiModel = {
    id: number;
    user_id: number;
    room_id: number;
    media_type: 'image' | 'video' | string;
    media_url: string;
    caption: string;
    uploaded_at: string;
};
export type UploadResponse = { success: boolean; message: string; media_url: string };

export const MemeService = {
    async login(username: string, password: string) {
        const form = new URLSearchParams();
        form.append('username', username);
        form.append('password', password);

        const res = await api.post('users-login.php', form.toString(), {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        });
        return res.data as KullaniciResponse;
    },

    async register(username: string, password: string) {
        const form = new URLSearchParams();
        form.append('username', username);
        form.append('password', password);

        const res = await api.post('users-register.php', form.toString(), {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        });
        return res.data as KullaniciResponse;
    },

    async getJoinedRooms(userId: number) {
        const res = await api.get('rooms-get-joined.php', { params: { user_id: userId } });
        return res.data as OdaModel[];
    },

    async createRoom(userId: number) {
        const form = new URLSearchParams();
        form.append('user_id', String(userId));

        const res = await api.post('rooms-create.php', form.toString(), {
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        });
        return res.data as SimpleResponse;
    },

    async joinRoom(userId: number, roomCode: string) {
        const res = await api.get('rooms-join.php', { params: { user_id: userId, room_code: roomCode } });
        return res.data as SimpleResponse;
    },

    async getAllMedia(roomId: number) {
        const res = await api.get('media-get-all.php', { params: { room_id: roomId } });
        return res.data as GonderiModel[];
    },

    async uploadImageBase64(payload: { room_id: number; user_id: number; base64_image: string; caption: string }) {
        const res = await api.post('media-upload-image.php', payload, {
            headers: { 'Content-Type': 'application/json' },
        });
        return res.data as UploadResponse;
    },

    async uploadVideoMultipart(params: { roomId: number; userId: number; caption: string; file: any }) {
        const form = new FormData();
        form.append('room_id', String(params.roomId));
        form.append('user_id', String(params.userId));
        form.append('caption', params.caption);
        form.append('video_file', params.file); // RN file obj

        const res = await api.post('media-upload-video.php', form, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return res.data as UploadResponse;
    },
};
