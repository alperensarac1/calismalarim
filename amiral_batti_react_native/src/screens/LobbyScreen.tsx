import React, { useEffect, useState } from "react";
import {
    Button,
    SafeAreaView,
    StyleSheet,
    Text,
    TextInput,
    View,
} from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import { socketManager, SocketEventListener } from "../core/socketManager";
import { RootStackParamList } from "../navigation/types";
import { RoomCreatedData } from "../models/roomCreatedData";
import { JoinedRoomData } from "../models/joinedRoomData";
import { PlayerJoinedData } from "../models/playerJoinedData";
import { ErrorData } from "../models/errorData";

type Props = NativeStackScreenProps<RootStackParamList, "Lobby">;

export default function LobbyScreen({ navigation }: Props) {
    const [playerName, setPlayerName] = useState("");
    const [roomCode, setRoomCode] = useState("");
    const [roomInfo, setRoomInfo] = useState("Oda: -");
    const [playersInfo, setPlayersInfo] = useState("Oyuncular: -");
    const [statusText, setStatusText] = useState("Durum: Hazır");
    const [currentRoomCode, setCurrentRoomCode] = useState("");
    const [currentPlayerId, setCurrentPlayerId] = useState("");

    useEffect(() => {
        const listener: SocketEventListener = {
            onConnected() {
                setStatusText("Durum: Sunucuya bağlandı");
            },
            onDisconnected() {
                setStatusText("Durum: Bağlantı kesildi");
            },
            onError(errorMessage) {
                setStatusText(`Hata: ${errorMessage}`);
            },
            onMessage(message) {
                const parsed = JSON.parse(message);
                const type = parsed.type as string;
                const data = parsed.data ?? {};

                switch (type) {
                    case "ROOM_CREATED": {
                        const decoded = data as RoomCreatedData;
                        setCurrentRoomCode(decoded.roomCode);
                        setCurrentPlayerId(decoded.playerId);
                        setRoomCode(decoded.roomCode);
                        setRoomInfo(`Oda: ${decoded.roomCode}`);
                        setPlayersInfo(
                            `Oyuncular: ${decoded.players.map((p) => p.name).join(" | ")}`
                        );
                        setStatusText(decoded.message);
                        break;
                    }

                    case "JOINED_ROOM": {
                        const decoded = data as JoinedRoomData;
                        setCurrentRoomCode(decoded.roomCode);
                        setCurrentPlayerId(decoded.playerId);
                        setRoomCode(decoded.roomCode);
                        setRoomInfo(`Oda: ${decoded.roomCode}`);
                        setPlayersInfo(
                            `Oyuncular: ${decoded.players.map((p) => p.name).join(" | ")}`
                        );
                        setStatusText(decoded.message);
                        break;
                    }

                    case "PLAYER_JOINED": {
                        const decoded = data as PlayerJoinedData;
                        setRoomInfo(`Oda: ${decoded.roomCode}`);
                        setPlayersInfo(
                            `Oyuncular: ${decoded.players.map((p) => p.name).join(" | ")}`
                        );
                        setStatusText(decoded.message);

                        if (decoded.players.length === 2) {
                            navigation.navigate("Placement", {
                                roomCode: currentRoomCode || decoded.roomCode,
                                playerId: currentPlayerId,
                                playerName: playerName.trim(),
                            });
                        }
                        break;
                    }

                    case "PLAYER_LEFT": {
                        const decoded = data as PlayerJoinedData;
                        setPlayersInfo(
                            `Oyuncular: ${decoded.players.map((p) => p.name).join(" | ")}`
                        );
                        setStatusText(decoded.message);
                        break;
                    }

                    case "ERROR": {
                        const decoded = data as ErrorData;
                        setStatusText(`Hata: ${decoded.message}`);
                        break;
                    }
                }
            },
        };

        socketManager.setListener(listener);

        return () => {
            socketManager.clearListener(listener);
        };
    }, [navigation, currentPlayerId, currentRoomCode, playerName]);

    const connectToServer = () => {
        setStatusText("Durum: Sunucuya bağlanılıyor...");
        socketManager.connect();
    };

    const createRoom = () => {
        if (!playerName.trim()) {
            setStatusText("Hata: Oyuncu adı gir");
            return;
        }

        socketManager.sendMap({
            type: "CREATE_ROOM",
            data: {
                playerName: playerName.trim(),
            },
        });
    };

    const joinRoom = () => {
        if (!playerName.trim() || !roomCode.trim()) {
            setStatusText("Hata: Oyuncu adı ve oda kodu gir");
            return;
        }

        socketManager.sendMap({
            type: "JOIN_ROOM",
            data: {
                playerName: playerName.trim(),
                roomCode: roomCode.trim(),
            },
        });
    };

    return (
        <SafeAreaView style={styles.container}>
            <Text style={styles.title}>Amiral Battı</Text>

            <TextInput
                style={styles.input}
                placeholder="Oyuncu adı"
                value={playerName}
                onChangeText={setPlayerName}
            />

            <TextInput
                style={styles.input}
                placeholder="Oda kodu"
                value={roomCode}
                onChangeText={setRoomCode}
                keyboardType="number-pad"
            />

            <View style={styles.buttonGap}>
                <Button title="Sunucuya Bağlan" onPress={connectToServer} />
            </View>
            <View style={styles.buttonGap}>
                <Button title="Oda Oluştur" onPress={createRoom} />
            </View>
            <View style={styles.buttonGap}>
                <Button title="Odaya Katıl" onPress={joinRoom} />
            </View>

            <View style={styles.card}>
                <Text>{roomInfo}</Text>
                <Text style={styles.spaceTop}>{playersInfo}</Text>
                <Text style={styles.spaceTop}>{statusText}</Text>
            </View>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 16,
    },
    title: {
        fontSize: 30,
        fontWeight: "700",
        marginBottom: 16,
    },
    input: {
        borderWidth: 1,
        borderColor: "#ccc",
        borderRadius: 10,
        paddingHorizontal: 12,
        paddingVertical: 12,
        marginBottom: 12,
    },
    buttonGap: {
        marginBottom: 12,
    },
    card: {
        marginTop: 12,
        padding: 16,
        borderRadius: 12,
        backgroundColor: "#f4f4f4",
    },
    spaceTop: {
        marginTop: 8,
    },
});
