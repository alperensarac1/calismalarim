export type LoginRequest = {
    phone: string;
    password: string;
};

export type RegisterRequest = {
    full_name: string;
    phone: string;
    email?: string | null;
    password: string;
    role: string;
};

export type AuthResponse = {
    access_token: string;
    token_type: string;
    user_id: number;
    full_name: string;
    role: "customer" | "driver" | string;
};