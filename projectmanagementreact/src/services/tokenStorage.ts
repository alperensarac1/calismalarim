const ACCESS_TOKEN_KEY = 'project_management_access_token';
const REFRESH_TOKEN_KEY = 'project_management_refresh_token';
const TOKEN_EXPIRES_AT_KEY = 'project_management_token_expires_at';

export interface StoredAuthTokens {
    accessToken: string;
    refreshToken: string;
    expiresAtUtc: string;
}

export const tokenStorage = {
    getAccessToken(): string | null {
        return localStorage.getItem(ACCESS_TOKEN_KEY);
    },

    getRefreshToken(): string | null {
        return localStorage.getItem(REFRESH_TOKEN_KEY);
    },

    getExpiresAtUtc(): string | null {
        return localStorage.getItem(TOKEN_EXPIRES_AT_KEY);
    },

    setTokens(tokens: StoredAuthTokens): void {
        localStorage.setItem(
            ACCESS_TOKEN_KEY,
            tokens.accessToken,
        );

        localStorage.setItem(
            REFRESH_TOKEN_KEY,
            tokens.refreshToken,
        );

        localStorage.setItem(
            TOKEN_EXPIRES_AT_KEY,
            tokens.expiresAtUtc,
        );
    },

    clearTokens(): void {
        localStorage.removeItem(ACCESS_TOKEN_KEY);
        localStorage.removeItem(REFRESH_TOKEN_KEY);
        localStorage.removeItem(TOKEN_EXPIRES_AT_KEY);
    },

    hasAccessToken(): boolean {
        const accessToken = this.getAccessToken();

        return Boolean(accessToken);
    },
};