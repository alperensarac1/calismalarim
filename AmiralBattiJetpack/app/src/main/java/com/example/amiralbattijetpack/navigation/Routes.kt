package com.example.amiralbattijetpack.navigation

import android.net.Uri

object Routes {
    const val LOBBY = "lobby"
    const val PLACEMENT = "placement/{roomCode}/{playerId}/{playerName}"
    const val GAME = "game/{roomCode}/{playerId}/{playerName}/{firstTurnPlayerId}/{ownBoardJson}"

    fun placementRoute(
        roomCode: String,
        playerId: String,
        playerName: String
    ): String {
        return "placement/${Uri.encode(roomCode)}/${Uri.encode(playerId)}/${Uri.encode(playerName)}"
    }

    fun gameRoute(
        roomCode: String,
        playerId: String,
        playerName: String,
        firstTurnPlayerId: String,
        ownBoardJson: String
    ): String {
        return "game/${Uri.encode(roomCode)}/${Uri.encode(playerId)}/${Uri.encode(playerName)}/${Uri.encode(firstTurnPlayerId)}/${Uri.encode(ownBoardJson)}"
    }
}

