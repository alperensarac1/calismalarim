package com.example.amiralbattijetpack.ui.lobby

import androidx.lifecycle.ViewModel
import com.example.amiralbattijetpack.data.SocketManager
import com.example.amiralbattijetpack.model.BaseSocketMessage
import com.example.amiralbattijetpack.model.ErrorData
import com.example.amiralbattijetpack.model.JoinedRoomData
import com.example.amiralbattijetpack.model.PlayerInfo
import com.example.amiralbattijetpack.model.PlayerJoinedData
import com.example.amiralbattijetpack.model.RoomCreatedData
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class LobbyViewModel : ViewModel(), SocketManager.SocketEventListener {

    private val gson = Gson()

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState

    init {
        SocketManager.setListener(this)
    }

    fun updatePlayerName(value: String) {
        _uiState.update { it.copy(playerName = value) }
    }

    fun updateRoomCode(value: String) {
        _uiState.update { it.copy(roomCode = value) }
    }

    fun connectToServer() {
        _uiState.update { it.copy(statusText = "Durum: Sunucuya bağlanılıyor...") }
        SocketManager.setListener(this)
        SocketManager.connect()
    }

    fun createRoom() {
        val playerName = uiState.value.playerName.trim()
        if (playerName.isEmpty()) {
            _uiState.update { it.copy(statusText = "Hata: Oyuncu adı gir") }
            return
        }

        val messageMap = mapOf(
            "type" to "CREATE_ROOM",
            "data" to mapOf("playerName" to playerName)
        )

        SocketManager.send(gson.toJson(messageMap))
    }

    fun joinRoom() {
        val playerName = uiState.value.playerName.trim()
        val roomCode = uiState.value.roomCode.trim()

        if (playerName.isEmpty() || roomCode.isEmpty()) {
            _uiState.update { it.copy(statusText = "Hata: Oyuncu adı ve oda kodu gir") }
            return
        }

        val messageMap = mapOf(
            "type" to "JOIN_ROOM",
            "data" to mapOf(
                "playerName" to playerName,
                "roomCode" to roomCode
            )
        )

        SocketManager.send(gson.toJson(messageMap))
    }

    override fun onConnected() {
        _uiState.update { it.copy(statusText = "Durum: Sunucuya bağlandı") }
    }

    override fun onDisconnected() {
        _uiState.update { it.copy(statusText = "Durum: Bağlantı kesildi") }
    }

    override fun onMessage(message: String) {
        android.util.Log.d("LobbyVM", "received = $message")
        handleSocketMessage(message)
    }

    override fun onError(errorMessage: String) {
        _uiState.update { it.copy(statusText = "Hata: $errorMessage") }
    }

    private fun handleSocketMessage(message: String) {
        try {
            val baseMessage = gson.fromJson(message, BaseSocketMessage::class.java)

            when (baseMessage.type) {
                "ROOM_CREATED" -> {
                    val data = gson.fromJson(baseMessage.data, RoomCreatedData::class.java)

                    _uiState.update {
                        it.copy(
                            roomCode = data.roomCode,
                            roomInfo = "Oda: ${data.roomCode}",
                            playersText = "Oyuncular: ${formatPlayers(data.players)}",
                            statusText = data.message,
                            players = data.players,
                            currentRoomCode = data.roomCode,
                            currentPlayerId = data.playerId
                        )
                    }
                }

                "JOINED_ROOM" -> {
                    val data = gson.fromJson(baseMessage.data, JoinedRoomData::class.java)

                    _uiState.update {
                        it.copy(
                            roomCode = data.roomCode,
                            roomInfo = "Oda: ${data.roomCode}",
                            playersText = "Oyuncular: ${formatPlayers(data.players)}",
                            statusText = data.message,
                            players = data.players,
                            currentRoomCode = data.roomCode,
                            currentPlayerId = data.playerId
                        )
                    }
                }

                "PLAYER_JOINED" -> {
                    val data = gson.fromJson(baseMessage.data, PlayerJoinedData::class.java)

                    _uiState.update {
                        it.copy(
                            roomInfo = "Oda: ${data.roomCode}",
                            playersText = "Oyuncular: ${formatPlayers(data.players)}",
                            statusText = data.message,
                            players = data.players,
                            shouldNavigateToPlacement = data.players.size == 2
                        )
                    }
                }

                "PLAYER_LEFT" -> {
                    val data = gson.fromJson(baseMessage.data, PlayerJoinedData::class.java)

                    _uiState.update {
                        it.copy(
                            playersText = "Oyuncular: ${formatPlayers(data.players)}",
                            statusText = data.message,
                            players = data.players
                        )
                    }
                }

                "ERROR" -> {
                    val data = gson.fromJson(baseMessage.data, ErrorData::class.java)
                    _uiState.update { it.copy(statusText = "Hata: ${data.message}") }
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(statusText = "Mesaj ayrıştırılamadı: ${e.message}") }
        }
    }

    private fun formatPlayers(players: List<PlayerInfo>): String {
        if (players.isEmpty()) return "-"
        return players.joinToString(" | ") { it.name }
    }

    fun consumePlacementNavigation() {
        _uiState.update { it.copy(shouldNavigateToPlacement = false) }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.clearListener(this)
    }
}
