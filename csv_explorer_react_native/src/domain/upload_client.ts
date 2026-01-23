import axios from "axios";

type UploadResponse =
    | string
    | { download_url?: string; url?: string; file_url?: string; link?: string; [k: string]: unknown };

export async function uploadCsv(endpoint: string, uri: string): Promise<string> {
    const form = new FormData();

    // PHP genelde "file" bekler. Eğer endpoint $_FILES['csv'] istiyorsa: "file" -> "csv"
    form.append("file", {
        uri,
        name: `upload_${Date.now()}.csv`,
        type: "text/csv",
    } as any);

    const res = await axios.post<UploadResponse>(endpoint, form, {
        headers: { "Content-Type": "multipart/form-data" },
        timeout: 60000,
    });

    const body = res.data;

    if (body && typeof body === "object") {
        const url = (body.download_url || body.url || body.file_url || body.link || "").toString().trim();
        if (url) return url;
    }

    const text = typeof body === "string" ? body : JSON.stringify(body);
    const m = text.match(/https?:\/\/\S+/);
    if (m?.[0]) return m[0];

    throw new Error("No download URL found in server response.");
}
