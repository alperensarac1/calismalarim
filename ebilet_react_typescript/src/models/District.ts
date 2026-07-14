export interface District {
    id: number;
    city_id?: number | null;
    cityId?: number | null;
    name: string;
}

export function parseDistrict(json: any): District {
    const cityId =
        json?.city_id === undefined || json?.city_id === null
            ? null
            : Number(json.city_id);

    return {
        id: Number(json?.id ?? 0),
        city_id: cityId,
        cityId,
        name: String(json?.name ?? ""),
    };
}