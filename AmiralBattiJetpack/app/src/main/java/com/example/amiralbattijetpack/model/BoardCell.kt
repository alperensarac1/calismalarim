package com.example.amiralbattijetpack.model

data class BoardCell(
    val row: Int,
    val col: Int,
    val state: CellState = CellState.EMPTY
)
