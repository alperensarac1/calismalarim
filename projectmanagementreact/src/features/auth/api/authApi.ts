import type { ApiResponse } from '../../../types/api';
import { apiClient } from '../../../services/apiClient';

import type {
    CurrentUserResponseData,
    LoginRequest,
    LoginResponseData,
    LogoutRequest,
    RefreshTokenRequest,
    RefreshTokenResponseData,
} from '../types/auth.types';


export const authApi = {
    async login(
        request: LoginRequest,
    ): Promise<LoginResponseData> {
        const response = await apiClient.post<
            ApiResponse<LoginResponseData>
        >('/api/Auth/login', request);

        return response.data.data;
    },

    async getCurrentUser(): Promise<CurrentUserResponseData> {
        const response = await apiClient.get<
            ApiResponse<CurrentUserResponseData>
        >('/api/Auth/me');

        return response.data.data;
    },

    async refreshToken(
        request: RefreshTokenRequest,
    ): Promise<RefreshTokenResponseData> {
        const response = await apiClient.post<
            ApiResponse<RefreshTokenResponseData>
        >('/api/Auth/refresh', request);

        return response.data.data;
    },
    async logout(request: LogoutRequest): Promise<void> {
        await apiClient.post('/api/Auth/logout', request);
    },
    async logoutAll(): Promise<void> {
        await apiClient.post('/api/Auth/logout-all');
    },
};