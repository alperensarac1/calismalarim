import React, { useEffect, useRef, useState } from "react";
import {
    FlatList,
    StyleSheet,
    Text,
    TouchableOpacity,
    View,
} from "react-native";

import { AppConfig } from "../config/AppConfig";
import { RoomModel } from "../models/RoomModel";
import { LiveSocketService } from "../services/LiveSocketService";

type Props = {
    onBack: () => void;
    onRoomPress: (room: RoomModel) => void;
};

export function RoomListScreen({ onBack, onRoomPress }: Props) {
    const [rooms, setRooms] = useState<RoomModel[]>([]);
    const [statusText, setStatusText] = useState("Sunucuya bağlanıyor...");

    const socketRef = useRef<LiveSocketService | null>(null);

    useEffect(() => {
        const socket = new LiveSocketService(AppConfig.SERVER_URL, {
            onConnected: () => {
                setStatusText("Sunucuya bağlandı");

                socket.sendJson({
                    type: "get_rooms",
                });
            },

            onMessage: message => {
                try {
                    const data = JSON.parse(message);

                    if (data.type === "rooms_list") {
                        const mappedRooms: RoomModel[] = data.rooms.map((item: any) => ({
                            roomId: item.room_id,
                            title: item.title,
                            broadcasterName: item.broadcaster_name,
                            createdAt: item.created_at,
                            viewerCount: item.viewer_count,
                        }));

                        setRooms(mappedRooms);
                    }

                    if (data.type === "error") {
                        setStatusText(data.message);
                    }
                } catch (error) {
                    setStatusText("Sunucudan gelen veri okunamadı");
                }
            },

            onError: error => {
                setStatusText(`Hata: ${error}`);
            },

            onDisconnected: () => {
                setStatusText("Bağlantı kapandı");
            },
        });

        socketRef.current = socket;
        socket.connect();

        return () => {
            socket.disconnect();
            socketRef.current = null;
        };
    }, []);

    return (
        <View style={styles.container}>
            <TouchableOpacity style={styles.backButton} onPress={onBack}>
                <Text style={styles.backButtonText}>Geri</Text>
            </TouchableOpacity>

            <Text style={styles.title}>Aktif Yayınlar</Text>

            <Text style={styles.status}>{statusText}</Text>

            {rooms.length === 0 ? (
                <View style={styles.emptyContainer}>
                    <Text style={styles.emptyText}>Aktif yayın yok</Text>
                </View>
            ) : (
                <FlatList
                    data={rooms}
                    keyExtractor={item => item.roomId}
                    renderItem={({ item }) => (
                        <TouchableOpacity
                            style={styles.roomCard}
                            onPress={() => onRoomPress(item)}
                        >
                            <Text style={styles.roomTitle}>{item.title}</Text>

                            <Text style={styles.roomText}>
                                Yayıncı: {item.broadcasterName}
                            </Text>

                            <Text style={styles.roomText}>
                                İzleyici: {item.viewerCount}
                            </Text>

                            <Text style={styles.roomDate}>
                                Başlama: {item.createdAt}
                            </Text>
                        </TouchableOpacity>
                    )}
                />
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 16,
        backgroundColor: "#F8FAFC",
    },
    backButton: {
        alignSelf: "flex-start",
        backgroundColor: "#334155",
        paddingVertical: 10,
        paddingHorizontal: 16,
        borderRadius: 10,
        marginBottom: 12,
    },
    backButtonText: {
        color: "#FFFFFF",
        fontWeight: "700",
    },
    title: {
        fontSize: 24,
        fontWeight: "800",
        color: "#0F172A",
        marginBottom: 8,
    },
    status: {
        color: "#475569",
        marginBottom: 16,
    },
    emptyContainer: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
    },
    emptyText: {
        color: "#64748B",
        fontSize: 16,
    },
    roomCard: {
        backgroundColor: "#FFFFFF",
        padding: 16,
        borderRadius: 14,
        marginBottom: 12,
        borderWidth: 1,
        borderColor: "#E2E8F0",
    },
    roomTitle: {
        fontSize: 18,
        fontWeight: "800",
        color: "#0F172A",
        marginBottom: 6,
    },
    roomText: {
        color: "#334155",
        marginTop: 4,
    },
    roomDate: {
        color: "#64748B",
        fontSize: 12,
        marginTop: 6,
    },
});