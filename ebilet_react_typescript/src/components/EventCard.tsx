import { ApiClient } from "../core/apiClient";
import type {Event} from "../models/Event.ts";

type Props = {
    event: Event;
    onClick: () => void;
};

export function EventCard({ event, onClick }: Props) {
    const posterUrl = ApiClient.getImageUrl(event.posterUrl ?? event.poster_url);

    const venueName = event.venue?.name ?? "-";

    const cityName = event.cityName ?? event.city_name ?? event.city?.name ?? "-";

    const districtName =
        event.districtName ?? event.district_name ?? event.district?.name ?? "-";

    const priceText = `${event.basePrice ?? event.base_price ?? 0} TL`;

    const quotaText = `Kalan: ${
        event.remainingQuota ?? event.remaining_quota ?? 0
    }`;

    return (
        <article className="event-card" onClick={onClick}>
            <div className="event-poster">
                {posterUrl ? (
                    <img src={posterUrl} alt={event.title} />
                ) : (
                    <div className="event-poster-placeholder">🖼️</div>
                )}
            </div>

            <div className="event-card-body">
                <h3>{event.title}</h3>

                <p className="event-info">Tarih: {event.eventDate ?? event.event_date ?? "-"}</p>

                <p className="event-info">Sahne: {venueName}</p>

                <p className="event-location">
                    {cityName} / {districtName}
                </p>

                <div className="event-card-footer">
                    <strong className="event-price">{priceText}</strong>

                    <span className="event-quota">{quotaText}</span>
                </div>
            </div>
        </article>
    );
}