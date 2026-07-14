import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { ApiService } from "../core/apiService";
import { SessionManager } from "../core/sessionManager";
import type {City} from "../models/City";
import type { District } from "../models/District";
import {type AppEvent } from "../models/Event";
import { AppButton } from "../components/AppButton";
import { EventCard } from "../components/EventCard";

export function HomePage() {
    const navigate = useNavigate();

    const [cities, setCities] = useState<City[]>([]);
    const [districts, setDistricts] = useState<District[]>([]);
    const [events, setEvents] = useState<AppEvent[]>([]);

    const [selectedCityId, setSelectedCityId] = useState<number>(0);
    const [selectedDistrictId, setSelectedDistrictId] = useState<number>(0);

    const [statusMessage, setStatusMessage] = useState("Şehirler yükleniyor...");

    const [loadingCities, setLoadingCities] = useState(false);
    const [loadingDistricts, setLoadingDistricts] = useState(false);
    const [loadingEvents, setLoadingEvents] = useState(false);

    const fullName = SessionManager.getFullName();
    const role = SessionManager.getRole();

    useEffect(() => {
        loadCities();
    }, []);

    async function loadCities() {
        try {
            setLoadingCities(true);
            setStatusMessage("Şehirler yükleniyor...");

            const apiToken = SessionManager.getApiToken();

            const response = await ApiService.getCities(apiToken);

            setLoadingCities(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            const list = response.data ?? [];

            setCities(list);
            setDistricts([]);
            setEvents([]);
            setSelectedCityId(0);
            setSelectedDistrictId(0);

            if (list.length === 0) {
                setStatusMessage("Aktif şehir bulunamadı.");
            } else {
                setStatusMessage("Şehir seçiniz.");
            }
        } catch (error) {
            setLoadingCities(false);

            const message =
                error instanceof Error ? error.message : "Şehirler yüklenemedi.";

            setStatusMessage(message);
        }
    }

    async function loadDistricts(cityId: number) {
        if (cityId <= 0) {
            setDistricts([]);
            setEvents([]);
            setSelectedCityId(0);
            setSelectedDistrictId(0);
            setStatusMessage("Şehir seçiniz.");
            return;
        }

        try {
            setLoadingDistricts(true);
            setStatusMessage("İlçeler yükleniyor...");

            setSelectedCityId(cityId);
            setSelectedDistrictId(0);
            setDistricts([]);
            setEvents([]);

            const apiToken = SessionManager.getApiToken();

            const response = await ApiService.getDistrictsByCity({
                apiToken,
                cityId,
            });

            setLoadingDistricts(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            const list = response.data ?? [];

            setDistricts(list);

            if (list.length === 0) {
                setStatusMessage("Bu şehir için aktif ilçe bulunamadı.");
            } else {
                setStatusMessage("İlçe seçip etkinlikleri listeleyebilirsin.");
            }
        } catch (error) {
            setLoadingDistricts(false);

            const message =
                error instanceof Error ? error.message : "İlçeler yüklenemedi.";

            setStatusMessage(message);
        }
    }

    async function loadEvents() {
        if (selectedCityId <= 0) {
            alert("Lütfen şehir seçiniz.");
            return;
        }

        if (selectedDistrictId <= 0) {
            alert("Lütfen ilçe seçiniz.");
            return;
        }

        try {
            setLoadingEvents(true);
            setStatusMessage("Etkinlikler yükleniyor...");

            const apiToken = SessionManager.getApiToken();

            const response = await ApiService.getEventsByLocation({
                apiToken,
                cityId: selectedCityId,
                districtId: selectedDistrictId,
            });

            setLoadingEvents(false);

            if (!response.success) {
                setStatusMessage(response.message);
                return;
            }

            const list = response.data ?? [];

            setEvents(list);

            if (list.length === 0) {
                setStatusMessage("Bu konum için etkinlik bulunamadı.");
            } else {
                setStatusMessage(`${list.length} etkinlik listelendi.`);
            }
        } catch (error) {
            setLoadingEvents(false);

            const message =
                error instanceof Error ? error.message : "Etkinlikler yüklenemedi.";

            setStatusMessage(message);
        }
    }

    function logout() {
        SessionManager.logout();

        navigate("/login", {
            replace: true,
        });
    }

    function openMyTickets() {
        navigate("/tickets");
    }

    function openEventDetail(event: AppEvent) {
        navigate(`/events/${event.id}`);
    }

    function roleText() {
        if (role === "admin") {
            return "Admin hesabı";
        }

        if (role === "staff") {
            return "Görevli hesabı";
        }

        return "Etkinlikleri keşfet";
    }

    const isAnyLoading = loadingCities || loadingDistricts || loadingEvents;

    return (
        <main className="page">
            <section className="home-header-card">
                <div>
                    <h1>Hoş geldin, {fullName || "Kullanıcı"}</h1>
                    <p>{roleText()}</p>
                </div>

                <div className="home-header-actions">
                    <button className="small-action-button blue" onClick={openMyTickets}>
                        Biletlerim
                    </button>

                    <button className="small-action-button red" onClick={logout}>
                        Çıkış
                    </button>
                </div>
            </section>

            <section className="filter-card">
                <h2>Konum Seç</h2>

                <p>Önce şehir, sonra ilçe seçerek etkinlikleri listeleyebilirsin.</p>

                <div className="filter-grid">
                    <div className="select-group">
                        <label>Şehir</label>

                        <select
                            value={selectedCityId}
                            disabled={loadingCities}
                            onChange={(event) => loadDistricts(Number(event.target.value))}
                        >
                            <option value={0}>
                                {loadingCities ? "Şehirler yükleniyor..." : "Şehir seçiniz"}
                            </option>

                            {cities.map((city) => (
                                <option key={city.id} value={city.id}>
                                    {city.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="select-group">
                        <label>İlçe</label>

                        <select
                            value={selectedDistrictId}
                            disabled={selectedCityId <= 0 || loadingDistricts}
                            onChange={(event) => setSelectedDistrictId(Number(event.target.value))}
                        >
                            <option value={0}>
                                {selectedCityId <= 0
                                    ? "Önce şehir seçiniz"
                                    : loadingDistricts
                                        ? "İlçeler yükleniyor..."
                                        : "İlçe seçiniz"}
                            </option>

                            {districts.map((district) => (
                                <option key={district.id} value={district.id}>
                                    {district.name}
                                </option>
                            ))}
                        </select>
                    </div>
                </div>

                <div className="filter-button-area">
                    <AppButton
                        title="Etkinlikleri Listele"
                        color="green"
                        loading={loadingEvents}
                        onClick={loadEvents}
                    />
                </div>
            </section>

            <div className="status-row">
                {isAnyLoading ? <span className="mini-loader" /> : null}
                <span>{statusMessage}</span>
            </div>

            {events.length === 0 && !isAnyLoading ? (
                <section className="empty-card">
                    <div className="empty-icon">📅</div>
                    <h3>Henüz etkinlik listelenmedi</h3>
                    <p>Şehir ve ilçe seçtikten sonra etkinlikleri listeleyebilirsin.</p>
                </section>
            ) : null}

            <section className="events-grid">
                {events.map((event) => (
                    <EventCard
                        key={event.id}
                        event={event}
                        onClick={() => openEventDetail(event)}
                    />
                ))}
            </section>
        </main>
    );
}