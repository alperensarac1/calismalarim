import React, { useEffect, useState } from "react";
import { ScrollView, Text, StyleSheet } from "react-native";

import { SocketListener } from "../models/types";
import { WebSocketManager } from "../socket/WebSocketManager";

type Props = {
    roomCode: string;
    username: string;
    questionTime: number;
    onQuizStarted: () => void;
};

export default function WaitingRoomScreen({
                                              roomCode,
                                              username,
                                              questionTime,
                                              onQuizStarted,
                                          }: Props) {
    const [playersText, setPlayersText] = useState("Oyuncular yükleniyor...");
    const [statusText, setStatusText] = useState("");

    useEffect(() => {
        const listener: SocketListener = {
            onMessage: (message) => {
                const json = JSON.parse(message);

                if (json.type === "player_list_updated") {
                    setPlayersText(buildPlayersText(json.players ?? []));
                }

                if (json.type === "quiz_started") {
                    setStatusText("Quiz başladı.");
                    onQuizStarted();
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
    }, [onQuizStarted]);

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Bekleme Odası</Text>

            <Text style={styles.info}>
                Kullanıcı: {username}
                {"\n"}
                Oda Kodu: {roomCode}
                {"\n"}
                Soru Süresi: {questionTime} saniye
                {"\n\n"}
                Oda sahibi quizi başlatınca sorular ekrana gelecek.
            </Text>

            <Text style={styles.status}>{statusText}</Text>

            <Text style={styles.players}>{playersText}</Text>
        </ScrollView>
    );
}

function buildPlayersText(players: string[]): string {
    if (!players || players.length === 0) {
        return "Oyuncular yükleniyor...";
    }

    return `Odada bulunan oyuncular:\n\n${players
        .map((player, index) => `${index + 1}. ${player}`)
        .join("\n")}`;
}

const styles = StyleSheet.create({
    container: {
        padding: 24,
        backgroundColor: "#F8FAFC",
        flexGrow: 1,
    },
    title: {
        fontSize: 28,
        fontWeight: "800",
        color: "#111827",
    },
    info: {
        marginTop: 18,
        fontSize: 16,
        color: "#374151",
        lineHeight: 24,
    },
    status: {
        marginTop: 18,
        fontSize: 15,
        color: "#6D28D9",
        fontWeight: "700",
    },
    players: {
        marginTop: 24,
        fontSize: 15,
        color: "#111827",
        lineHeight: 22,
    },
});