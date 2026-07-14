import { ApiClient } from "../core/apiClient";
import {getResolvedTicketId, type Ticket } from "../models/Ticket";

type Props = {
    ticket: Ticket;
    onClick: () => void;
};

export function TicketCard({ ticket, onClick }: Props) {
    const posterUrl = ApiClient.getImageUrl(
        ticket.event?.posterUrl ?? ticket.event?.poster_url
    );

    const eventTitle =
        ticket.event?.title ?? ticket.eventTitle ?? ticket.event_title ?? "Etkinlik";

    const eventDate = ticket.event?.eventDate ?? ticket.event?.event_date ?? "-";

    const venueName =
        ticket.location?.venueName ??
        ticket.location?.venue_name ??
        ticket.venue?.name ??
        ticket.event?.venue?.name ??
        "-";

    const cityName =
        ticket.location?.cityName ??
        ticket.location?.city_name ??
        ticket.city?.name ??
        ticket.event?.city?.name ??
        "-";

    const districtName =
        ticket.location?.districtName ??
        ticket.location?.district_name ??
        ticket.district?.name ??
        ticket.event?.district?.name ??
        "-";

    const priceText = `${ticket.price ?? 0} TL`;

    const status = ticket.status ?? ticket.ticketStatus ?? ticket.ticket_status ?? "-";

    const statusText =
        status === "active"
            ? "Aktif"
            : status === "used"
                ? "Kullanıldı"
                : status === "cancelled"
                    ? "İptal"
                    : status;

    const statusClass =
        status === "active"
            ? "active"
            : status === "used"
                ? "used"
                : status === "cancelled"
                    ? "cancelled"
                    : "default";

    return (
        <article className="ticket-card" onClick={onClick}>
            <div className="ticket-poster">
                {posterUrl ? (
                    <img src={posterUrl} alt={eventTitle} />
                ) : (
                    <div className="ticket-poster-placeholder">🎟️</div>
                )}
            </div>

            <div className="ticket-card-body">
                <h3>{eventTitle}</h3>

                <p>Tarih: {eventDate}</p>
                <p>Sahne: {venueName}</p>
                <p>
                    {cityName} / {districtName}
                </p>

                <div className="ticket-card-footer">
                    <strong>{priceText}</strong>

                    <span className={`ticket-status ${statusClass}`}>{statusText}</span>
                </div>

                <small>Bilet ID: {getResolvedTicketId(ticket)}</small>
            </div>
        </article>
    );
}