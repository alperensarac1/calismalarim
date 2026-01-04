import { decode as b64decode } from "base-64";

export const QRParser = {
    parsePayload(raw: string): Record<string, unknown> {
        let s = (raw ?? "").trim();

        if (
            (s.startsWith('"') && s.endsWith('"')) ||
            (s.startsWith("“") && s.endsWith("”"))
        ) {
            s = s.slice(1, -1);
        }

        s = s.replace(/\\"/g, '"').replace(/\\\\/g, "\\");

        if (s.toLowerCase().startsWith("http")) {
            try {
                const u = new URL(s);
                const q = u.searchParams.get("qr");
                if (q && q.trim().startsWith("{")) s = q.trim();
            } catch (_) {}
        }

        if (!s.startsWith("{") && this.looksLikeBase64(s)) {
            try {
                const decoded = b64decode(s);
                const t = String(decoded).trim();
                if (t.startsWith("{")) s = t;
            } catch (_) {}
        }

        if (!s.startsWith("{")) {
            const preview = s.length > 60 ? s.slice(0, 60) + "..." : s;
            throw new Error("Geçersiz QR: " + preview);
        }

        const obj = JSON.parse(s);
        if (!obj || typeof obj !== "object" || Array.isArray(obj)) {
            throw new Error("QR JSON obje değil");
        }
        return obj as Record<string, unknown>;
    },

    looksLikeBase64(s: string): boolean {
        return /^[A-Za-z0-9+/=\s]+$/.test(s);
    },
};
