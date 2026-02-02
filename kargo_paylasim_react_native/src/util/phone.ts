export function normalizeTrToE164(raw: string): string {
    const p = (raw || "").replace(/\D+/g, "");
    if (!p) return "";
    if (p.length === 11 && p.startsWith("0")) return `+90${p.slice(1)}`;
    if (p.length === 10 && p.startsWith("5")) return `+90${p}`;
    if (p.startsWith("90") && p.length >= 12) return `+${p}`;
    if (raw.startsWith("+") && p.length >= 12) return `+${p}`;
    return raw.trim();
}

export function isLikelyTrPhoneE164(p: string): boolean {
    const digits = p.replace(/\D+/g, "");
    return p.startsWith("+90") && digits.length === 12; // +90 + 10 hane
}
