export class ApiClient {
    /**
     * Android Emulator için:
     */
    static baseUrl = 'https://alperensaracdeneme.com/event_ticket_api/';

    /**
     * iOS Simulator için:
     * static baseUrl = 'http://localhost/event_ticket_api/';
     *
     * Gerçek telefon için:
     * static baseUrl = 'http://192.168.1.35/event_ticket_api/';
     */

    static async post<T>(
        endpoint: string,
        parameters: Record<string, string>,
    ): Promise<T> {
        const formBody = Object.keys(parameters)
            .map(key => {
                const encodedKey = encodeURIComponent(key);
                const encodedValue = encodeURIComponent(parameters[key]);
                return `${encodedKey}=${encodedValue}`;
            })
            .join('&');

        const response = await fetch(ApiClient.baseUrl + endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formBody,
        });

        const rawText = await response.text();

        if (!response.ok) {
            throw new Error(`HTTP sunucu hatası: ${response.status}`);
        }

        try {
            return JSON.parse(rawText) as T;
        } catch (error) {
            console.log('JSON Decode Error Raw Response:');
            console.log(rawText);
            throw error;
        }
    }
}