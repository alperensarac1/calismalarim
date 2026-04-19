package com.example.amiralbattijetpack.ui.placement

import com.example.amiralbattijetpack.model.BoardCell
import com.example.amiralbattijetpack.model.ShipOrientation

data class PlacementUiState(
    val roomCode: String = "",
    val playerId: String = "",
    val playerName: String = "",
    val boardCells: List<BoardCell> = emptyList(),
    val currentShipSizeText: String = "Seçili gemi: -",
    val orientation: ShipOrientation = ShipOrientation.HORIZONTAL,
    val statusText: String = "Durum: Gemileri yerleştir",
    val readyEnabled: Boolean = false,
    val navigateToGame: Boolean = false,
    val firstTurnPlayerId: String = "",
    val ownBoardJson: String = ""
)
