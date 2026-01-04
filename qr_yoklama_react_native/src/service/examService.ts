import { EXAM_CONFIG_URL } from "../util/constants";
import { ApiClient } from "./apiClient";

export async function getExamUrl(studentNo: string): Promise<string> {
    const api = new ApiClient();
    const resp = await api.get(EXAM_CONFIG_URL);
    const text = await resp.text();

    if (!resp.ok) {
        throw new Error(`Config alınamadı (${resp.status})\n${text}`);
    }

    const obj = JSON.parse(text) as { giris?: string };
    const base = (obj.giris ?? "").trim();
    if (!base) throw new Error("Config içinde 'giris' yok.");

    return base + encodeURIComponent(studentNo);
}
