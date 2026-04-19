package com.example.amiralbattikotlin.repository

import com.example.amiralbattikotlin.network.GameWebSocketClient

class GameRepository(
    private val webSocketClient: GameWebSocketClient
) {
    fun connect() {
        webSocketClient.connect()
    }

    fun send(message: String) {
        webSocketClient.send(message)
    }

    fun disconnect() {
        webSocketClient.disconnect()
    }
}