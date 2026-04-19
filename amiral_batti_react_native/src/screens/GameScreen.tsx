import React, { useEffect, useState } from "react";
import {
    Alert,
    Button,
    SafeAreaView,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import BoardGrid from "../components/BoardGrid";
import { socketManager, SocketEventListener } from "../core/socketManager";
import { BoardCell } from "../models/boardCell";
import { CellState } from "../models/cellState";
import { ErrorData } from "../models/errorData";
import { FireResultData } from "../models/fireResultData";
import { RematchStartedData } from "../models/rematchStartedData";
import { RematchStatusData } from "../models/rematchStatusData";
import { RootStackParamList } from "../navigation/types";
import { boardFromMatrix, createEmptyBoard } from "../utils/boardUtils";

type Props = NativeStackScreenProps<RootStackParamList, "Game">;

export default function GameScreen({ route, navigation }: Props) {
    const { roomCode, playerId, playerName, firstTurnPlayerId, ownBoardMatrix } =
        route.params;

    const [ownBoardCells, setOwnBoardCells] = useState<BoardCell[]>(
        boardFromMatrix(ownBoardMatrix)
    );
    const [enemyBoardCells, setEnemyBoardCells] =
        useState<BoardCell[]>(createEmptyBoard());

    const [currentTurnPlayerId, setCurrentTurnPlayerId] =
        useState(firstTurnPlayerId);
    const [turnText, setTurnText] = useState(
        firstTurnPlayerId === playerId ? "Sıra sende" : "Rakibin sırası"
    );
    const [statusText, setStatusText] = useState("Oyun başladı");

    const [isFireRequestPending, setIsFireRequestPending] = useState(false);
    const [isRematchRequested, setIsRematchRequested] = useState(false);
    const [isGameOverDialogShown, setIsGameOverDialogShown] = useState(false);

    useEffect(() => {
        const listener: SocketEventListener = {
            onConnected() {
                setStatusText("Bağlantı aktif");
            },
            onDisconnected() {
                setStatusText("Bağlantı kesildi");
            },
            onError(errorMessage) {
                setIsFireRequestPending(false);
                setStatusText(`Hata: ${errorMessage}`);
            },
            onMessage(message) {
                const parsed = JSON.parse(message);
                const type = parsed.type as string;
                const data = parsed.data ?? {};

                switch (type) {
                    case "FIRE_RESULT": {
                        const decoded = data as FireResultData;
                        handleFireResult(decoded);
                        break;
                    }

                    case "REMATCH_STATUS": {
                        const decoded = data as RematchStatusData;
                        setStatusText(
                            `${decoded.message}\n${decoded.players
                                .map((p) => `${p.name}: ${p.wantsRematch ? "hazır" : "bekleniyor"}`)
                                .join(" | ")}`
                        );
                        break;
                    }

                    case "REMATCH_STARTED": {
                        const decoded = data as RematchStartedData;
                        setStatusText(decoded.message);
                        setIsGameOverDialogShown(false);

                        navigation.replace("Placement", {
                            roomCode,
                            playerId,
                            playerName,
                        });
                        break;
                    }

                    case "PLAYER_LEFT": {
                        setStatusText("Rakip oyundan ayrıldı");
                        Alert.alert("Rakip Ayrıldı", "Rakip oyundan çıktı. Lobiye dönmek ister misin?", [
                            {
                                text: "Lobiye Dön",
                                onPress: () => navigation.popToTop(),
                            },
                        ]);
                        break;
                    }

                    case "ERROR": {
                        const decoded = data as ErrorData;
                        setIsFireRequestPending(false);
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
    }, [navigation, playerId, playerName, roomCode]);

    useEffect(() => {
        setTurnText(currentTurnPlayerId === playerId ? "Sıra sende" : "Rakibin sırası");
    }, [currentTurnPlayerId, playerId]);

    const onEnemyCellPress = (cell: BoardCell) => {
        if (currentTurnPlayerId !== playerId) {
            setStatusText("Sıra sende değil");
            return;
        }

        if (isFireRequestPending) {
            setStatusText("Önce önceki atışın sonucunu bekle");
            return;
        }

        const index = cell.row * 10 + cell.col;
        const state = enemyBoardCells[index].state;

        if (state === CellState.HIT || state === CellState.MISS) {
            setStatusText("Bu hücreye zaten ateş ettin");
            return;
        }

        socketManager.sendMap({
            type: "FIRE",
            data: {
                roomCode,
                playerId,
                row: cell.row,
                col: cell.col,
            },
        });

        setIsFireRequestPending(true);
        setStatusText("Atış gönderildi...");
    };

    const handleFireResult = (result: FireResultData) => {
        const index = result.row * 10 + result.col;
        const shooterIsMe = result.shooterPlayerId === playerId;

        if (shooterIsMe) {
            const updated = [...enemyBoardCells];
            updated[index] = {
                ...updated[index],
                state: result.hit ? CellState.HIT : CellState.MISS,
            };
            setEnemyBoardCells(updated);
        } else {
            const updated = [...ownBoardCells];
            updated[index] = {
                ...updated[index],
                state: result.hit ? CellState.HIT : CellState.MISS,
            };
            setOwnBoardCells(updated);
        }

        setIsFireRequestPending(false);
        setStatusText(result.message);

        if (result.gameOver) {
            const isWinner = result.winnerPlayerId === playerId;
            setTurnText(isWinner ? "Oyun bitti: Kazandın" : "Oyun bitti: Kaybettin");
            setIsRematchRequested(false);

            if (!isGameOverDialogShown) {
                setIsGameOverDialogShown(true);

                Alert.alert(
                    isWinner ? "Tebrikler" : "Oyun Bitti",
                    isWinner
                        ? "Rakibin tüm gemilerini batırdın.\n\nYeniden oynamak ister misin?"
                        : "Tüm gemilerin batırıldı.\n\nYeniden oynamak ister misin?",
                    [
                        {
                            text: "Yeniden Oyna",
                            onPress: requestRematch,
                        },
                        {
                            text: "Lobiye Dön",
                            onPress: () => navigation.popToTop(),
                        },
                    ]
                );
            }
            return;
        }

        setCurrentTurnPlayerId(result.nextTurnPlayerId ?? "");
    };

    const requestRematch = () => {
        if (isRematchRequested) {
            setStatusText("Zaten yeniden oyun isteği gönderdin");
            return;
        }

        socketManager.sendMap({
            type: "REQUEST_REMATCH",
            data: {
                roomCode,
                playerId,
            },
        });

        setIsRematchRequested(true);
        setIsGameOverDialogShown(false);
        setStatusText("Yeniden oyun isteği gönderildi. Rakip bekleniyor...");
    };

    return (
        <SafeAreaView style={styles.container}>
            <ScrollView>
                <Text style={styles.title}>Oyun Ekranı</Text>

                <View style={styles.card}>
                    <Text>{turnText}</Text>
                    <Text style={styles.spaceTop}>{statusText}</Text>
                </View>

                <Text style={styles.sectionTitle}>Kendi Tahtan</Text>
                <BoardGrid cells={ownBoardCells} />

                <Text style={styles.sectionTitle}>Rakip Tahtası</Text>
                <BoardGrid cells={enemyBoardCells} onCellPress={onEnemyCellPress} />
            </ScrollView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        padding: 16,
    },
    title: {
        fontSize: 28,
        fontWeight: "700",
        marginBottom: 14,
    },
    card: {
        marginBottom: 16,
        padding: 16,
        borderRadius: 12,
        backgroundColor: "#f4f4f4",
    },
    sectionTitle: {
        fontSize: 20,
        fontWeight: "700",
        marginBottom: 8,
        marginTop: 12,
    },
    spaceTop: {
        marginTop: 8,
    },
});
