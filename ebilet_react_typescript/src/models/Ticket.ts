import {type AppEvent, parseEvent } from "./Event";
import {type City, parseCity} from "./City.ts";
import {type District, parseDistrict} from "./District.ts";
import {parseVenue, type Venue} from "./Venue.ts";
import {parseUser, type User} from "./User.ts";

export interface TicketLocation {
    city_name?: string | null;
    cityName?: string | null;

    district_name?: string | null;
    districtName?: string | null;

    venue_name?: string | null;
    venueName?: string | null;

    venue_address?: string | null;
    venueAddress?: string | null;
}

export interface Ticket {
    id?: number | null;
    ticket_id?: number | null;
    ticketId?: number | null;

    event_id?: number | null;
    eventId?: number | null;

    event_title?: string | null;
    eventTitle?: string | null;

    ticket_code?: string | null;
    ticketCode?: string | null;

    qr_code_text?: string | null;
    qrCodeText?: string | null;

    price?: number | null;

    status?: string | null;

    ticket_status?: string | null;
    ticketStatus?: string | null;

    purchased_at?: string | null;
    purchasedAt?: string | null;

    used_at?: string | null;
    usedAt?: string | null;

    transaction_id?: string | null;
    transactionId?: string | null;

    event?: AppEvent | null;
    city?: City | null;
    district?: District | null;
    venue?: Venue | null;
    location?: TicketLocation | null;
    user?: User | null;

    result?: string | null;
}

export function parseTicketLocation(json: any): TicketLocation {
    return {
        city_name: json?.city_name ?? null,
        cityName: json?.city_name ?? null,

        district_name: json?.district_name ?? null,
        districtName: json?.district_name ?? null,

        venue_name: json?.venue_name ?? null,
        venueName: json?.venue_name ?? null,

        venue_address: json?.venue_address ?? null,
        venueAddress: json?.venue_address ?? null,
    };
}

export function parseTicket(json: any): Ticket {
    const id =
        json?.id === undefined || json?.id === null ? null : Number(json.id);

    const ticketId =
        json?.ticket_id === undefined || json?.ticket_id === null
            ? null
            : Number(json.ticket_id);

    const eventId =
        json?.event_id === undefined || json?.event_id === null
            ? null
            : Number(json.event_id);

    const price =
        json?.price === undefined || json?.price === null
            ? null
            : Number(json.price);

    return {
        id,

        ticket_id: ticketId,
        ticketId,

        event_id: eventId,
        eventId,

        event_title: json?.event_title ?? null,
        eventTitle: json?.event_title ?? null,

        ticket_code: json?.ticket_code ?? null,
        ticketCode: json?.ticket_code ?? null,

        qr_code_text: json?.qr_code_text ?? null,
        qrCodeText: json?.qr_code_text ?? null,

        price,

        status: json?.status ?? null,

        ticket_status: json?.ticket_status ?? null,
        ticketStatus: json?.ticket_status ?? null,

        purchased_at: json?.purchased_at ?? null,
        purchasedAt: json?.purchased_at ?? null,

        used_at: json?.used_at ?? null,
        usedAt: json?.used_at ?? null,

        transaction_id: json?.transaction_id ?? null,
        transactionId: json?.transaction_id ?? null,

        event: json?.event ? parseEvent(json.event) : null,
        city: json?.city ? parseCity(json.city) : null,
        district: json?.district ? parseDistrict(json.district) : null,
        venue: json?.venue ? parseVenue(json.venue) : null,
        location: json?.location ? parseTicketLocation(json.location) : null,
        user: json?.user ? parseUser(json.user) : null,

        result: json?.result ?? null,
    };
}

export function getResolvedTicketId(ticket: Ticket): number {
    return ticket.ticketId ?? ticket.ticket_id ?? ticket.id ?? 0;
}