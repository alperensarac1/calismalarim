package com.example.amiralbattijetpack.ui.game

import com.example.amiralbattijetpack.model.BoardCell

data class GameUiState(
    val roomCode: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val firstTurnPlayerId: String = "",
    val currentTurnPlayerId: String = "",
    val ownBoardCells: List<BoardCell> = emptyList(),
    val enemyBoardCells: List<BoardCell> = emptyList(),
    val turnText: String = "Sıra bilgisi",
    val statusText: String = "Durum",
    val isFireRequestPending: Boolean = false,
    val isRematchRequested: Boolean = false,
    val showGameOverDialog: Boolean = false,
    val gameOverWinner: Boolean = false,
    val showPlayerLeftDialog: Boolean = false,
    val navigateToPlacement: Boolean = false
)
