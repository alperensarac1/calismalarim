import { http } from "./http";
import { Endpoints } from "./endpoints";
import { ApiResponse } from "./types";

export type LoginRequest = { email: string; password: string };
export type LoginResponse = { token: string };

export type RegisterRequest = { name: string; email: string; password: string };
export type RegisterResponse = { token: string };

export type UserDto = {
    id: number;
    name: string;
    email: string;
};

export const authApi = {
    async login(body: LoginRequest) {
        const { data } = await http.post<ApiResponse<LoginResponse>>(Endpoints.login, body);
        if (!data.ok || !data.data) throw new Error(data.error || "Login failed");
        return data.data;
    },

    async register(body: RegisterRequest) {
        const { data } = await http.post<ApiResponse<RegisterResponse>>(Endpoints.register, body);
        if (!data.ok || !data.data) throw new Error(data.error || "Register failed");
        return data.data;
    },

    async me() {
        const { data } = await http.get<ApiResponse<UserDto>>(Endpoints.me);
        if (!data.ok || !data.data) throw new Error(data.error || "Me failed");
        return data.data;
    },
};
