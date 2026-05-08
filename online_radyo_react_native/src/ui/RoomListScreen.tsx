import React, { useEffect, useState } from "react";
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    StyleSheet,
    RefreshControl,
} from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import { RadioRoom } from "../model/RadioRoom";
import RadioSocketManager from "../network/RadioSocketManager";
import {RootStackParamList} from "../../App";

type Props = NativeStackScreenProps<RootStackParamList, "RoomList">;

export default function RoomListScreen({ navigation }: Props) {
    const [rooms, setRooms] = useState<RadioRoom[]>([]);
    const [statusText, setStatusText] = useState("Sunucuya bağlanılıyor...");
    const [refreshing, setRefreshing] = useState(false);

    useEffect(() => {
        RadioSocketManager.onConnected = () => {
            setStatusText("Sunucuya bağlandı");
            RadioSocketManager.getRooms();
        };

        RadioSocketManager.onMessage = message => {
            handleSocketMessage(message);
        };

        RadioSocketManager.onError = error => {
            setStatusText(`Hata: ${error}`);
        };

        RadioSocketManager.connect();
        RadioSocketManager.getRooms();
    }, []);

    function handleSocketMessage(message: string) {
        try {
            const json = JSON.parse(message);

            if (json.type === "ROOM_LIST" || json.type === "ROOM_UPDATED") {
                const parsedRooms: RadioRoom[] = json.rooms ?? [];
                setRooms(parsedRooms);
            }
        } catch (error) {
            console.log("JSON parse hatası:", error);
        }
    }

    function refreshRooms() {
        setRefreshing(true);
        RadioSocketManager.getRooms();

        setTimeout(() => {
            setRefreshing(false);
        }, 500);
    }

    function openRoom(room: RadioRoom) {
        navigation.navigate("RadioPlayer", {
            room,
        });
    }

    return (
        <View style={styles.container}>
            <Text style={styles.status}>{statusText}</Text>

            <FlatList
                data={rooms}
                keyExtractor={item => item.id.toString()}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={refreshRooms} />
                }
                renderItem={({ item }) => (
                    <TouchableOpacity
                        style={styles.card}
                        onPress={() => openRoom(item)}
                    >
                        <Text style={styles.roomName}>{item.roomName}</Text>

                        <Text style={styles.musicText}>
                            {item.currentMusic
                                ? `Şu an: ${item.currentMusic}`
                                : "Şu an: Müzik yok"}
                        </Text>

                        <Text style={styles.listenerText}>
                            Dinleyici: {item.listenerCount}
                        </Text>
                    </TouchableOpacity>
                )}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 14,
        backgroundColor: "#F6F6F6",
    },
    status: {
        marginBottom: 12,
        fontSize: 14,
        color: "#555",
    },
    card: {
        backgroundColor: "white",
        padding: 16,
        marginBottom: 10,
        borderRadius: 12,
        elevation: 3,
    },
    roomName: {
        fontSize: 18,
        fontWeight: "700",
        marginBottom: 6,
    },
    musicText: {
        fontSize: 14,
        color: "#444",
        marginBottom: 4,
    },
    listenerText: {
        fontSize: 13,
        color: "#777",
    },
});