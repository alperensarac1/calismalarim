import {BoardCell} from "../models/boardCell";
import {CellState} from "../models/cellState";
import {ShipOrientation} from "../models/shipOrientation";

export const BOARD_SIZE = 10;

export function createEmptyBoard(): BoardCell[] {
    const cells: BoardCell[] = [];
    for (let row = 0; row < BOARD_SIZE; row++) {
        for (let col = 0; col < BOARD_SIZE; col++) {
            cells.push({ row, col, state: CellState.EMPTY });
        }
    }
    return cells;
}

export function canPlaceShip(
    board: BoardCell[],
    startRow: number,
    startCol: number,
    shipSize: number,
    orientation: ShipOrientation
): boolean {
    const targetCells: Array<{ row: number; col: number }> = [];

    for (let i = 0; i < shipSize; i++) {
        const row = orientation === ShipOrientation.VERTICAL ? startRow + i : startRow;
        const col = orientation === ShipOrientation.HORIZONTAL ? startCol + i : startCol;

        if (row >= BOARD_SIZE || col >= BOARD_SIZE) {
            return false;
        }

        targetCells.push({ row, col });
    }

    for (const target of targetCells) {
        for (let r = target.row - 1; r <= target.row + 1; r++) {
            for (let c = target.col - 1; c <= target.col + 1; c++) {
                if (r < 0 || r >= BOARD_SIZE || c < 0 || c >= BOARD_SIZE) continue;

                const index = r * BOARD_SIZE + c;
                if (board[index].state === CellState.SHIP) {
                    return false;
                }
            }
        }
    }

    return true;
}

export function placeShipOnBoard(
    board: BoardCell[],
    startRow: number,
    startCol: number,
    shipSize: number,
    orientation: ShipOrientation
): BoardCell[] {
    const updated = [...board];

    for (let i = 0; i < shipSize; i++) {
        const row = orientation === ShipOrientation.VERTICAL ? startRow + i : startRow;
        const col = orientation === ShipOrientation.HORIZONTAL ? startCol + i : startCol;
        const index = row * BOARD_SIZE + col;

        updated[index] = {
            ...updated[index],
            state: CellState.SHIP,
        };
    }

    return updated;
}

export function buildBoardMatrix(board: BoardCell[]): number[][] {
    const matrix = Array.from({ length: BOARD_SIZE }, () =>
        Array.from({ length: BOARD_SIZE }, () => 0)
    );

    for (const cell of board) {
        matrix[cell.row][cell.col] = cell.state === CellState.SHIP ? 1 : 0;
    }

    return matrix;
}

export function boardFromMatrix(matrix: number[][]): BoardCell[] {
    const board: BoardCell[] = [];
    for (let row = 0; row < BOARD_SIZE; row++) {
        for (let col = 0; col < BOARD_SIZE; col++) {
            const value = matrix?.[row]?.[col] ?? 0;
            board.push({
                row,
                col,
                state: value === 1 ? CellState.SHIP : CellState.EMPTY,
            });
        }
    }
    return board;
}

