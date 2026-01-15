import {AuthAction, AuthState} from "./auth_state";


export const initialAuthState: AuthState = {
    isLoading: false,
    error: null,
    userId: null,
    lastResponse: null,
};

export function authReducer(state: AuthState, action: AuthAction): AuthState {
    switch (action.type) {
        case 'AUTH_REQUEST':
            return { ...state, isLoading: true, error: null };
        case 'AUTH_SUCCESS':
            return {
                ...state,
                isLoading: false,
                error: null,
                userId: action.payload.userId,
                lastResponse: action.payload.response,
            };
        case 'AUTH_FAIL':
            return {
                ...state,
                isLoading: false,
                error: action.payload.error,
                lastResponse: action.payload.response ?? state.lastResponse,
            };
        case 'AUTH_LOGOUT':
            return initialAuthState;
        default:
            return state;
    }
}
