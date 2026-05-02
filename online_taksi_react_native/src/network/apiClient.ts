import { Constants } from "../core/constants";
import { SessionManager } from "../core/sessionManager";

type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

async function request<T>(
    path: string,
    method: HttpMethod,
    body?: unknown
): Promise<T> {
    const token = await SessionManager.getToken();

    const response = await fetch(Constants.BASE_URL + path, {
        method,
        headers: {
            "Content-Type": "application/json",
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
        body: body ? JSON.stringify(body) : undefined,
    });

    const text = await response.text();
    const json = text ? JSON.parse(text) : {};

    if (!response.ok) {
        const message =
            typeof json?.detail === "string" ? json.detail : text || "İstek başarısız";
        throw new Error(message);
    }

    return json as T;
}

export const ApiClient = {
    get<T>(path: string) {
        return request<T>(path, "GET");
    },

    post<T>(path: string, body: unknown) {
        return request<T>(path, "POST", body);
    },

    put<T>(path: string, body?: unknown) {
        return request<T>(path, "PUT", body ?? {});
    },
};