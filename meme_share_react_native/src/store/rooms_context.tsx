import React, { createContext, useContext, useMemo, useReducer } from 'react';
import {RoomsState} from "./rooms_types";
import {initialRoomsState, roomsReducer} from "./rooms_reducer";
import {MemeService} from "../service/meme_service";


type RoomsContextValue = {
    state: RoomsState;
    fetchRooms: (userId: number) => Promise<void>;
    createRoom: (userId: number) => Promise<{ ok: boolean; message?: string; roomCode?: string | null }>;
    joinRoom: (userId: number, roomCode: string) => Promise<{ ok: boolean; message?: string }>;
};

const RoomsContext = createContext<RoomsContextValue | null>(null);

export function RoomsProvider({ children }: { children: React.ReactNode }) {
    const [state, dispatch] = useReducer(roomsReducer, initialRoomsState);

    const value = useMemo<RoomsContextValue>(() => {
        return {
            state,

            async fetchRooms(userId) {
                dispatch({ type: 'ROOMS_FETCH_REQUEST' });
                try {
                    const rooms = await MemeService.getJoinedRooms(userId);
                    dispatch({ type: 'ROOMS_FETCH_SUCCESS', payload: rooms });
                } catch (e: any) {
                    dispatch({ type: 'ROOMS_FETCH_FAIL', payload: e?.message || 'Odalar alınamadı' });
                }
            },

            async createRoom(userId) {
                dispatch({ type: 'ROOMS_CREATE_REQUEST' });
                try {
                    const res = await MemeService.createRoom(userId);
                    dispatch({ type: 'ROOMS_CREATE_SUCCESS', payload: res });

                    if (res.success) return { ok: true, roomCode: res.room_code };
                    return { ok: false, message: res.message || 'Oda oluşturma başarısız' };
                } catch (e: any) {
                    const msg = e?.message ? `Bağlantı hatası: ${e.message}` : 'Bağlantı hatası';
                    dispatch({ type: 'ROOMS_CREATE_FAIL', payload: msg });
                    return { ok: false, message: msg };
                }
            },

            async joinRoom(userId, roomCode) {
                dispatch({ type: 'ROOMS_JOIN_REQUEST' });
                try {
                    const res = await MemeService.joinRoom(userId, roomCode);
                    dispatch({ type: 'ROOMS_JOIN_SUCCESS', payload: res });

                    if (res.success) return { ok: true };
                    return { ok: false, message: res.message || 'Katılım başarısız' };
                } catch (e: any) {
                    const msg = e?.message ? `Bağlantı hatası: ${e.message}` : 'Bağlantı hatası';
                    dispatch({ type: 'ROOMS_JOIN_FAIL', payload: msg });
                    return { ok: false, message: msg };
                }
            },
        };
    }, [state]);

    return <RoomsContext.Provider value={value}>{children}</RoomsContext.Provider>;
}

export function useRooms() {
    const ctx = useContext(RoomsContext);
    if (!ctx) throw new Error('useRooms must be used within RoomsProvider');
    return ctx;
}
