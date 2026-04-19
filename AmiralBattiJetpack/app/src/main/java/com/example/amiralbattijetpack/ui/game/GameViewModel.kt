package com.example.amiralbattijetpack.ui.game

import androidx.lifecycle.ViewModel
import com.example.amiralbattijetpack.data.SocketManager
import com.example.amiralbattijetpack.model.BaseSocketMessage
import com.example.amiralbattijetpack.model.BoardCell
import com.example.amiralbattijetpack.model.CellState
import com.example.amiralbattijetpack.model.ErrorData
import com.example.amiralbattijetpack.model.FireResultData
import com.example.amiralbattijetpack.model.RematchPlayerInfo
import com.example.amiralbattijetpack.model.RematchStartedData
import com.example.amiralbattijetpack.model.RematchStatusData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel(), SocketManager.SocketEventListener {

    private val gson = Gson()
    private val boardSize = 10

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    fun initialize(
        roomCode: String,
        playerId: String,
        playerName: String,
        firstTurnPlayerId: String,
        ownBoardJson: String
    ) {
        val ownBoard = parseOwnBoard(ownBoardJson)
        val enemyBoard = buildEmptyBoard()

        _uiState.update {
            it.copy(
                roomCode = roomCode,
                playerId = playerId,
                playerName = playerName,
                firstTurnPlayerId = firstTurnPlayerId,
                currentTurnPlayerId = firstTurnPlayerId,
                ownBoardCells = ownBoard,
                enemyBoardCells = enemyBoard,
                turnText = if (playerId == firstTurnPlayerId) "Sıra sende" else "Rakibin sırası",
                statusText = "Oyun başladı"
            )
        }

        SocketManager.setListener(this)
    }

    private fun parseOwnBoard(ownBoardJson: String): List<BoardCell> {
        val type = object : TypeToken<List<List<Int>>>() {}.type
        val matrix: List<List<Int>> = gson.fromJson(ownBoardJson, type)

        val cells = mutableListOf<BoardCell>()

        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val value = matrix.getOrNull(row)?.getOrNull(col) ?: 0
                cells.add(
                    BoardCell(
                        row = row,
                        col = col,
                        state = if (value == 1) CellState.SHIP else CellState.EMPTY
                    )
                )
            }
        }

        return cells
    }

    private fun buildEmptyBoard(): List<BoardCell> {
        return buildList {
            for (row in 0 until boardSize) {
                for (col in 0 until boardSize) {
                    add(BoardCell(row = row, col = col, state = CellState.EMPTY))
                }
            }
        }
    }

    fun onEnemyCellClick(row: Int, col: Int) {
        val state = uiState.value

        if (state.currentTurnPlayerId != state.playerId) {
            _uiState.update { it.copy(statusText = "Sıra sende değil") }
            return
        }

        if (state.isFireRequestPending) {
            _uiState.update { it.copy(statusText = "Önce önceki atışın sonucunu bekle") }
            return
        }

        val index = row * boardSize + col
        val cellState = state.enemyBoardCells[index].state

        if (cellState == CellState.HIT || cellState == CellState.MISS) {
            _uiState.update { it.copy(statusText = "Bu hücreye zaten ateş ettin") }
            return
        }

        val messageMap = mapOf(
            "type" to "FIRE",
            "data" to mapOf(
                "roomCode" to state.roomCode,
                "playerId" to state.playerId,
                "row" to row,
                "col" to col
            )
        )

        SocketManager.send(gson.toJson(messageMap))

        _uiState.update {
            it.copy(
                isFireRequestPending = true,
                statusText = "Atış gönderildi..."
            )
        }
    }

    private fun handleFireResult(data: FireResultData) {
        val currentState = uiState.value
        val index = data.row * boardSize + data.col

        if (index !in 0 until boardSize * boardSize) return

        val updatedOwnBoard = currentState.ownBoardCells.toMutableList()
        val updatedEnemyBoard = currentState.enemyBoardCells.toMutableList()

        val shooterIsMe = data.shooterPlayerId == currentState.playerId

        if (shooterIsMe) {
            updatedEnemyBoard[index] = updatedEnemyBoard[index].copy(
                state = if (data.hit) CellState.HIT else CellState.MISS
            )
        } else {
            updatedOwnBoard[index] = updatedOwnBoard[index].copy(
                state = if (data.hit) CellState.HIT else CellState.MISS
            )
        }

        val nextTurnPlayerId = data.nextTurnPlayerId.orEmpty()

        _uiState.update {
            it.copy(
                ownBoardCells = updatedOwnBoard,
                enemyBoardCells = updatedEnemyBoard,
                isFireRequestPending = false,
                currentTurnPlayerId = nextTurnPlayerId,
                turnText = if (!data.gameOver) {
                    if (nextTurnPlayerId == currentState.playerId) "Sıra sende" else "Rakibin sırası"
                } else {
                    if (data.winnerPlayerId == currentState.playerId) {
                        "Oyun bitti: Kazandın"
                    } else {
                        "Oyun bitti: Kaybettin"
                    }
                },
                statusText = data.message,
                isRematchRequested = if (data.gameOver) false else it.isRematchRequested,
                showGameOverDialog = data.gameOver,
                gameOverWinner = data.winnerPlayerId == currentState.playerId
            )
        }
    }

    fun requestRematch() {
        val state = uiState.value

        if (state.isRematchRequested) {
            _uiState.update { it.copy(statusText = "Zaten yeniden oyun isteği gönderdin") }
            return
        }

        val messageMap = mapOf(
            "type" to "REQUEST_REMATCH",
            "data" to mapOf(
                "roomCode" to state.roomCode,
                "playerId" to state.playerId
            )
        )

        SocketManager.send(gson.toJson(messageMap))

        _uiState.update {
            it.copy(
                isRematchRequested = true,
                showGameOverDialog = false,
                statusText = "Yeniden oyun isteği gönderildi. Rakip bekleniyor..."
            )
        }
    }

    fun dismissGameOverDialog() {
        _uiState.update { it.copy(showGameOverDialog = false) }
    }

    fun dismissPlayerLeftDialog() {
        _uiState.update { it.copy(showPlayerLeftDialog = false) }
    }

    fun consumePlacementNavigation() {
        _uiState.update { it.copy(navigateToPlacement = false) }
    }

    override fun onConnected() {
        _uiState.update { it.copy(statusText = "Bağlantı aktif") }
    }

    override fun onDisconnected() {
        _uiState.update { it.copy(statusText = "Bağlantı kesildi") }
    }

    override fun onMessage(message: String) {
        android.util.Log.d("GameVM", "received = $message")
        handleSocketMessage(message)
    }

    override fun onError(errorMessage: String) {
        _uiState.update {
            it.copy(
                statusText = "Hata: $errorMessage",
                isFireRequestPending = false
            )
        }
    }

    private fun handleSocketMessage(message: String) {
        try {
            val baseMessage = gson.fromJson(message, BaseSocketMessage::class.java)

            when (baseMessage.type) {
                "FIRE_RESULT" -> {
                    val data = gson.fromJson(baseMessage.data, FireResultData::class.java)
                    handleFireResult(data)
                }

                "REMATCH_STATUS" -> {
                    val data = gson.fromJson(baseMessage.data, RematchStatusData::class.java)
                    _uiState.update {
                        it.copy(
                            statusText = data.message + "\n" + formatRematchPlayers(data.players)
                        )
                    }
                }

                "REMATCH_STARTED" -> {
                    val data = gson.fromJson(baseMessage.data, RematchStartedData::class.java)
                    _uiState.update {
                        it.copy(
                            statusText = data.message,
                            navigateToPlacement = true,
                            showGameOverDialog = false
                        )
                    }
                }

                "PLAYER_LEFT" -> {
                    _uiState.update {
                        it.copy(
                            statusText = "Rakip oyundan ayrıldı",
                            showPlayerLeftDialog = true
                        )
                    }
                }

                "ERROR" -> {
                    val data = gson.fromJson(baseMessage.data, ErrorData::class.java)
                    _uiState.update {
                        it.copy(
                            statusText = "Hata: ${data.message}",
                            isFireRequestPending = false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(statusText = "Mesaj okunamadı: ${e.message}")
            }
        }
    }

    private fun formatRematchPlayers(players: List<RematchPlayerInfo>): String {
        if (players.isEmpty()) return "-"

        return players.joinToString(" | ") { player ->
            "${player.name}: ${if (player.wantsRematch) "hazır" else "bekleniyor"}"
        }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.clearListener(this)
    }
}
