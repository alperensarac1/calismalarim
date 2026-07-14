import {parseCity, type City } from "./City";
import {parseDistrict, type District } from "./District";
import {parseVenue, type Venue } from "./Venue";

export interface AppEvent {
    id: number;

    city_id?: number | null;
    cityId?: number | null;

    district_id?: number | null;
    districtId?: number | null;

    venue_id?: number | null;
    venueId?: number | null;

    title: string;
    description?: string | null;

    poster_url?: string | null;
    posterUrl?: string | null;

    event_date?: string | null;
    eventDate?: string | null;

    base_price?: number | null;
    basePrice?: number | null;

    total_quota?: number | null;
    totalQuota?: number | null;

    sold_count?: number | null;
    soldCount?: number | null;

    remaining_quota?: number | null;
    remainingQuota?: number | null;

    city_name?: string | null;
    cityName?: string | null;

    district_name?: string | null;
    districtName?: string | null;

    venue?: Venue | null;
    city?: City | null;
    district?: District | null;

    created_at?: string | null;
}

export function parseEvent(json: any): AppEvent {
    const cityId =
        json?.city_id === undefined || json?.city_id === null
            ? null
            : Number(json.city_id);

    const districtId =
        json?.district_id === undefined || json?.district_id === null
            ? null
            : Number(json.district_id);

    const venueId =
        json?.venue_id === undefined || json?.venue_id === null
            ? null
            : Number(json.venue_id);

    const basePrice =
        json?.base_price === undefined || json?.base_price === null
            ? null
            : Number(json.base_price);

    const totalQuota =
        json?.total_quota === undefined || json?.total_quota === null
            ? null
            : Number(json.total_quota);

    const soldCount =
        json?.sold_count === undefined || json?.sold_count === null
            ? null
            : Number(json.sold_count);

    const remainingQuota =
        json?.remaining_quota === undefined || json?.remaining_quota === null
            ? null
            : Number(json.remaining_quota);

    return {
        id: Number(json?.id ?? 0),

        city_id: cityId,
        cityId,

        district_id: districtId,
        districtId,

        venue_id: venueId,
        venueId,

        title: String(json?.title ?? ""),
        description: json?.description ?? null,

        poster_url: json?.poster_url ?? null,
        posterUrl: json?.poster_url ?? null,

        event_date: json?.event_date ?? null,
        eventDate: json?.event_date ?? null,

        base_price: basePrice,
        basePrice,

        total_quota: totalQuota,
        totalQuota,

        sold_count: soldCount,
        soldCount,

        remaining_quota: remainingQuota,
        remainingQuota,

        city_name: json?.city_name ?? null,
        cityName: json?.city_name ?? null,

        district_name: json?.district_name ?? null,
        districtName: json?.district_name ?? null,

        venue: json?.venue ? parseVenue(json.venue) : null,
        city: json?.city ? parseCity(json.city) : null,
        district: json?.district ? parseDistrict(json.district) : null,

        created_at: json?.created_at ?? null,
    };
}