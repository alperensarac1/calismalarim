import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { ApiService } from "../core/apiService";
import { SessionManager } from "../core/sessionManager";
import {getResolvedTicketId, type Ticket } from "../models/Ticket";
import { TicketCard } from "../components/TicketCard";
import { AppButton } from "../components/AppButton";

export function MyTicketsPage() {
    const navigate = useNavigate();

    const [tickets, setTickets] = useState<Ticket[]>([]);
    const [loading, setLoading] = useState(false);
    const [statusMessage, setStatusMessage] = useState("Biletler yükleniyor...");

    useEffect(() => {
        loadMyTickets();
    }, []);

    async function loadMyTickets() {
        try {
            setLoading(true);
            setStatusMessage("Biletler yükleniyor...");

            const apiToken = SessionManager.getApiToken();

            const response = await ApiService.getMyTickets(apiToken);

            setLoading(false);

            if (!response.success) {
                setStatusMessage(response.message);
                alert(response.message);
                return;
            }

            const list = response.data ?? [];

            setTickets(list);

            if (list.length === 0) {
                setStatusMessage("Henüz satın alınmış biletin yok.");
            } else {
                setStatusMessage(`${list.length} bilet listelendi.`);
            }
        } catch (error) {
            setLoading(false);

            const message =
                error instanceof Error ? error.message : "Biletler yüklenemedi.";

            setStatusMessage(message);
            alert(message);
        }
    }

    function openTicketDetail(ticket: Ticket) {
        const ticketId = getResolvedTicketId(ticket);

        if (ticketId <= 0) {
            alert("Bilet ID alınamadı.");
            return;
        }

        navigate(`/tickets/${ticketId}`);
    }

    return (
        <main className="page">
            <section className="home-header-card">
                <div>
                    <h1>Biletlerim</h1>
                    <p>Satın aldığın biletleri ve QR kodlarını buradan görüntüleyebilirsin.</p>
                </div>

                <div className="home-header-actions">
                    <button className="small-action-button blue" onClick={() => navigate("/")}>
                        Etkinlikler
                    </button>

                    <button className="small-action-button red" onClick={() => navigate("/")}>
                        Geri Dön
                    </button>
                </div>
            </section>

            <div className="status-row">
                {loading ? <span className="mini-loader" /> : null}
                <span>{statusMessage}</span>
            </div>

            {loading && tickets.length === 0 ? (
                <section className="card center-card">
                    <span className="mini-loader" />
                    <p>Biletler yükleniyor...</p>
                </section>
            ) : null}

            {!loading && tickets.length === 0 ? (
                <section className="empty-card">
                    <div className="empty-icon">🎟️</div>
                    <h3>Henüz biletin yok</h3>
                    <p>Bir etkinlik seçip bilet satın aldığında burada görünecek.</p>

                    <div className="detail-actions">
                        <AppButton
                            title="Etkinliklere Git"
                            color="blue"
                            onClick={() => navigate("/")}
                        />
                    </div>
                </section>
            ) : null}

            <section className="tickets-grid">
                {tickets.map((ticket) => {
                    const ticketId = getResolvedTicketId(ticket);

                    return (
                        <TicketCard
                            key={ticketId}
                            ticket={ticket}
                            onClick={() => openTicketDetail(ticket)}
                        />
                    );
                })}
            </section>
        </main>
    );
}