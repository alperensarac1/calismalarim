import React, { createContext, useContext, useMemo, useReducer } from 'react';
import {MediaState} from "./media_types";
import {initialMediaState, mediaReducer} from "./media_reducer";
import {MemeService} from "../service/meme_service";


type UploadImageParams = {
    roomId: number;
    userId: number;
    caption: string;
    base64: string; // data:... olmadan saf base64 bekliyoruz
};

type UploadVideoParams = {
    roomId: number;
    userId: number;
    caption: string;
    file: {
        uri: string;
        name: string;
        type: string; // video/mp4
    };
};

type MediaContextValue = {
    state: MediaState;
    fetchPosts: (roomId: number) => Promise<void>;
    uploadImage: (p: UploadImageParams) => Promise<{ ok: boolean; message?: string }>;
    uploadVideo: (p: UploadVideoParams) => Promise<{ ok: boolean; message?: string }>;
    clearError: () => void;
};

const MediaContext = createContext<MediaContextValue | null>(null);

export function MediaProvider({ children }: { children: React.ReactNode }) {
    const [state, dispatch] = useReducer(mediaReducer, initialMediaState);

    const value = useMemo<MediaContextValue>(() => {
        return {
            state,

            async fetchPosts(roomId) {
                dispatch({ type: 'MEDIA_LIST_REQUEST' });
                try {
                    const posts = await MemeService.getAllMedia(roomId);
                    dispatch({ type: 'MEDIA_LIST_SUCCESS', payload: posts });
                } catch (e: any) {
                    dispatch({ type: 'MEDIA_LIST_FAIL', payload: e?.message || 'Gönderiler alınamadı' });
                }
            },

            async uploadImage({ roomId, userId, caption, base64 }) {
                dispatch({ type: 'MEDIA_UPLOAD_REQUEST' });
                try {
                    const res = await MemeService.uploadImageBase64({
                        room_id: roomId,
                        user_id: userId,
                        base64_image: base64,
                        caption,
                    });

                    if (res.success) {
                        dispatch({ type: 'MEDIA_UPLOAD_SUCCESS', payload: res });
                        return { ok: true };
                    } else {
                        dispatch({ type: 'MEDIA_UPLOAD_FAIL', payload: res.message || 'Görsel yükleme hatası' });
                        return { ok: false, message: res.message || 'Görsel yükleme hatası' };
                    }
                } catch (e: any) {
                    const msg = e?.message ? `Bağlantı hatası: ${e.message}` : 'Bağlantı hatası';
                    dispatch({ type: 'MEDIA_UPLOAD_FAIL', payload: msg });
                    return { ok: false, message: msg };
                }
            },

            async uploadVideo({ roomId, userId, caption, file }) {
                dispatch({ type: 'MEDIA_UPLOAD_REQUEST' });
                try {
                    const res = await MemeService.uploadVideoMultipart({
                        roomId,
                        userId,
                        caption,
                        file,
                    });

                    if (res.success) {
                        dispatch({ type: 'MEDIA_UPLOAD_SUCCESS', payload: res });
                        return { ok: true };
                    } else {
                        dispatch({ type: 'MEDIA_UPLOAD_FAIL', payload: res.message || 'Video yükleme hatası' });
                        return { ok: false, message: res.message || 'Video yükleme hatası' };
                    }
                } catch (e: any) {
                    const msg = e?.message ? `Bağlantı hatası: ${e.message}` : 'Bağlantı hatası';
                    dispatch({ type: 'MEDIA_UPLOAD_FAIL', payload: msg });
                    return { ok: false, message: msg };
                }
            },

            clearError() {
                dispatch({ type: 'MEDIA_CLEAR_ERROR' });
            },
        };
    }, [state]);

    return <MediaContext.Provider value={value}>{children}</MediaContext.Provider>;
}

export function useMedia() {
    const ctx = useContext(MediaContext);
    if (!ctx) throw new Error('useMedia must be used within MediaProvider');
    return ctx;
}
