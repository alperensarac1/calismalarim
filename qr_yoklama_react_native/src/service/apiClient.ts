export class ApiClient {
    async get(url: string): Promise<Response> {
        return fetch(url, { method: "GET" });
    }

    async postJson(url: string, body: Record<string, unknown>): Promise<Response> {
        return fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json; charset=utf-8",
                "X-Platform": "expo",
            },
            body: JSON.stringify(body),
        });
    }
}
