package com.example.amiralbattijetpack.ui.lobby

import com.example.amiralbattijetpack.model.PlayerInfo

data class LobbyUiState(
    val playerName: String = "",
    val roomCode: String = "",
    val roomInfo: String = "Oda: -",
    val playersText: String = "Oyuncular: -",
    val statusText: String = "Durum: Hazır",
    val players: List<PlayerInfo> = emptyList(),
    val currentRoomCode: String = "",
    val currentPlayerId: String = "",
    val shouldNavigateToPlacement: Boolean = false
)
