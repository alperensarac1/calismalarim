import {CellState} from "./cellState";

export interface BoardCell {
    row: number;
    col: number;
    state: CellState;
}
