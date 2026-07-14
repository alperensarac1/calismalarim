export class ApiClient {
    static baseUrl = "https://alperensaracdeneme.com/event_ticket_api/";

    static async post<T>(
        endpoint: string,
        parameters: Record<string, string>
    ): Promise<T> {
        const formBody = new URLSearchParams(parameters);

        const response = await fetch(ApiClient.baseUrl + endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: formBody.toString(),
        });

        const rawText = await response.text();

        if (!response.ok) {
            throw new Error(`HTTP sunucu hatası: ${response.status}`);
        }

        try {
            return JSON.parse(rawText) as T;
        } catch (error) {
            console.log("JSON Decode Error Raw Response:");
            console.log(rawText);
            throw error;
        }
    }

    static getImageUrl(path?: string | null): string {
        if (!path) {
            return "";
        }

        if (path.startsWith("http")) {
            return path;
        }

        return ApiClient.baseUrl + path;
    }
}