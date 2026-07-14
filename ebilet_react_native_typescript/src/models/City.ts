export interface City {
    id: number;
    name: string;
}

export function parseCity(json: any): City {
    return {
        id: Number(json?.id ?? 0),
        name: String(json?.name ?? ''),
    };
}