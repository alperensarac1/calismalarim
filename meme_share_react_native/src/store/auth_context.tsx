import React, { createContext, useContext, useMemo, useReducer } from 'react';
import {authReducer, initialAuthState} from "./auth_reducer";
import {MemeService} from "../service/meme_service";
import {AuthState} from "./auth_state";

type AuthContextValue = {
    state: AuthState;
    login: (username: string, password: string) => Promise<{ ok: boolean; userId?: number; message?: string }>;
    register: (username: string, password: string) => Promise<{ ok: boolean; message?: string }>;
    logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [state, dispatch] = useReducer(authReducer, initialAuthState);

    const actions = useMemo<AuthContextValue>(() => {
        return {
            state,

            async login(username, password) {
                dispatch({ type: 'AUTH_REQUEST' });
                try {
                    const res = await MemeService.login(username, password);
                    if (res.success) {
                        dispatch({ type: 'AUTH_SUCCESS', payload: { userId: res.user_id, response: res } });
                        return { ok: true, userId: res.user_id };
                    } else {
                        dispatch({ type: 'AUTH_FAIL', payload: { error: res.message || 'Giriş başarısız', response: res } });
                        return { ok: false, message: res.message || 'Giriş başarısız' };
                    }
                } catch (e: any) {
                    const msg = e?.message ? `Bağlantı hatası: ${e.message}` : 'Bağlantı hatası';
                    dispatch({ type: 'AUTH_FAIL', payload: { error: msg } });
                    return { ok: false, message: msg };
                }
            },

            async register(username, password) {
                dispatch({ type: 'AUTH_REQUEST' });
                try {
                    const res = await MemeService.register(username, password);
                    if (res.success) {
                        dispatch({ type: 'AUTH_SUCCESS', payload: { userId: res.user_id, response: res } });
                        // Register success sonrası login ekranına dönmek isteyeceğiz (UI’da reset)
                        return { ok: true };
                    } else {
                        dispatch({ type: 'AUTH_FAIL', payload: { error: res.message || 'Sunucu hatası', response: res } });
                        return { ok: false, message: res.message || 'Sunucu hatası' };
                    }
                } catch (e: any) {
                    const msg = e?.message ? `Bağlantı hatası: ${e.message}` : 'Bağlantı hatası';
                    dispatch({ type: 'AUTH_FAIL', payload: { error: msg } });
                    return { ok: false, message: msg };
                }
            },

            logout() {
                dispatch({ type: 'AUTH_LOGOUT' });
            },
        };
    }, [state]);

    return <AuthContext.Provider value={actions}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error('useAuth must be used within AuthProvider');
    return ctx;
}
