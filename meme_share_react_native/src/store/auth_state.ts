import {KullaniciResponse} from "../service/meme_service";


export type AuthState = {
    isLoading: boolean;
    error: string | null;
    userId: number | null;
    lastResponse: KullaniciResponse | null;
};

export type AuthAction =
    | { type: 'AUTH_REQUEST' }
    | { type: 'AUTH_SUCCESS'; payload: { userId: number; response: KullaniciResponse } }
    | { type: 'AUTH_FAIL'; payload: { error: string; response?: KullaniciResponse } }
    | { type: 'AUTH_LOGOUT' };
