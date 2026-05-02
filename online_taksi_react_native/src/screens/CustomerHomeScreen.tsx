import React, { useEffect, useMemo, useRef, useState } from "react";
import {
    Alert,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";
import MapView, { Marker, PROVIDER_GOOGLE, Region } from "react-native-maps";
import { AppRoute } from "../../App";
import { SessionManager } from "../core/sessionManager";
import { RideResponse } from "../models/rideModels";
import { RideRepository } from "../repositories/rideRepository";
import { SocketManager } from "../socket/socketManager";

type Props = {
    onRoute: (route: AppRoute) => void;
};

export function CustomerHomeScreen({ onRoute }: Props) {
    const socketManager = useMemo(() => new SocketManager(), []);
    const mapRef = useRef<MapView | null>(null);

    const [socketConnected, setSocketConnected] = useState(false);
    const [rideStatus, setRideStatus] = useState("Aktif ride yok");
    const [lastEvent, setLastEvent] = useState("-");
    const [driverText, setDriverText] = useState("Taksi konumu: -");

    const [pickupLat, setPickupLat] = useState("");
    const [pickupLng, setPickupLng] = useState("");
    const [pickupAddress, setPickupAddress] = useState("");

    const [dropoffLat, setDropoffLat] = useState("");
    const [dropoffLng, setDropoffLng] = useState("");
    const [dropoffAddress, setDropoffAddress] = useState("");

    const [activeRide, setActiveRide] = useState<RideResponse | null>(null);
    const [driverLocation, setDriverLocation] = useState<{
        lat: number;
        lng: number;
    } | null>(null);

    const [loading, setLoading] = useState(false);

    const initialRegion: Region = {
        latitude: 41.0082,
        longitude: 28.9784,
        latitudeDelta: 0.12,
        longitudeDelta: 0.12,
    };

    useEffect(() => {
        socketManager.onConnected = () => {
            setSocketConnected(true);
            setLastEvent("Socket bağlandı");
        };

        socketManager.onDisconnected = () => {
            setSocketConnected(false);
            setLastEvent("Socket kapandı");
        };

        socketManager.onError = (error) => {
            setLastEvent(`Socket hata: ${error}`);
            Alert.alert("Socket Hatası", error);
        };

        socketManager.onMessage = handleSocketMessage;

        return () => {
            socketManager.disconnect();
        };
    }, []);

    function handleSocketMessage(message: string) {
        setLastEvent(message);

        try {
            const json = JSON.parse(message);
            const event = json.event;

            if (
                event === "RIDE_ACCEPTED" ||
                event === "RIDE_STATUS_CHANGED" ||
                event === "RIDE_CANCELLED"
            ) {
                const status = json.data?.status;
                if (status) {
                    setRideStatus(status);
                }
            }

            if (event === "DRIVER_LOCATION") {
                const lat = Number(json.data?.lat);
                const lng = Number(json.data?.lng);

                if (!Number.isNaN(lat) && !Number.isNaN(lng)) {
                    setDriverLocation({ lat, lng });
                    setDriverText(`Taksi konumu: ${lat}, ${lng}`);
                }
            }
        } catch (e) {
            setLastEvent("Socket parse hatası");
        }
    }

    async function connectSocket() {
        await socketManager.connect();
    }

    async function createRide() {
        const pLat = Number(pickupLat);
        const pLng = Number(pickupLng);
        const dLat = Number(dropoffLat);
        const dLng = Number(dropoffLng);

        if (
            Number.isNaN(pLat) ||
            Number.isNaN(pLng) ||
            Number.isNaN(dLat) ||
            Number.isNaN(dLng) ||
            !pickupAddress.trim() ||
            !dropoffAddress.trim()
        ) {
            Alert.alert("Bilgi", "Tüm pickup/dropoff alanlarını doğru doldur");
            return;
        }

        try {
            setLoading(true);

            const ride = await RideRepository.createRide({
                pickup_lat: pLat,
                pickup_lng: pLng,
                pickup_address: pickupAddress.trim(),
                dropoff_lat: dLat,
                dropoff_lng: dLng,
                dropoff_address: dropoffAddress.trim(),
            });

            setActiveRide(ride);
            setRideStatus(ride.status);

            setTimeout(() => {
                fitMapToPoints(ride, driverLocation);
            }, 300);

            Alert.alert("Başarılı", "Taksi çağrısı oluşturuldu");
        } catch (e) {
            Alert.alert("Hata", e instanceof Error ? e.message : "Ride oluşturulamadı");
        } finally {
            setLoading(false);
        }
    }

    function fitMapToPoints(
        ride: RideResponse | null,
        driver: { lat: number; lng: number } | null
    ) {
        const coordinates = [];

        if (ride) {
            coordinates.push({
                latitude: ride.pickup_lat,
                longitude: ride.pickup_lng,
            });

            coordinates.push({
                latitude: ride.dropoff_lat,
                longitude: ride.dropoff_lng,
            });
        }

        if (driver) {
            coordinates.push({
                latitude: driver.lat,
                longitude: driver.lng,
            });
        }

        if (coordinates.length === 0) return;

        mapRef.current?.fitToCoordinates(coordinates, {
            edgePadding: {
                top: 80,
                right: 60,
                bottom: 80,
                left: 60,
            },
            animated: true,
        });
    }

    async function logout() {
        socketManager.disconnect();
        await SessionManager.clear();
        onRoute("login");
    }

    useEffect(() => {
        if (activeRide || driverLocation) {
            fitMapToPoints(activeRide, driverLocation);
        }
    }, [activeRide, driverLocation]);

    return (
        <ScrollView style={styles.container}>
            <Text style={styles.title}>Customer Home</Text>

            <Text>{socketConnected ? "Socket: Bağlı" : "Socket: Bağlı değil"}</Text>
            <Text>Ride durumu: {rideStatus}</Text>
            <Text>{driverText}</Text>
            <Text numberOfLines={3}>Son event: {lastEvent}</Text>

            <View style={styles.row}>
                <TouchableOpacity style={styles.button} onPress={connectSocket}>
                    <Text style={styles.buttonText}>Socket Bağlan</Text>
                </TouchableOpacity>

                <TouchableOpacity style={styles.outlineButton} onPress={() => socketManager.sendPing()}>
                    <Text>Ping</Text>
                </TouchableOpacity>
            </View>

            <MapView
                ref={mapRef}
                provider={PROVIDER_GOOGLE}
                style={styles.map}
                initialRegion={initialRegion}
            >
                {activeRide && (
                    <>
                        <Marker
                            coordinate={{
                                latitude: activeRide.pickup_lat,
                                longitude: activeRide.pickup_lng,
                            }}
                            title="Alınış Noktası"
                            description={activeRide.pickup_address}
                            pinColor="green"
                        />

                        <Marker
                            coordinate={{
                                latitude: activeRide.dropoff_lat,
                                longitude: activeRide.dropoff_lng,
                            }}
                            title="Varış Noktası"
                            description={activeRide.dropoff_address}
                            pinColor="red"
                        />
                    </>
                )}

                {driverLocation && (
                    <Marker
                        coordinate={{
                            latitude: driverLocation.lat,
                            longitude: driverLocation.lng,
                        }}
                        title="Taksiniz"
                        description="Canlı konum"
                        pinColor="orange"
                    />
                )}
            </MapView>

            <Text style={styles.sectionTitle}>Pickup / Dropoff Bilgileri</Text>

            <Input label="Pickup Enlem" value={pickupLat} onChangeText={setPickupLat} />
            <Input label="Pickup Boylam" value={pickupLng} onChangeText={setPickupLng} />
            <Input label="Pickup Adres" value={pickupAddress} onChangeText={setPickupAddress} />

            <Input label="Dropoff Enlem" value={dropoffLat} onChangeText={setDropoffLat} />
            <Input label="Dropoff Boylam" value={dropoffLng} onChangeText={setDropoffLng} />
            <Input label="Dropoff Adres" value={dropoffAddress} onChangeText={setDropoffAddress} />

            <TouchableOpacity
                style={[styles.button, loading && { opacity: 0.6 }]}
                onPress={createRide}
                disabled={loading}
            >
                <Text style={styles.buttonText}>{loading ? "Çağırılıyor..." : "Taksi Çağır"}</Text>
            </TouchableOpacity>

            {activeRide && (
                <View style={styles.card}>
                    <Text style={styles.sectionTitle}>Aktif Ride</Text>
                    <Text>Ride ID: {activeRide.id}</Text>
                    <Text>Pickup: {activeRide.pickup_address}</Text>
                    <Text>Dropoff: {activeRide.dropoff_address}</Text>
                    <Text>Durum: {activeRide.status}</Text>
                    <Text>Tahmini ücret: {activeRide.estimated_fare ?? "-"}</Text>
                </View>
            )}

            <TouchableOpacity style={styles.logoutButton} onPress={logout}>
                <Text style={styles.buttonText}>Çıkış Yap</Text>
            </TouchableOpacity>
        </ScrollView>
    );
}

function Input(props: {
    label: string;
    value: string;
    onChangeText: (text: string) => void;
}) {
    return (
        <TextInput
            value={props.value}
            onChangeText={props.onChangeText}
            placeholder={props.label}
            keyboardType={props.label.includes("Enlem") || props.label.includes("Boylam") ? "numeric" : "default"}
            style={styles.input}
        />
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
        marginTop: 16,
        marginBottom: 8,
    },
    row: {
        flexDirection: "row",
        gap: 8,
        marginVertical: 12,
    },
    button: {
        flex: 1,
        backgroundColor: "#111827",
        padding: 14,
        borderRadius: 12,
        alignItems: "center",
    },
    outlineButton: {
        flex: 1,
        borderWidth: 1,
        borderColor: "#111827",
        padding: 14,
        borderRadius: 12,
        alignItems: "center",
    },
    buttonText: {
        color: "white",
        fontWeight: "700",
    },
    map: {
        height: 300,
        borderRadius: 16,
        marginVertical: 12,
    },
    input: {
        borderWidth: 1,
        borderColor: "#CCCCCC",
        borderRadius: 12,
        padding: 12,
        marginBottom: 8,
    },
    card: {
        padding: 16,
        backgroundColor: "#F3F4F6",
        borderRadius: 12,
        marginTop: 16,
    },
    logoutButton: {
        backgroundColor: "#991B1B",
        padding: 14,
        borderRadius: 12,
        alignItems: "center",
        marginVertical: 24,
    },
});