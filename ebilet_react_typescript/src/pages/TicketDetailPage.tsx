import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { QRCodeSVG } from "qrcode.react";

import { ApiService } from "../core/apiService";
import { SessionManager } from "../core/sessionManager";
import type { Ticket } from "../models/Ticket";
import { AppButton } from "../components/AppButton";

export function TicketDetailPage() {
    const navigate = useNavigate();
    const params = useParams();

    const ticketId = Number(params.ticketId ?? 0);

    const [ticket, setTicket] = useState<Ticket | null>(null);
    const [loading, setLoading] = useState(false);
    const [statusMessage, setStatusMessage] = useState("Bilet detayı yükleniyor...");

    useEffect(() => {
        loadTicketDetail();
    }, [ticketId]);

    async function loadTicketDetail() {
        if (ticketId <= 0) {
            setStatusMessage("Bilet ID alınamadı.");
            return;
        }

        try {
            setLoading(true);
            setStatusMessage("Bilet detayı yükleniyor...");

            const apiToken = SessionManager.getApiToken();

            const response = await ApiService.getTicketDetail({
                apiToken,
                ticketId,
            });

            setLoading(false);

            if (!response.success) {
                setStatusMessage(response.message);
                alert(response.message);
                return;
            }

            if (!response.data) {
                setStatusMessage("Bilet bilgisi alınamadı.");
                return;
            }

            setTicket(response.data);
            setStatusMessage("Bilet detayı getirildi.");
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error ? error.message : "Bilet detayı yüklenemedi.";

            setStatusMessage(message);
            alert(message);
        }
    }

    function statusText(): string {
        const status = ticket?.status ?? ticket?.ticketStatus ?? ticket?.ticket_status ?? "-";

        if (status === "active") return "Aktif Bilet";
        if (status === "used") return "Kullanıldı";
        if (status === "cancelled") return "İptal Edildi";

        return status;
    }

    function statusClass(): string {
        const status = ticket?.status ?? ticket?.ticketStatus ?? ticket?.ticket_status ?? "-";

        if (status === "active") return "active";
        if (status === "used") return "used";
        if (status === "cancelled") return "cancelled";

        return "default";
    }

    if (loading && !ticket) {
        return (
            <main className="page">
                <section className="card center-card">
                    <span className="mini-loader" />
                    <p>Bilet detayı yükleniyor...</p>
                </section>
            </main>
        );
    }

    if (!loading && !ticket) {
        return (
            <main className="page">
                <section className="card center-card">
                    <div className="empty-icon">⚠️</div>
                    <h2>Bilet bilgisi bulunamadı</h2>
                    <p>{statusMessage}</p>

                    <div className="detail-actions">
                        <AppButton
                            title="Biletlerime Dön"
                            color="blue"
                            onClick={() => navigate("/tickets")}
                        />
                    </div>
                </section>
            </main>
        );
    }

    const eventTitle =
        ticket?.event?.title ?? ticket?.eventTitle ?? ticket?.event_title ?? "Etkinlik";

    const eventDate = ticket?.event?.eventDate ?? ticket?.event?.event_date ?? "-";

    const venueName =
        ticket?.venue?.name ??
        ticket?.location?.venueName ??
        ticket?.location?.venue_name ??
        ticket?.event?.venue?.name ??
        "-";

    const cityName =
        ticket?.city?.name ??
        ticket?.location?.cityName ??
        ticket?.location?.city_name ??
        ticket?.event?.city?.name ??
        "-";

    const districtName =
        ticket?.district?.name ??
        ticket?.location?.districtName ??
        ticket?.location?.district_name ??
        ticket?.event?.district?.name ??
        "-";

    const priceText = `${ticket?.price ?? 0} TL`;

    const qrText =
        ticket?.qrCodeText ??
        ticket?.qr_code_text ??
        ticket?.ticketCode ??
        ticket?.ticket_code ??
        "";

    const ticketCodeText = ticket?.ticketCode ?? ticket?.ticket_code ?? qrText;

    const usedAt = ticket?.usedAt ?? ticket?.used_at ?? "";

    return (
        <main className="page">
            <div className="status-row">
                {loading ? <span className="mini-loader" /> : null}
                <span>{statusMessage}</span>
            </div>

            <section className="ticket-detail-card">
                <h1>{eventTitle}</h1>

                <span className={`ticket-detail-status ${statusClass()}`}>
          {statusText()}
        </span>

                <div className="qr-box">
                    {qrText ? (
                        <QRCodeSVG value={qrText} size={230} />
                    ) : (
                        <p>QR oluşturulamadı.</p>
                    )}
                </div>

                <p className="ticket-code-text">{ticketCodeText}</p>

                <div className="detail-divider" />

                <DetailLine title="Tarih" value={eventDate} />
                <DetailLine title="Sahne" value={venueName} />
                <DetailLine title="Konum" value={`${cityName} / ${districtName}`} />
                <DetailLine title="Fiyat" value={priceText} />

                <p className="ticket-used-text">
                    {usedAt ? `Kullanım zamanı: ${usedAt}` : "Bilet henüz kullanılmadı."}
                </p>

                <div className="detail-actions">
                    <AppButton
                        title="Biletlerime Dön"
                        color="blue"
                        onClick={() => navigate("/tickets")}
                    />

                    <AppButton
                        title="Etkinliklere Git"
                        color="green"
                        onClick={() => navigate("/")}
                    />
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