import type {User} from "../models/User.ts";


const KEYS = {
    userId: "user_id",
    fullName: "full_name",
    email: "email",
    phone: "phone",
    role: "role",
    apiToken: "api_token",
    isLoggedIn: "is_logged_in",
};

export class SessionManager {
    static saveUser(user: User): void {
        localStorage.setItem(KEYS.userId, String(user.id));
        localStorage.setItem(KEYS.fullName, user.fullName ?? user.full_name ?? "");
        localStorage.setItem(KEYS.email, user.email ?? "");
        localStorage.setItem(KEYS.phone, user.phone ?? "");
        localStorage.setItem(KEYS.role, user.role ?? "user");
        localStorage.setItem(KEYS.apiToken, user.apiToken ?? user.api_token ?? "");
        localStorage.setItem(KEYS.isLoggedIn, "true");
    }

    static isLoggedIn(): boolean {
        return localStorage.getItem(KEYS.isLoggedIn) === "true";
    }

    static getApiToken(): string {
        return localStorage.getItem(KEYS.apiToken) ?? "";
    }

    static getFullName(): string {
        return localStorage.getItem(KEYS.fullName) ?? "";
    }

    static getEmail(): string {
        return localStorage.getItem(KEYS.email) ?? "";
    }

    static getRole(): string {
        return localStorage.getItem(KEYS.role) ?? "user";
    }

    static logout(): void {
        localStorage.removeItem(KEYS.userId);
        localStorage.removeItem(KEYS.fullName);
        localStorage.removeItem(KEYS.email);
        localStorage.removeItem(KEYS.phone);
        localStorage.removeItem(KEYS.role);
        localStorage.removeItem(KEYS.apiToken);
        localStorage.removeItem(KEYS.isLoggedIn);
    }
}