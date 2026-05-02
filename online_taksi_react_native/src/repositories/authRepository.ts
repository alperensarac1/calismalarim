import { ApiClient } from "../network/apiClient";
import { AuthResponse, LoginRequest, RegisterRequest } from "../models/authModels";

export const AuthRepository = {
    login(phone: string, password: string) {
        const body: LoginRequest = { phone, password };
        return ApiClient.post<AuthResponse>("auth/login", body);
    },

    registerCustomer(params: {
        fullName: string;
        phone: string;
        email?: string | null;
        password: string;
    }) {
        const body: RegisterRequest = {
            full_name: params.fullName,
            phone: params.phone,
            email: params.email ?? null,
            password: params.password,
            role: "customer",
        };

        return ApiClient.post<AuthResponse>("auth/register", body);
    },
};