import { apiClient } from '../../../services/apiClient';

import type {
    ApiResponse,
    PagedResponse,
} from '../../../types/api';

import type {
    CreateUserRequest,
    GetUsersParams,
    ResetUserPasswordRequest,
    SystemUser,
    UpdateUserRequest,
    UpdateUserStatusRequest,
} from '../types/user.types';

/*
 * Kullanıcı listeleme endpointine gönderilebilecek
 * query parametresi değerleri.
 */
type UserQueryParamValue =
    | string
    | number
    | boolean;

/*
 * Frontend'deki camelCase query modelini backend'in
 * beklediği PascalCase parametrelere dönüştürür.
 */
function createUsersQueryParams(
    params: GetUsersParams,
): Record<string, UserQueryParamValue> {
    const queryParams: Record<
        string,
        UserQueryParamValue
    > = {
        Page: params.page ?? 1,
        PageSize: params.pageSize ?? 20,
    };

    const normalizedSearch =
        params.search?.trim();

    if (normalizedSearch) {
        queryParams.Search =
            normalizedSearch;
    }

    if (params.role) {
        queryParams.Role =
            params.role;
    }

    /*
     * false değeri de geçerli filtre olduğundan
     * doğrudan Boolean kontrolü yapılmaz.
     */
    if (
        params.isActive !== undefined
    ) {
        queryParams.IsActive =
            params.isActive;
    }

    return queryParams;
}

export const usersApi = {
    /*
     * Sayfalı ve filtrelenmiş kullanıcı listesini getirir.
     *
     * GET /api/Users
     */
    async getUsers(
        params: GetUsersParams,
    ): Promise<PagedResponse<SystemUser>> {
        const response = await apiClient.get<
            ApiResponse<
                PagedResponse<SystemUser>
            >
        >('/api/Users', {
            params:
                createUsersQueryParams(
                    params,
                ),
        });

        return response.data.data;
    },

    /*
     * Tek kullanıcının detayını getirir.
     *
     * GET /api/Users/{id}
     */
    async getUserById(
        userId: number,
    ): Promise<SystemUser> {
        const response = await apiClient.get<
            ApiResponse<SystemUser>
        >(`/api/Users/${userId}`);

        return response.data.data;
    },

    /*
     * Yeni kullanıcı oluşturur.
     *
     * POST /api/Users
     */
    async createUser(
        request: CreateUserRequest,
    ): Promise<SystemUser> {
        const response = await apiClient.post<
            ApiResponse<SystemUser>
        >('/api/Users', request);

        return response.data.data;
    },

    /*
     * Kullanıcının temel bilgilerini günceller.
     *
     * PUT /api/Users/{id}
     */
    async updateUser(
        userId: number,
        request: UpdateUserRequest,
    ): Promise<SystemUser> {
        const response = await apiClient.put<
            ApiResponse<SystemUser>
        >(
            `/api/Users/${userId}`,
            request,
        );

        return response.data.data;
    },

    /*
     * Kullanıcının aktiflik durumunu değiştirir.
     *
     * PATCH /api/Users/{id}/status
     */
    async updateUserStatus(
        userId: number,
        request: UpdateUserStatusRequest,
    ): Promise<SystemUser> {
        const response = await apiClient.patch<
            ApiResponse<SystemUser>
        >(
            `/api/Users/${userId}/status`,
            request,
        );

        return response.data.data;
    },

    /*
     * Admin tarafından kullanıcının parolasını sıfırlar.
     *
     * PATCH /api/Users/{id}/reset-password
     *
     * Backend response modeli ApiResponse<string> şeklindedir.
     * data alanında işlem sonucu mesajı veya açıklama dönebilir.
     */
    async resetUserPassword(
        userId: number,
        request: ResetUserPasswordRequest,
    ): Promise<string> {
        const response = await apiClient.patch<
            ApiResponse<string>
        >(
            `/api/Users/${userId}/reset-password`,
            request,
        );

        return response.data.data;
    },
    
    async deleteUser(
        userId: number,
    ): Promise<string> {
        const response = await apiClient.delete<
            ApiResponse<string>
        >(`/api/Users/${userId}`);

        return response.data.data;
    },
};