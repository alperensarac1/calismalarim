package com.example.amiralbattikotlin.model

data class BoardCell(
    val row: Int,
    val col: Int,
    var state: CellState = CellState.EMPTY
)