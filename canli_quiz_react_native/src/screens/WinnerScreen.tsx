import React from "react";
import { ScrollView, Text, Pressable, StyleSheet } from "react-native";
import { ScoreItem } from "../models/types";

type Props = {
    winners: ScoreItem[];
    scoreboard: ScoreItem[];
    onBackHome: () => void;
};

export default function WinnerScreen({
                                         winners,
                                         scoreboard,
                                         onBackHome,
                                     }: Props) {
    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Quiz Bitti</Text>

            <Text style={styles.subtitle}>Bunlar Kazandı</Text>

            <Text style={styles.winners}>{buildWinnersText(winners)}</Text>

            <Text style={styles.scoreboard}>{buildScoreboardText(scoreboard)}</Text>

            <Pressable style={styles.button} onPress={onBackHome}>
                <Text style={styles.buttonText}>Ana Sayfaya Dön</Text>
            </Pressable>
        </ScrollView>
    );
}

function buildWinnersText(winners: ScoreItem[]): string {
    if (!winners || winners.length === 0) {
        return "Kazanan bulunamadı.";
    }

    return winners
        .map((item, index) => {
            let medal = "";

            if (index === 0) {
                medal = "🥇";
            } else if (index === 1) {
                medal = "🥈";
            } else if (index === 2) {
                medal = "🥉";
            }

            return `${medal} ${item.username}\n${item.score} puan`;
        })
        .join("\n\n");
}

function buildScoreboardText(scoreboard: ScoreItem[]): string {
    if (!scoreboard || scoreboard.length === 0) {
        return "Puan tablosu yok.";
    }

    return `Genel Sıralama:\n\n${scoreboard
        .map((item, index) => `${index + 1}. ${item.username} - ${item.score} puan`)
        .join("\n")}`;
}

const styles = StyleSheet.create({
    container: {
        padding: 24,
        backgroundColor: "#F8FAFC",
        flexGrow: 1,
        alignItems: "center",
    },
    title: {
        fontSize: 32,
        fontWeight: "800",
        color: "#111827",
        textAlign: "center",
    },
    subtitle: {
        marginTop: 14,
        fontSize: 24,
        fontWeight: "800",
        color: "#6D28D9",
        textAlign: "center",
    },
    winners: {
        marginTop: 28,
        fontSize: 20,
        fontWeight: "800",
        color: "#111827",
        textAlign: "center",
        lineHeight: 28,
    },
    scoreboard: {
        alignSelf: "stretch",
        marginTop: 30,
        fontSize: 15,
        color: "#374151",
        lineHeight: 22,
    },
    button: {
        alignSelf: "stretch",
        height: 56,
        backgroundColor: "#6D28D9",
        borderRadius: 14,
        justifyContent: "center",
        alignItems: "center",
        marginTop: 32,
    },
    buttonText: {
        color: "#FFFFFF",
        fontSize: 17,
        fontWeight: "700",
    },
});