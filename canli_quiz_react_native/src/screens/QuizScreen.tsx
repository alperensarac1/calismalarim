import React, { useEffect, useRef, useState } from "react";
import {
    ScrollView,
    Text,
    Pressable,
    StyleSheet,
    View,
} from "react-native";

import { QuestionData, ScoreItem, SocketListener } from "../models/types";
import { SocketMessageFactory } from "../socket/SocketMessageFactory";
import { WebSocketManager } from "../socket/WebSocketManager";

type Props = {
    roomCode: string;
    username: string;
    questionTime: number;
    onQuizFinished: (winners: ScoreItem[], scoreboard: ScoreItem[]) => void;
};

type OptionVisualState = "normal" | "waiting" | "correct" | "wrong";

export default function QuizScreen({
                                       roomCode,
                                       username,
                                       questionTime,
                                       onQuizFinished,
                                   }: Props) {
    const [questionData, setQuestionData] = useState<QuestionData | null>(null);
    const [remainingTime, setRemainingTime] = useState(questionTime);

    const [selectedAnswerIndex, setSelectedAnswerIndex] = useState(-1);
    const [currentCorrectIndex, setCurrentCorrectIndex] = useState(-1);
    const [answeredCurrentQuestion, setAnsweredCurrentQuestion] = useState(false);

    const [answerResultText, setAnswerResultText] = useState("");
    const [scoreboardText, setScoreboardText] =
        useState("Puan tablosu bekleniyor...");

    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

    useEffect(() => {
        const listener: SocketListener = {
            onMessage: (message) => {
                const json = JSON.parse(message);

                if (json.type === "new_question") {
                    handleNewQuestion(json);
                }

                if (json.type === "answer_result") {
                    handleAnswerResult(json);
                }

                if (json.type === "scoreboard_updated") {
                    setScoreboardText(buildScoreboardText(json.scoreboard ?? []));
                }

                if (json.type === "time_up") {
                    handleTimeUp(json);
                }

                if (json.type === "quiz_finished") {
                    clearTimer();

                    onQuizFinished(json.winners ?? [], json.scoreboard ?? []);
                }

                if (json.type === "answer_rejected") {
                    setAnswerResultText(json.message ?? "Cevap reddedildi.");
                }

                if (json.type === "error") {
                    setAnswerResultText(json.message ?? "Bilinmeyen hata oluştu.");
                }
            },

            onClose: () => {
                setAnswerResultText("Sunucu bağlantısı kapandı.");
            },

            onError: (error) => {
                setAnswerResultText(`Bağlantı hatası: ${error}`);
            },
        };

        WebSocketManager.setListener(listener);

        return () => {
            clearTimer();
            WebSocketManager.removeListener(listener);
        };
    }, [onQuizFinished]);

    const clearTimer = () => {
        if (timerRef.current) {
            clearInterval(timerRef.current);
            timerRef.current = null;
        }
    };

    const startLocalTimer = (seconds: number) => {
        clearTimer();

        setRemainingTime(seconds);

        let remaining = seconds;

        timerRef.current = setInterval(() => {
            remaining -= 1;

            if (remaining <= 0) {
                setRemainingTime(0);
                clearTimer();
            } else {
                setRemainingTime(remaining);
            }
        }, 1000);
    };

    const handleNewQuestion = (json: any) => {
        const question: QuestionData = {
            questionNumber: json.question_number ?? 0,
            totalQuestions: json.total_questions ?? 0,
            questionText: json.question_text ?? "",
            options: json.options ?? [],
            questionTime: json.question_time ?? questionTime,
        };

        setQuestionData(question);
        setSelectedAnswerIndex(-1);
        setCurrentCorrectIndex(-1);
        setAnsweredCurrentQuestion(false);
        setAnswerResultText("");
        setScoreboardText(buildScoreboardText(json.scoreboard ?? []));

        startLocalTimer(question.questionTime);
    };

    const handleAnswerResult = (json: any) => {
        const isCorrect = Boolean(json.is_correct);
        const earnedScore = Number(json.earned_score ?? 0);
        const totalScore = Number(json.total_score ?? 0);

        if (isCorrect) {
            setAnswerResultText(
                `Doğru cevap! +${earnedScore} puan | Toplam: ${totalScore}`
            );
        } else {
            setAnswerResultText("Yanlış cevap. Puan kazanamadın.");
        }
    };

    const handleTimeUp = (json: any) => {
        clearTimer();

        const correctIndex = Number(json.correct_index ?? -1);

        setRemainingTime(0);
        setCurrentCorrectIndex(correctIndex);

        if (correctIndex >= 0) {
            setAnswerResultText(`Süre bitti. Doğru cevap: ${indexToLetter(correctIndex)}`);
        } else {
            setAnswerResultText("Süre bitti.");
        }

        setScoreboardText(buildScoreboardText(json.scoreboard ?? []));
    };

    const submitAnswer = (answerIndex: number) => {
        if (answeredCurrentQuestion) {
            setAnswerResultText("Bu soruya zaten cevap verdin.");
            return;
        }

        setAnsweredCurrentQuestion(true);
        setSelectedAnswerIndex(answerIndex);
        setAnswerResultText("Cevabın gönderildi...");

        WebSocketManager.send(
            SocketMessageFactory.submitAnswer(roomCode, username, answerIndex)
        );
    };

    const getOptionVisualState = (index: number): OptionVisualState => {
        if (currentCorrectIndex >= 0 && index === currentCorrectIndex) {
            return "correct";
        }

        if (
            currentCorrectIndex >= 0 &&
            selectedAnswerIndex >= 0 &&
            index === selectedAnswerIndex &&
            selectedAnswerIndex !== currentCorrectIndex
        ) {
            return "wrong";
        }

        if (
            answeredCurrentQuestion &&
            selectedAnswerIndex === index &&
            currentCorrectIndex === -1
        ) {
            return "waiting";
        }

        return "normal";
    };

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <Text style={styles.title}>Quiz</Text>

            {!questionData ? (
                <Text style={styles.waiting}>Soru bekleniyor...</Text>
            ) : (
                <View>
                    <Text style={styles.counter}>
                        Soru {questionData.questionNumber} / {questionData.totalQuestions}
                    </Text>

                    <Text style={styles.timer}>
                        {remainingTime > 0 ? `Süre: ${remainingTime}` : "Süre bitti"}
                    </Text>

                    <Text style={styles.question}>{questionData.questionText}</Text>

                    {questionData.options.map((option, index) => (
                        <Pressable
                            key={index}
                            disabled={answeredCurrentQuestion || remainingTime <= 0}
                            style={[
                                styles.optionButton,
                                getOptionStyle(getOptionVisualState(index)),
                            ]}
                            onPress={() => submitAnswer(index)}
                        >
                            <Text
                                style={[
                                    styles.optionText,
                                    getOptionTextStyle(getOptionVisualState(index)),
                                ]}
                            >
                                {indexToLetter(index)}) {option}
                            </Text>
                        </Pressable>
                    ))}

                    <Text style={styles.answerResult}>{answerResultText}</Text>

                    <Text style={styles.scoreboard}>{scoreboardText}</Text>
                </View>
            )}
        </ScrollView>
    );
}

function buildScoreboardText(scoreboard: ScoreItem[]): string {
    if (!scoreboard || scoreboard.length === 0) {
        return "Puan tablosu bekleniyor...";
    }

    return `Puan Tablosu:\n\n${scoreboard
        .map((item, index) => `${index + 1}. ${item.username} - ${item.score} puan`)
        .join("\n")}`;
}

function indexToLetter(index: number): string {
    if (index >= 0 && index < 26) {
        return String.fromCharCode(65 + index);
    }

    return String(index + 1);
}

function getOptionStyle(state: OptionVisualState) {
    if (state === "waiting") {
        return {
            backgroundColor: "#FEF3C7",
            borderColor: "#F59E0B",
        };
    }

    if (state === "correct") {
        return {
            backgroundColor: "#DCFCE7",
            borderColor: "#16A34A",
        };
    }

    if (state === "wrong") {
        return {
            backgroundColor: "#FEE2E2",
            borderColor: "#DC2626",
        };
    }

    return {
        backgroundColor: "#FFFFFF",
        borderColor: "#D1D5DB",
    };
}

function getOptionTextStyle(state: OptionVisualState) {
    if (state === "waiting") {
        return { color: "#92400E" };
    }

    if (state === "correct") {
        return { color: "#166534" };
    }

    if (state === "wrong") {
        return { color: "#991B1B" };
    }

    return { color: "#111827" };
}

const styles = StyleSheet.create({
    container: {
        padding: 24,
        backgroundColor: "#F8FAFC",
        flexGrow: 1,
    },
    title: {
        fontSize: 27,
        fontWeight: "800",
        color: "#111827",
    },
    waiting: {
        marginTop: 24,
        fontSize: 18,
        color: "#374151",
    },
    counter: {
        marginTop: 8,
        fontSize: 15,
        color: "#6B7280",
    },
    timer: {
        marginTop: 18,
        fontSize: 24,
        fontWeight: "800",
        color: "#DC2626",
    },
    question: {
        marginTop: 22,
        fontSize: 21,
        fontWeight: "800",
        color: "#111827",
        lineHeight: 28,
    },
    optionButton: {
        minHeight: 58,
        borderWidth: 1,
        borderRadius: 14,
        justifyContent: "center",
        alignItems: "center",
        paddingHorizontal: 14,
        marginTop: 14,
    },
    optionText: {
        fontSize: 16,
        fontWeight: "800",
        textAlign: "center",
    },
    answerResult: {
        marginTop: 22,
        fontSize: 17,
        fontWeight: "800",
        color: "#374151",
    },
    scoreboard: {
        marginTop: 24,
        fontSize: 15,
        color: "#111827",
        lineHeight: 22,
    },
});