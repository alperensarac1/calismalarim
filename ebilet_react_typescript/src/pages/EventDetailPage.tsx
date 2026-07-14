import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { ApiClient } from "../core/apiClient";
import { ApiService } from "../core/apiService";
import { SessionManager } from "../core/sessionManager";
import {type AppEvent } from "../models/Event";
import { AppButton } from "../components/AppButton";

export function EventDetailPage() {
    const navigate = useNavigate();
    const params = useParams();

    const eventId = Number(params.eventId ?? 0);

    const [event, setEvent] = useState<AppEvent | null>(null);

    const [loading, setLoading] = useState(false);
    const [buying, setBuying] = useState(false);

    const [statusMessage, setStatusMessage] = useState(
        "Etkinlik detayı yükleniyor..."
    );

    useEffect(() => {
        loadEventDetail();
    }, [eventId]);

    async function loadEventDetail() {
        if (eventId <= 0) {
            setStatusMessage("Etkinlik ID alınamadı.");
            return;
        }

        try {
            setLoading(true);
            setStatusMessage("Etkinlik detayı yükleniyor...");

            const apiToken = SessionManager.getApiToken();

            const response = await ApiService.getEventDetail({
                apiToken,
                eventId,
            });

            setLoading(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            if (!response.data) {
                setStatusMessage("Etkinlik bilgisi alınamadı.");
                return;
            }

            setEvent(response.data);
            setStatusMessage("Etkinlik detayı getirildi.");
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error
                    ? error.message
                    : "Etkinlik detayı yüklenemedi.";

            setStatusMessage(message);
        }
    }

    async function buyTicket() {
        if (!event) {
            alert("Etkinlik bilgisi bulunamadı.");
            return;
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        if (remainingQuota <= 0) {
            alert("Bu etkinlik için kontenjan kalmamış.");
            return;
        }

        try {
            setBuying(true);
            setStatusMessage("Bilet oluşturuluyor...");

            const apiToken = SessionManager.getApiToken();

            const response = await ApiService.buyTicket({
                apiToken,
                eventId: event.id,
            });

            setBuying(false);

            if (!response.success) {
                setStatusMessage(response.message);
                alert(response.message);
                return;
            }

            const ticketCode =
                response.data?.ticketCode ?? response.data?.ticket_code ?? "-";

            setStatusMessage("Bilet başarıyla oluşturuldu.");

            alert(`Bilet başarıyla alındı.\nBilet kodu: ${ticketCode}`);

            /*
              Bir sonraki adımda MyTicketsPage ekleyeceğiz.
              Şimdilik Home sayfasına dönüyoruz.
            */
            navigate("/");
        } catch (error) {
            setBuying(false);

            const message =
                error instanceof Error ? error.message : "Bilet oluşturulamadı.";

            setStatusMessage(message);
            alert(message);
        }
    }

    function posterUrl(): string {
        const poster = event?.posterUrl ?? event?.poster_url ?? "";
        return ApiClient.getImageUrl(poster);
    }

    function canBuy(): boolean {
        if (!event) {
            return false;
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        return remainingQuota > 0 && !loading && !buying;
    }

    function buyButtonTitle(): string {
        if (buying) {
            return "Bilet Oluşturuluyor...";
        }

        if (!event) {
            return "Bilet Al";
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        if (remainingQuota <= 0) {
            return "Kontenjan Doldu";
        }

        return "Bilet Al";
    }

    function buyButtonColor(): "green" | "gray" {
        if (!event) {
            return "green";
        }

        const remainingQuota = event.remainingQuota ?? event.remaining_quota ?? 0;

        if (remainingQuota <= 0) {
            return "gray";
        }

        return "green";
    }

    if (loading && !event) {
        return (
            <main className="page">
                <section className="card center-card">
                    <span className="mini-loader" />
                    <p>Etkinlik detayı yükleniyor...</p>
                </section>
            </main>
        );
    }

    if (!loading && !event) {
        return (
            <main className="page">
                <section className="card center-card">
                    <div className="empty-icon">⚠️</div>
                    <h2>Etkinlik bilgisi bulunamadı</h2>
                    <p>{statusMessage}</p>

                    <div className="detail-actions">
                        <AppButton title="Geri Dön" color="blue" onClick={() => navigate("/")} />
                    </div>
                </section>
            </main>
        );
    }

    const cityName = event?.city?.name ?? event?.cityName ?? event?.city_name ?? "-";

    const districtName =
        event?.district?.name ?? event?.districtName ?? event?.district_name ?? "-";

    const venueName = event?.venue?.name ?? "-";
    const venueAddress = event?.venue?.address ?? "-";

    const priceText = `${event?.basePrice ?? event?.base_price ?? 0} TL`;

    const quotaText = `Kalan: ${
        event?.remainingQuota ?? event?.remaining_quota ?? 0
    }`;

    return (
        <main className="page">
            <div className="status-row">
                {loading || buying ? <span className="mini-loader" /> : null}
                <span>{statusMessage}</span>
            </div>

            <section className="event-detail-layout">
                <div className="detail-poster-card">
                    {posterUrl() ? (
                        <img src={posterUrl()} alt={event?.title ?? "Etkinlik"} />
                    ) : (
                        <div className="detail-poster-placeholder">🖼️</div>
                    )}
                </div>

                <div className="detail-info-card">
                    <h1>{event?.title}</h1>

                    <p className="detail-description">
                        {event?.description ?? "Açıklama bulunmuyor."}
                    </p>

                    <div className="detail-divider" />

                    <DetailLine title="Tarih" value={event?.eventDate ?? event?.event_date ?? "-"} />

                    <DetailLine title="Konum" value={`${cityName} / ${districtName}`} />

                    <DetailLine title="Sahne" value={venueName} />

                    <DetailLine title="Adres" value={venueAddress} />

                    <div className="detail-divider" />

                    <div className="detail-price-row">
                        <strong>{priceText}</strong>
                        <span>{quotaText}</span>
                    </div>

                    <div className="detail-actions">
                        <AppButton
                            title={buyButtonTitle()}
                            color={buyButtonColor()}
                            loading={buying}
                            disabled={!canBuy()}
                            onClick={buyTicket}
                        />

                        <AppButton title="Geri Dön" color="blue" onClick={() => navigate("/")} />
                    </div>
                </div>
            </section>
        </main>
    );
}

function DetailLine({ title, value }: { title: string; value: string }) {
    return (
        <div className="detail-line">
            <span>{title}</span>
            <strong>{value}</strong>
        </div>
    );
}