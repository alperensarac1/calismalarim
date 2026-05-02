import React, { useEffect, useMemo, useRef, useState } from "react";
import {
    Alert,
    ScrollView,
    StyleSheet,
    Text,
    TouchableOpacity,
    View,
} from "react-native";
import * as Location from "expo-location";
import { AppRoute } from "../../App";
import { SessionManager } from "../core/sessionManager";
import { AvailableRideItem } from "../models/driverModels";
import { RideResponse } from "../models/rideModels";
import { DriverRepository } from "../repositories/driverRepository";
import { SocketManager } from "../socket/socketManager";

type Props = {
    onRoute: (route: AppRoute) => void;
};

export function DriverHomeScreen({ onRoute }: Props) {
    const socketManager = useMemo(() => new SocketManager(), []);
    const locationSubscription = useRef<Location.LocationSubscription | null>(null);

    const [socketConnected, setSocketConnected] = useState(false);
    const [isOnline, setIsOnline] = useState(false);
    const [logText, setLogText] = useState("Hazır");
    const [locationText, setLocationText] = useState("Konum: -");

    const [availableRides, setAvailableRides] = useState<AvailableRideItem[]>([]);
    const [activeRide, setActiveRide] = useState<RideResponse | null>(null);

    const [loadingRides, setLoadingRides] = useState(false);
    const [sendingLocation, setSendingLocation] = useState(false);

    useEffect(() => {
        socketManager.onConnected = () => {
            setSocketConnected(true);
            setLogText("Socket bağlandı");
        };

        socketManager.onDisconnected = () => {
            setSocketConnected(false);
            setLogText("Socket kapandı");
        };

        socketManager.onError = (error) => {
            setLogText(`Socket hata: ${error}`);
            Alert.alert("Socket Hatası", error);
        };

        socketManager.onMessage = handleSocketMessage;

        return () => {
            socketManager.disconnect();
            locationSubscription.current?.remove();
        };
    }, []);

    async function connectSocket() {
        await socketManager.connect();
    }

    async function setDriverOnline(value: boolean) {
        try {
            const response = await DriverRepository.setOnline(value);
            setIsOnline(response.is_online);
            setLogText(value ? "Online oldun" : "Offline oldun");
            Alert.alert("Bilgi", value ? "Online oldun" : "Offline oldun");
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Online/offline hatası");
        }
    }

    async function loadAvailableRides() {
        try {
            setLoadingRides(true);
            const response = await DriverRepository.getAvailableRides();
            setAvailableRides(response.rides);
            setLogText("Açık ride listesi güncellendi");
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Ride listesi alınamadı");
        } finally {
            setLoadingRides(false);
        }
    }

    async function acceptRide(ride: AvailableRideItem) {
        try {
            const accepted = await DriverRepository.acceptRide(ride.id);
            setActiveRide(accepted);
            setAvailableRides((prev) => prev.filter((item) => item.id !== ride.id));
            setLogText(`Ride kabul edildi. id=${ride.id}`);
            Alert.alert("Başarılı", "Ride kabul edildi");
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Ride kabul edilemedi");
        }
    }

    async function updateStatus(status: string, note: string) {
        if (!activeRide) {
            Alert.alert("Bilgi", "Aktif ride yok");
            return;
        }

        try {
            const updated = await DriverRepository.updateRideStatus(
                activeRide.id,
                status,
                note
            );
            setActiveRide(updated);
            setLogText(`Status güncellendi: ${status}`);
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Status güncellenemedi");
        }
    }

    async function requestLocationPermission(): Promise<boolean> {
        const { status } = await Location.requestForegroundPermissionsAsync();
        if (status !== "granted") {
            Alert.alert("Bilgi", "Konum izni gerekli");
            return false;
        }
        return true;
    }

    async function sendCurrentLocation() {
        const permissionOk = await requestLocationPermission();
        if (!permissionOk) return;

        try {
            setSendingLocation(true);

            const current = await Location.getCurrentPositionAsync({
                accuracy: Location.Accuracy.High,
            });

            const lat = current.coords.latitude;
            const lng = current.coords.longitude;

            await DriverRepository.updateLocation(lat, lng);

            setLocationText(`Konum: ${lat}, ${lng}`);
            setLogText("Konum gönderildi");
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Konum gönderilemedi");
        } finally {
            setSendingLocation(false);
        }
    }

    async function startLiveLocation() {
        const permissionOk = await requestLocationPermission();
        if (!permissionOk) return;

        locationSubscription.current?.remove();

        locationSubscription.current = await Location.watchPositionAsync(
            {
                accuracy: Location.Accuracy.High,
                distanceInterval: 10,
                timeInterval: 5000,
            },
            async (location: { coords: { latitude: any; longitude: any; }; }) => {
                const lat = location.coords.latitude;
                const lng = location.coords.longitude;

                setLocationText(`Konum: ${lat}, ${lng}`);

                try {
                    await DriverRepository.updateLocation(lat, lng);
                    setLogText("Canlı konum gönderildi");
                } catch (e) {
                    setLogText("Konum gönderme hatası");
                }
            }
        );

        Alert.alert("Bilgi", "Canlı konum başladı");
    }

    function stopLiveLocation() {
        locationSubscription.current?.remove();
        locationSubscription.current = null;
        setLogText("Canlı konum durduruldu");
    }

    function handleSocketMessage(message: string) {
        setLogText(message);

        try {
            const json = JSON.parse(message);
            const event = json.event;

            if (event === "NEW_RIDE_REQUEST") {
                const data = json.data;
                if (!data) return;

                const ride: AvailableRideItem = {
                    id: Number(data.ride_id ?? data.id),
                    customer_id: Number(data.customer_id),
                    pickup_lat: Number(data.pickup_lat),
                    pickup_lng: Number(data.pickup_lng),
                    pickup_address: String(data.pickup_address),
                    dropoff_lat: Number(data.dropoff_lat),
                    dropoff_lng: Number(data.dropoff_lng),
                    dropoff_address: String(data.dropoff_address),
                    status: String(data.status),
                    estimated_fare:
                        data.estimated_fare == null ? null : Number(data.estimated_fare),
                };

                setAvailableRides((prev) => {
                    const exists = prev.some((item) => item.id === ride.id);
                    return exists ? prev : [ride, ...prev];
                });

                Alert.alert("Yeni Çağrı", "Yeni ride geldi");
            }
        } catch {
            setLogText("Socket parse hatası");
        }
    }

    async function logout() {
        socketManager.disconnect();
        locationSubscription.current?.remove();
        await SessionManager.clear();
        onRoute("login");
    }

    return (
        <ScrollView style={styles.container}>
            <Text style={styles.title}>Driver Home</Text>

            <Text>{socketConnected ? "Socket: Bağlı" : "Socket: Bağlı değil"}</Text>
            <Text>{isOnline ? "Durum: Online" : "Durum: Offline"}</Text>
            <Text>{locationText}</Text>
            <Text numberOfLines={3}>Log: {logText}</Text>

            <View style={styles.row}>
                <Button title="Socket Bağlan" onPress={connectSocket} />
            </View>

            <View style={styles.row}>
                <Button title="ONLINE OL" onPress={() => setDriverOnline(true)} />
                <Button title="OFFLINE OL" onPress={() => setDriverOnline(false)} outline />
            </View>

            <Button
                title={loadingRides ? "Yükleniyor..." : "Açık Ride'ları Getir"}
                onPress={loadAvailableRides}
                disabled={loadingRides}
                outline
            />

            <Text style={styles.sectionTitle}>Konum İşlemleri</Text>

            <View style={styles.row}>
                <Button
                    title={sendingLocation ? "Gönderiliyor..." : "Konum Gönder"}
                    onPress={sendCurrentLocation}
                    disabled={sendingLocation}
                />
                <Button title="Canlı Başlat" onPress={startLiveLocation} outline />
            </View>

            <Button title="Canlı Durdur" onPress={stopLiveLocation} outline />

            <Text style={styles.sectionTitle}>Aktif Ride</Text>
            {activeRide ? (
                <View style={styles.card}>
                    <Text style={styles.bold}>Ride ID: {activeRide.id}</Text>
                    <Text>Pickup: {activeRide.pickup_address}</Text>
                    <Text>Dropoff: {activeRide.dropoff_address}</Text>
                    <Text>Durum: {activeRide.status}</Text>

                    <Button
                        title="Yoldayım"
                        onPress={() =>
                            updateStatus(
                                "DRIVER_ARRIVING",
                                "Şoför müşteriye doğru yola çıktı."
                            )
                        }
                    />
                    <Button
                        title="Geldim"
                        onPress={() =>
                            updateStatus("DRIVER_ARRIVED", "Şoför alım noktasına ulaştı.")
                        }
                    />
                    <Button
                        title="Başlat"
                        onPress={() =>
                            updateStatus("RIDE_STARTED", "Müşteri araca bindi.")
                        }
                    />
                    <Button
                        title="Bitir"
                        onPress={() =>
                            updateStatus("RIDE_COMPLETED", "Yolculuk tamamlandı.")
                        }
                    />
                </View>
            ) : (
                <Text>Aktif ride yok</Text>
            )}

            <Text style={styles.sectionTitle}>Açık Ride Listesi</Text>
            {availableRides.length === 0 ? (
                <Text>Açık ride yok</Text>
            ) : (
                availableRides.map((ride) => (
                    <View key={ride.id} style={styles.card}>
                        <Text style={styles.bold}>Ride ID: {ride.id}</Text>
                        <Text>Pickup: {ride.pickup_address}</Text>
                        <Text>Dropoff: {ride.dropoff_address}</Text>
                        <Text>Tahmini ücret: {ride.estimated_fare ?? "-"}</Text>

                        <Button title="Kabul Et" onPress={() => acceptRide(ride)} />
                    </View>
                ))
            )}

            <TouchableOpacity style={styles.logoutButton} onPress={logout}>
                <Text style={styles.logoutText}>Çıkış Yap</Text>
            </TouchableOpacity>
        </ScrollView>
    );
}

function Button(props: {
    title: string;
    onPress: () => void;
    disabled?: boolean;
    outline?: boolean;
}) {
    return (
        <TouchableOpacity
            style={[
                styles.button,
                props.outline && styles.outlineButton,
                props.disabled && { opacity: 0.5 },
            ]}
            onPress={props.onPress}
            disabled={props.disabled}
        >
            <Text style={[styles.buttonText, props.outline && styles.outlineText]}>
                {props.title}
            </Text>
        </TouchableOpacity>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 16,
    },
    title: {
        fontSize: 28,
        fontWeight: "800",
        marginBottom: 12,
    },
    sectionTitle: {
        fontSize: 18,
        fontWeight: "800",
        marginTop: 20,
        marginBottom: 8,
    },
    row: {
        flexDirection: "row",
        gap: 8,
        marginVertical: 8,
    },
    button: {
        flex: 1,
        backgroundColor: "#111827",
        padding: 13,
        borderRadius: 12,
        alignItems: "center",
        marginVertical: 4,
    },
    outlineButton: {
        backgroundColor: "transparent",
        borderWidth: 1,
        borderColor: "#111827",
    },
    buttonText: {
        color: "white",
        fontWeight: "700",
    },
    outlineText: {
        color: "#111827",
    },
    card: {
        backgroundColor: "#F3F4F6",
        borderRadius: 12,
        padding: 14,
        marginBottom: 12,
        gap: 6,
    },
    bold: {
        fontWeight: "800",
    },
    logoutButton: {
        backgroundColor: "#991B1B",
        padding: 14,
        borderRadius: 12,
        alignItems: "center",
        marginVertical: 24,
    },
    logoutText: {
        color: "white",
        fontWeight: "800",
    },
});