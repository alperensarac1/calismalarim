import React from "react";
import { Pressable, StyleSheet, View } from "react-native";
import { BoardCell } from "../models/boardCell";
import { CellState } from "../models/cellState";

type Props = {
    cells: BoardCell[];
    onCellPress?: (cell: BoardCell) => void;
};

export default function BoardGrid({ cells, onCellPress }: Props) {
    return (
        <View style={styles.grid}>
            {cells.map((cell, index) => (
                <Pressable
                    key={`${cell.row}-${cell.col}-${index}`}
                    onPress={() => onCellPress?.(cell)}
                    style={[
                        styles.cell,
                        {
                            backgroundColor: getCellColor(cell.state),
                        },
                    ]}
                />
            ))}
        </View>
    );
}

function getCellColor(state: CellState) {
    switch (state) {
        case CellState.EMPTY:
            return "#D9EAF7";
        case CellState.SHIP:
            return "#5B7C99";
        case CellState.HIT:
            return "red";
        case CellState.MISS:
            return "white";
    }
}

const styles = StyleSheet.create({
    grid: {
        flexDirection: "row",
        flexWrap: "wrap",
        gap: 2,
    },
    cell: {
        width: "9.2%",
        aspectRatio: 1,
        borderRadius: 4,
        borderWidth: 1,
        borderColor: "#ddd",
    },
});
