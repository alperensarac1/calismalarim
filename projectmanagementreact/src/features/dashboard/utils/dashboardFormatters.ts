export function formatHours(value: number): string {
    const safeValue = Number.isFinite(value)
        ? value
        : 0;

    return `${new Intl.NumberFormat('tr-TR', {
        maximumFractionDigits: 2,
    }).format(safeValue)} saat`;
}

export function normalizePercentage(
    value: number,
): number {
    if (!Number.isFinite(value)) {
        return 0;
    }

    return Math.min(100, Math.max(0, value));
}

export function formatPercentage(
    value: number,
): string {
    const normalizedValue =
        normalizePercentage(value);

    return `%${new Intl.NumberFormat('tr-TR', {
        maximumFractionDigits: 1,
    }).format(normalizedValue)}`;
}

export function formatDateTime(
    value: string,
): string {
    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return '-';
    }

    return new Intl.DateTimeFormat('tr-TR', {
        dateStyle: 'medium',
        timeStyle: 'short',
    }).format(date);
}