export interface User {
    id: number;
    full_name?: string;
    fullName?: string;
    email: string;
    phone?: string | null;
    role: string;
    api_token?: string | null;
    apiToken?: string | null;
    created_at?: string | null;
}

export function parseUser(json: any): User {
    return {
        id: Number(json?.id ?? 0),
        full_name: String(json?.full_name ?? ''),
        fullName: String(json?.full_name ?? ''),
        email: String(json?.email ?? ''),
        phone: json?.phone ?? null,
        role: String(json?.role ?? 'user'),
        api_token: json?.api_token ?? null,
        apiToken: json?.api_token ?? null,
        created_at: json?.created_at ?? null,
    };
}