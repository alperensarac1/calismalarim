import React, { useEffect, useState } from "react";
import {
    Text,
    TextInput,
    Pressable,
    StyleSheet,
    ScrollView,
} from "react-native";
import { SocketListener } from "../models/types";
import { SocketMessageFactory } from "../socket/SocketMessageFactory";
import { WebSocketManager } from "../socket/WebSocketManager";

type Props = {
    onBack: () => void;
    onRoomJoined: (
        roomCode: string,
        username: string,
        questionTime: number
    ) => void;
};

export default function JoinRoomScreen({ onBack, onRoomJoined }: Props) {
    const [username, setUsername] = useState("");
    const [roomCode, setRoomCode] = useState("");
    const [statusText, setStatusText] = useState("");

    const [pendingUsername, setPendingUsername] = useState("");
    const [pendingRoomCode, setPendingRoomCode] = useState("");
    const [sendAfterConnect, setSendAfterConnect] = useState(false);

    useEffect(() => {
        const listener: SocketListener = {
            onOpen: () => {
                setStatusText("Sunucuya bağlandı.");

                if (sendAfterConnect) {
                    WebSocketManager.send(
                        SocketMessageFactory.joinRoom(pendingRoomCode, pendingUsername)
                    );

                    setSendAfterConnect(false);
                    setStatusText("Odaya katılma isteği gönderildi...");
                }
            },

            onMessage: (message) => {
                const json = JSON.parse(message);

                if (json.type === "room_joined") {
                    onRoomJoined(json.room_code, json.username, json.question_time ?? 20);
                }

                if (json.type === "error") {
                    setStatusText(json.message ?? "Bilinmeyen hata oluştu.");
                }
            },

            onClose: () => {
                setStatusText("Sunucu bağlantısı kapandı.");
            },

            onError: (error) => {
                setStatusText(`Bağlantı hatası: ${error}`);
            },
        };

        WebSocketManager.setListener(listener);

        return () => {
            WebSocketManager.removeListener(listener);
        };
    }, [sendAfterConnect, pendingUsername, pendingRoomCode]);

    const joinRoom = () => {
        const cleanUsername = username.trim();
        const cleanRoomCode = roomCode.trim();

        if (!cleanUsername) {
            setStatusText("Kullanıcı adı boş olamaz.");
            return;
        }

        if (!cleanRoomCode) {
            setStatusText("Oda kodu boş olamaz.");
            return;
        }

        setPendingUsername(cleanUsername);
        setPendingRoomCode(cleanRoomCode);

        if (WebSocketManager.isConnected()) {
            WebSocketManager.send(
                SocketMessageFactory.joinRoom(cleanRoomCode, cleanUsername)
            );

            setStatusText("Odaya katılma isteği gönderildi...");
        } else {
            setSendAfterConnect(true);
            setStatusText("Sunucuya bağlanılıyor...");
            WebSocketManager.connect();
        }
    };

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Odaya Giriş Yap</Text>

            <Text style={styles.subtitle}>Kullanıcı adını ve oda kodunu gir.</Text>

            <TextInput
                style={styles.input}
                placeholder="Kullanıcı adı"
                value={username}
                onChangeText={setUsername}
            />

            <TextInput
                style={styles.input}
                placeholder="Oda kodu"
                value={roomCode}
                onChangeText={setRoomCode}
                keyboardType="number-pad"
            />

            <Pressable style={styles.button} onPress={joinRoom}>
                <Text style={styles.buttonText}>Odaya Katıl</Text>
            </Pressable>

            <Pressable style={styles.backButton} onPress={onBack}>
                <Text style={styles.backText}>Geri dön</Text>
            </Pressable>

            <Text style={styles.status}>{statusText}</Text>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        padding: 24,
    },
    title: {
        fontSize: 28,
        fontWeight: "800",
        color: "#111827",
    },
    subtitle: {
        fontSize: 15,
        color: "#6B7280",
        marginTop: 8,
        marginBottom: 24,
    },
    input: {
        height: 54,
        backgroundColor: "#FFFFFF",
        borderRadius: 12,
        borderWidth: 1,
        borderColor: "#D1D5DB",
        paddingHorizontal: 14,
        marginTop: 14,
    },
    button: {
        height: 56,
        backgroundColor: "#6D28D9",
        borderRadius: 14,
        justifyContent: "center",
        alignItems: "center",
        marginTop: 24,
    },
    buttonText: {
        color: "#FFFFFF",
        fontSize: 17,
        fontWeight: "700",
    },
    backButton: {
        marginTop: 14,
        alignItems: "center",
    },
    backText: {
        color: "#6D28D9",
        fontSize: 16,
        fontWeight: "600",
    },
    status: {
        marginTop: 20,
        color: "#374151",
        fontSize: 15,
    },
});