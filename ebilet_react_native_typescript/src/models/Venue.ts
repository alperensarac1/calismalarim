export interface Venue {
    id: number;
    city_id?: number | null;
    cityId?: number | null;
    district_id?: number | null;
    districtId?: number | null;
    name: string;
    address?: string | null;
    capacity?: number | null;
}

export function parseVenue(json: any): Venue {
    const cityId =
        json?.city_id === undefined || json?.city_id === null
            ? null
            : Number(json.city_id);

    const districtId =
        json?.district_id === undefined || json?.district_id === null
            ? null
            : Number(json.district_id);

    return {
        id: Number(json?.id ?? 0),
        city_id: cityId,
        cityId,
        district_id: districtId,
        districtId,
        name: String(json?.name ?? ''),
        address: json?.address ?? null,
        capacity:
            json?.capacity === undefined || json?.capacity === null
                ? null
                : Number(json.capacity),
    };
}