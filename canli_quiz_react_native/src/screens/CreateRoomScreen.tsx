import React, { useEffect, useState } from "react";
import {
    View,
    Text,
    TextInput,
    Pressable,
    StyleSheet,
    ScrollView,
} from "react-native";
import { SocketMessageFactory } from "../socket/SocketMessageFactory";
import { WebSocketManager } from "../socket/WebSocketManager";
import { SocketListener } from "../models/types";

type Props = {
    onBack: () => void;
    onRoomCreated: (
        roomCode: string,
        username: string,
        questionTime: number
    ) => void;
};

export default function CreateRoomScreen({ onBack, onRoomCreated }: Props) {
    const [username, setUsername] = useState("");
    const [questionTimeText, setQuestionTimeText] = useState("20");
    const [statusText, setStatusText] = useState("");

    const [pendingUsername, setPendingUsername] = useState("");
    const [pendingQuestionTime, setPendingQuestionTime] = useState(20);
    const [sendAfterConnect, setSendAfterConnect] = useState(false);

    useEffect(() => {
        const listener: SocketListener = {
            onOpen: () => {
                setStatusText("Sunucuya bağlandı.");

                if (sendAfterConnect) {
                    const message = SocketMessageFactory.createRoom(
                        pendingUsername,
                        pendingQuestionTime
                    );

                    WebSocketManager.send(message);
                    setSendAfterConnect(false);
                    setStatusText("Oda oluşturma isteği gönderildi...");
                }
            },

            onMessage: (message) => {
                const json = JSON.parse(message);
                const type = json.type;

                if (type === "room_created") {
                    onRoomCreated(
                        json.room_code,
                        json.username,
                        json.question_time ?? pendingQuestionTime
                    );
                }

                if (type === "error") {
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
    }, [sendAfterConnect, pendingUsername, pendingQuestionTime]);

    const createRoom = () => {
        const cleanUsername = username.trim();
        const questionTime = Number(questionTimeText) || 20;

        if (!cleanUsername) {
            setStatusText("Kullanıcı adı boş olamaz.");
            return;
        }

        if (questionTime < 5) {
            setStatusText("Soru süresi en az 5 saniye olmalı.");
            return;
        }

        setPendingUsername(cleanUsername);
        setPendingQuestionTime(questionTime);

        if (WebSocketManager.isConnected()) {
            WebSocketManager.send(
                SocketMessageFactory.createRoom(cleanUsername, questionTime)
            );

            setStatusText("Oda oluşturma isteği gönderildi...");
        } else {
            setSendAfterConnect(true);
            setStatusText("Sunucuya bağlanılıyor...");
            WebSocketManager.connect();
        }
    };

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Oda Oluştur</Text>

            <Text style={styles.subtitle}>
                Kullanıcı adını ve soru başına süreyi gir.
            </Text>

            <TextInput
                style={styles.input}
                placeholder="Kullanıcı adı"
                value={username}
                onChangeText={setUsername}
            />

            <TextInput
                style={styles.input}
                placeholder="Soru süresi örn: 20"
                value={questionTimeText}
                onChangeText={setQuestionTimeText}
                keyboardType="number-pad"
            />

            <Pressable style={styles.button} onPress={createRoom}>
                <Text style={styles.buttonText}>Odayı Oluştur</Text>
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