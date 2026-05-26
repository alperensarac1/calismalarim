import React, { useEffect, useState } from "react";
import {
    ScrollView,
    View,
    Text,
    TextInput,
    Pressable,
    StyleSheet,
} from "react-native";

import { SocketListener } from "../models/types";
import { SocketMessageFactory } from "../socket/SocketMessageFactory";
import { WebSocketManager } from "../socket/WebSocketManager";

type Props = {
    roomCode: string;
    username: string;
    questionTime: number;
    onQuizStarted: () => void;
};

export default function OwnerRoomScreen({
                                            roomCode,
                                            username,
                                            questionTime,
                                            onQuizStarted,
                                        }: Props) {
    const [playersText, setPlayersText] = useState("Oyuncular bekleniyor...");
    const [questionText, setQuestionText] = useState("");
    const [options, setOptions] = useState<string[]>(["", ""]);
    const [selectedCorrectIndex, setSelectedCorrectIndex] = useState<number>(-1);
    const [questionCount, setQuestionCount] = useState(0);
    const [statusText, setStatusText] = useState("");

    useEffect(() => {
        const listener: SocketListener = {
            onMessage: (message) => {
                const json = JSON.parse(message);

                if (json.type === "player_list_updated") {
                    setPlayersText(buildPlayersText(json.players ?? []));
                }

                if (json.type === "question_added") {
                    setQuestionCount(json.question_count ?? questionCount + 1);
                    setStatusText(json.message ?? "Soru eklendi.");

                    setQuestionText("");
                    setOptions(["", ""]);
                    setSelectedCorrectIndex(-1);
                }

                if (json.type === "room_question_count_updated") {
                    setQuestionCount(json.question_count ?? questionCount);
                }

                if (json.type === "quiz_started") {
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
    }, [questionCount, onQuizStarted]);

    const addOption = () => {
        setOptions((prev) => [...prev, ""]);
    };

    const updateOption = (index: number, value: string) => {
        setOptions((prev) => {
            const copy = [...prev];
            copy[index] = value;
            return copy;
        });
    };

    const deleteOption = (index: number) => {
        if (options.length <= 2) {
            setStatusText("En az 2 şık kalmalı.");
            return;
        }

        setOptions((prev) => prev.filter((_, i) => i !== index));

        if (selectedCorrectIndex === index) {
            setSelectedCorrectIndex(-1);
        } else if (selectedCorrectIndex > index) {
            setSelectedCorrectIndex(selectedCorrectIndex - 1);
        }
    };

    const addQuestion = () => {
        const cleanQuestion = questionText.trim();

        if (!cleanQuestion) {
            setStatusText("Soru metni boş olamaz.");
            return;
        }

        if (selectedCorrectIndex === -1) {
            setStatusText("Doğru cevabı seçmelisin.");
            return;
        }

        const filledOptions: string[] = [];
        let correctIndexInFilledOptions = -1;

        options.forEach((option, originalIndex) => {
            const cleanOption = option.trim();

            if (cleanOption.length > 0) {
                if (originalIndex === selectedCorrectIndex) {
                    correctIndexInFilledOptions = filledOptions.length;
                }

                filledOptions.push(cleanOption);
            }
        });

        if (filledOptions.length < 2) {
            setStatusText("En az 2 dolu şık girmelisin.");
            return;
        }

        if (correctIndexInFilledOptions === -1) {
            setStatusText("Doğru cevap olarak seçtiğin şık boş olamaz.");
            return;
        }

        WebSocketManager.send(
            SocketMessageFactory.addQuestion(
                roomCode,
                cleanQuestion,
                filledOptions,
                correctIndexInFilledOptions
            )
        );

        setStatusText("Soru gönderildi...");
    };

    const startQuiz = () => {
        if (questionCount <= 0) {
            setStatusText("Quiz başlatmak için en az 1 soru eklemelisin.");
            return;
        }

        WebSocketManager.send(SocketMessageFactory.startQuiz(roomCode));

        setStatusText("Quiz başlatma isteği gönderildi...");
    };

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Oda Sahibi Paneli</Text>

            <Text style={styles.roomCode}>Oda Kodu: {roomCode}</Text>

            <Text style={styles.info}>
                Kullanıcı: {username}
                {"\n"}
                Soru Süresi: {questionTime} saniye
            </Text>

            <Text style={styles.players}>{playersText}</Text>

            <View style={styles.divider} />

            <Text style={styles.sectionTitle}>Soru Ekle</Text>

            <TextInput
                style={styles.questionInput}
                placeholder="Soru metni"
                value={questionText}
                onChangeText={setQuestionText}
                multiline
                textAlignVertical="top"
            />

            <Text style={styles.optionTitle}>Şıklar</Text>

            {options.map((option, index) => (
                <View key={index} style={styles.optionRow}>
                    <Pressable
                        style={[
                            styles.radio,
                            selectedCorrectIndex === index && styles.radioSelected,
                        ]}
                        onPress={() => setSelectedCorrectIndex(index)}
                    >
                        <Text style={styles.radioText}>
                            {selectedCorrectIndex === index ? "●" : "○"}
                        </Text>
                    </Pressable>

                    <TextInput
                        style={styles.optionInput}
                        placeholder={`Şık ${index + 1}`}
                        value={option}
                        onChangeText={(text) => updateOption(index, text)}
                    />

                    <Pressable
                        style={[
                            styles.deleteButton,
                            options.length <= 2 && styles.deleteButtonDisabled,
                        ]}
                        disabled={options.length <= 2}
                        onPress={() => deleteOption(index)}
                    >
                        <Text style={styles.deleteText}>Sil</Text>
                    </Pressable>
                </View>
            ))}

            <Pressable style={styles.secondaryButton} onPress={addOption}>
                <Text style={styles.secondaryButtonText}>+ Şık Ekle</Text>
            </Pressable>

            <Pressable style={styles.button} onPress={addQuestion}>
                <Text style={styles.buttonText}>Soruyu Ekle</Text>
            </Pressable>

            <Pressable style={styles.button} onPress={startQuiz}>
                <Text style={styles.buttonText}>Quizi Başlat</Text>
            </Pressable>

            <Text style={styles.count}>Eklenen soru: {questionCount}</Text>

            <Text style={styles.status}>{statusText}</Text>
        </ScrollView>
    );
}

function buildPlayersText(players: string[]): string {
    if (!players || players.length === 0) {
        return "Oyuncular bekleniyor...";
    }

    return `Oyuncular:\n\n${players
        .map((player, index) => `${index + 1}. ${player}`)
        .join("\n")}`;
}

const styles = StyleSheet.create({
    container: {
        padding: 24,
        backgroundColor: "#F8FAFC",
    },
    title: {
        fontSize: 27,
        fontWeight: "800",
        color: "#111827",
    },
    roomCode: {
        marginTop: 18,
        fontSize: 24,
        fontWeight: "800",
        color: "#6D28D9",
    },
    info: {
        marginTop: 14,
        fontSize: 15,
        color: "#374151",
        lineHeight: 22,
    },
    players: {
        marginTop: 20,
        fontSize: 15,
        color: "#111827",
        lineHeight: 22,
    },
    divider: {
        height: 1,
        backgroundColor: "#E5E7EB",
        marginVertical: 24,
    },
    sectionTitle: {
        fontSize: 22,
        fontWeight: "800",
        color: "#111827",
    },
    questionInput: {
        height: 110,
        backgroundColor: "#FFFFFF",
        borderRadius: 12,
        borderWidth: 1,
        borderColor: "#D1D5DB",
        padding: 14,
        marginTop: 14,
    },
    optionTitle: {
        fontSize: 17,
        fontWeight: "700",
        marginTop: 20,
        marginBottom: 10,
        color: "#111827",
    },
    optionRow: {
        flexDirection: "row",
        alignItems: "center",
        marginBottom: 10,
    },
    radio: {
        width: 42,
        height: 42,
        justifyContent: "center",
        alignItems: "center",
    },
    radioSelected: {},
    radioText: {
        color: "#6D28D9",
        fontSize: 22,
        fontWeight: "800",
    },
    optionInput: {
        flex: 1,
        height: 52,
        backgroundColor: "#FFFFFF",
        borderRadius: 12,
        borderWidth: 1,
        borderColor: "#D1D5DB",
        paddingHorizontal: 12,
    },
    deleteButton: {
        width: 64,
        height: 52,
        marginLeft: 8,
        backgroundColor: "#FEE2E2",
        borderRadius: 12,
        justifyContent: "center",
        alignItems: "center",
    },
    deleteButtonDisabled: {
        opacity: 0.4,
    },
    deleteText: {
        color: "#DC2626",
        fontWeight: "700",
    },
    secondaryButton: {
        height: 52,
        borderRadius: 14,
        borderWidth: 1,
        borderColor: "#6D28D9",
        justifyContent: "center",
        alignItems: "center",
        marginTop: 10,
    },
    secondaryButtonText: {
        color: "#6D28D9",
        fontWeight: "700",
    },
    button: {
        height: 56,
        backgroundColor: "#6D28D9",
        borderRadius: 14,
        justifyContent: "center",
        alignItems: "center",
        marginTop: 14,
    },
    buttonText: {
        color: "#FFFFFF",
        fontSize: 17,
        fontWeight: "700",
    },
    count: {
        marginTop: 16,
        color: "#374151",
        fontSize: 15,
    },
    status: {
        marginTop: 12,
        color: "#374151",
        fontSize: 15,
    },
});