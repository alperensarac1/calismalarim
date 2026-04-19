import React, { useEffect, useMemo, useState } from "react";
import { Alert, Button, SafeAreaView, ScrollView, StyleSheet, Text, View } from "react-native";
import { NativeStackScreenProps } from "@react-navigation/native-stack";

import BoardGrid from "../components/BoardGrid";
import { socketManager, SocketEventListener } from "../core/socketManager";
import { BoardSetData } from "../models/boardSetData";
import { ErrorData } from "../models/errorData";
import { GameStartedData } from "../models/gameStartedData";
import { BoardCell } from "../models/boardCell";
import { Ship } from "../models/ship";
import { ShipOrientation } from "../models/shipOrientation";
import { RootStackParamList } from "../navigation/types";
import {
    buildBoardMatrix,
    canPlaceShip,
    createEmptyBoard,
    placeShipOnBoard,
} from "../utils/boardUtils";

type Props = NativeStackScreenProps<RootStackParamList, "Placement">;

export default function PlacementScreen({ route, navigation }: Props) {
    const { roomCode, playerId, playerName } = route.params;

    const initialShips = useMemo<Ship[]>(
        () => [
            { size: 4, placed: false },
            { size: 3, placed: false },
            { size: 3, placed: false },
            { size: 2, placed: false },
            { size: 2, placed: false },
            { size: 1, placed: false },
            { size: 1, placed: false },
        ],
        []
    );

    const [boardCells, setBoardCells] = useState<BoardCell[]>(createEmptyBoard());
    const [shipsToPlace, setShipsToPlace] = useState<Ship[]>(initialShips);
    const [currentShipIndex, setCurrentShipIndex] = useState(0);
    const [orientation, setOrientation] = useState<ShipOrientation>(
        ShipOrientation.HORIZONTAL
    );
    const [statusText, setStatusText] = useState("Durum: Gemileri yerleştir");
    const [readyEnabled, setReadyEnabled] = useState(false);

    useEffect(() => {
        const listener: SocketEventListener = {
            onConnected() {
                setStatusText("Durum: Bağlantı hazır");
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
                    case "BOARD_SET": {
                        const decoded = data as BoardSetData;
                        setStatusText(decoded.message);
                        break;
                    }

                    case "GAME_STARTED": {
                        const decoded = data as GameStartedData;
                        navigation.navigate("Game", {
                            roomCode,
                            playerId,
                            playerName,
                            firstTurnPlayerId: decoded.firstTurnPlayerId,
                            ownBoardMatrix: buildBoardMatrix(boardCells),
                        });
                        break;
                    }

                    case "ERROR": {
                        const decoded = data as ErrorData;
                        setStatusText(`Hata: ${decoded.message}`);
                        setReadyEnabled(true);
                        break;
                    }
                }
            },
        };

        socketManager.setListener(listener);

        return () => {
            socketManager.clearListener(listener);
        };
    }, [boardCells, navigation, playerId, playerName, roomCode]);

    const currentShipText =
        currentShipIndex < shipsToPlace.length
            ? `Seçili gemi: ${shipsToPlace[currentShipIndex].size} hücrelik gemi`
            : "Seçili gemi: Tüm gemiler yerleştirildi";

    const orientationText =
        orientation === ShipOrientation.HORIZONTAL ? "Yön: Yatay" : "Yön: Dikey";

    const resetBoard = () => {
        setBoardCells(createEmptyBoard());
        setShipsToPlace(initialShips);
        setCurrentShipIndex(0);
        setOrientation(ShipOrientation.HORIZONTAL);
        setStatusText("Durum: Gemileri yerleştir");
        setReadyEnabled(false);
    };

    const handleCellPress = (cell: BoardCell) => {
        if (currentShipIndex >= shipsToPlace.length) {
            return;
        }

        const currentShip = shipsToPlace[currentShipIndex];

        if (
            !canPlaceShip(
                boardCells,
                cell.row,
                cell.col,
                currentShip.size,
                orientation
            )
        ) {
            setStatusText("Durum: Gemi burada konumlanamaz");
            return;
        }

        const updatedBoard = placeShipOnBoard(
            boardCells,
            cell.row,
            cell.col,
            currentShip.size,
            orientation
        );

        const updatedShips = [...shipsToPlace];
        updatedShips[currentShipIndex] = {
            ...updatedShips[currentShipIndex],
            placed: true,
        };

        const nextIndex = currentShipIndex + 1;
        const allPlaced = nextIndex >= updatedShips.length;

        setBoardCells(updatedBoard);
        setShipsToPlace(updatedShips);
        setCurrentShipIndex(nextIndex);
        setReadyEnabled(allPlaced);
        setStatusText(
            allPlaced
                ? "Durum: Tüm gemiler yerleştirildi. Hazırım butonuna bas."
                : "Durum: Gemileri yerleştir"
        );
    };

    const sendBoardToServer = () => {
        socketManager.sendMap({
            type: "SET_BOARD",
            data: {
                roomCode,
                playerId,
                board: buildBoardMatrix(boardCells),
            },
        });

        setStatusText("Durum: Tahta gönderildi, rakip bekleniyor...");
        setReadyEnabled(false);
    };

    return (
        <SafeAreaView style={styles.container}>
            <ScrollView>
                <Text style={styles.title}>Gemi Yerleştirme</Text>

                <View style={styles.card}>
                    <Text>Oda: {roomCode}</Text>
                    <Text style={styles.spaceTop}>Oyuncu: {playerName}</Text>
                    <Text style={styles.spaceTop}>{currentShipText}</Text>
                    <Text style={styles.spaceTop}>{orientationText}</Text>
                </View>

                <View style={styles.buttonGap}>
                    <Button
                        title="Yönü Değiştir"
                        onPress={() =>
                            setOrientation((prev) =>
                                prev === ShipOrientation.HORIZONTAL
                                    ? ShipOrientation.VERTICAL
                                    : ShipOrientation.HORIZONTAL
                            )
                        }
                    />
                </View>

                <View style={styles.buttonGap}>
                    <Button title="Tahtayı Sıfırla" onPress={resetBoard} />
                </View>

                <BoardGrid cells={boardCells} onCellPress={handleCellPress} />

                <View style={styles.card}>
                    <Text>{statusText}</Text>
                </View>

                <View style={styles.buttonGap}>
                    <Button
                        title="Hazırım"
                        onPress={sendBoardToServer}
                        disabled={!readyEnabled}
                    />
                </View>
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
        marginBottom: 12,
        padding: 16,
        borderRadius: 12,
        backgroundColor: "#f4f4f4",
    },
    buttonGap: {
        marginBottom: 12,
    },
    spaceTop: {
        marginTop: 6,
    },
});
