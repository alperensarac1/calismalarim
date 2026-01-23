
import * as FileSystem from "expo-file-system";
import Papa from "papaparse";

export type ImportRow = { externalId: string | null; dataJson: string };
export type ImportResult = { headers: string[]; rows: ImportRow[] };

export async function importCsvFromUri(uri: string): Promise<ImportResult> {
    const text = await FileSystem.readAsStringAsync(uri);


    const parsed = Papa.parse<Record<string, unknown>>(text, {
        header: true,
        skipEmptyLines: true,
    });

    const headers = (parsed.meta?.fields ?? []).filter(Boolean);
    const rows: ImportRow[] = [];

    for (const obj0 of parsed.data ?? []) {
        const cleaned: Record<string, string> = {};

        for (const k of headers) {
            const v = (obj0?.[k] ?? "").toString().trim();
            if (v) cleaned[k] = v;
        }

        const externalId = guessExternalId(headers, cleaned);
        rows.push({
            externalId,
            dataJson: JSON.stringify(cleaned),
        });
    }

    return { headers, rows };
}

function guessExternalId(headers: string[], obj: Record<string, string>): string | null {
    const candidates = ["id", "ID", "Id", "user_id", "uid", "pk"];
    for (const c of candidates) {
        if (headers.includes(c) && obj[c]) return obj[c];
    }
    return null;
}
