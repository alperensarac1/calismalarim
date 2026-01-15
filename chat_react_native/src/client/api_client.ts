import axios from "axios";

export const client = axios.create({
    baseURL: "https://alperensaracdeneme.com/mesajlasma/",
    timeout: 20000,
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
});

export function toFormUrlEncoded(obj: Record<string, any>) {
    const params = new URLSearchParams();
    Object.entries(obj).forEach(([k, v]) => {
        if (v !== undefined && v !== null) params.append(k, String(v));
    });
    return params.toString();
}
