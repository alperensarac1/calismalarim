package com.example.amiralbattijetpack.ui.placement

import androidx.lifecycle.ViewModel
import com.example.amiralbattijetpack.data.SocketManager
import com.example.amiralbattijetpack.model.BaseSocketMessage
import com.example.amiralbattijetpack.model.BoardCell
import com.example.amiralbattijetpack.model.BoardSetData
import com.example.amiralbattijetpack.model.CellState
import com.example.amiralbattijetpack.model.ErrorData
import com.example.amiralbattijetpack.model.GameStartedData
import com.example.amiralbattijetpack.model.Ship
import com.example.amiralbattijetpack.model.ShipOrientation
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class PlacementViewModel : ViewModel(), SocketManager.SocketEventListener {

    private val gson = Gson()
    private val boardSize = 10

    private val shipsToPlace = mutableListOf(
        Ship(size = 4),
        Ship(size = 3),
        Ship(size = 3),
        Ship(size = 2),
        Ship(size = 2),
        Ship(size = 1),
        Ship(size = 1)
    )

    private var currentShipIndex = 0

    private val _uiState = MutableStateFlow(PlacementUiState())
    val uiState: StateFlow<PlacementUiState> = _uiState

    init {
        createBoard()
        updateShipText()
    }

    fun initialize(roomCode: String, playerId: String, playerName: String) {
        _uiState.update {
            it.copy(
                roomCode = roomCode,
                playerId = playerId,
                playerName = playerName
            )
        }
        SocketManager.setListener(this)
    }

    fun rotateShip() {
        _uiState.update {
            it.copy(
                orientation = if (it.orientation == ShipOrientation.HORIZONTAL) {
                    ShipOrientation.VERTICAL
                } else {
                    ShipOrientation.HORIZONTAL
                }
            )
        }
    }

    fun resetBoard() {
        shipsToPlace.forEachIndexed { index, ship ->
            shipsToPlace[index] = ship.copy(placed = false)
        }
        currentShipIndex = 0
        createBoard()

        _uiState.update {
            it.copy(
                orientation = ShipOrientation.HORIZONTAL,
                statusText = "Durum: Gemileri yerleştir",
                readyEnabled = false
            )
        }

        updateShipText()
    }

    fun onCellClick(row: Int, col: Int) {
        if (currentShipIndex >= shipsToPlace.size) return

        val ship = shipsToPlace[currentShipIndex]
        val orientation = uiState.value.orientation

        if (!canPlaceShip(row, col, ship.size, orientation)) {
            _uiState.update {
                it.copy(
                    statusText = "Durum: Gemi burada konumlanamaz"
                )
            }
            return
        }

        val updatedBoard = uiState.value.boardCells.toMutableList()

        for (i in 0 until ship.size) {
            val targetRow = if (orientation == ShipOrientation.VERTICAL) row + i else row
            val targetCol = if (orientation == ShipOrientation.HORIZONTAL) col + i else col
            val index = targetRow * boardSize + targetCol

            updatedBoard[index] = updatedBoard[index].copy(state = CellState.SHIP)
        }

        shipsToPlace[currentShipIndex] = ship.copy(placed = true)
        currentShipIndex++

        val allPlaced = currentShipIndex >= shipsToPlace.size

        _uiState.update {
            it.copy(
                boardCells = updatedBoard,
                statusText = if (allPlaced) {
                    "Durum: Tüm gemiler yerleştirildi. Hazırım butonuna bas."
                } else {
                    "Durum: Gemileri yerleştir"
                },
                readyEnabled = allPlaced
            )
        }

        updateShipText()
    }

    private fun createBoard() {
        val cells = buildList {
            for (row in 0 until boardSize) {
                for (col in 0 until boardSize) {
                    add(BoardCell(row = row, col = col, state = CellState.EMPTY))
                }
            }
        }

        _uiState.update { it.copy(boardCells = cells) }
    }

    private fun canPlaceShip(
        startRow: Int,
        startCol: Int,
        shipSize: Int,
        orientation: ShipOrientation
    ): Boolean {
        val targetCells = mutableListOf<Pair<Int, Int>>()

        for (i in 0 until shipSize) {
            val row = if (orientation == ShipOrientation.VERTICAL) startRow + i else startRow
            val col = if (orientation == ShipOrientation.HORIZONTAL) startCol + i else startCol

            if (row >= boardSize || col >= boardSize) return false
            targetCells.add(row to col)
        }

        val board = uiState.value.boardCells

        for ((row, col) in targetCells) {
            for (r in row - 1..row + 1) {
                for (c in col - 1..col + 1) {
                    if (r < 0 || r >= boardSize || c < 0 || c >= boardSize) continue

                    val neighborIndex = r * boardSize + c
                    if (board[neighborIndex].state == CellState.SHIP) {
                        return false
                    }
                }
            }
        }

        return true
    }

    private fun updateShipText() {
        _uiState.update {
            it.copy(
                currentShipSizeText = if (currentShipIndex < shipsToPlace.size) {
                    "Seçili gemi: ${shipsToPlace[currentShipIndex].size} hücrelik gemi"
                } else {
                    "Seçili gemi: Tüm gemiler yerleştirildi"
                }
            )
        }
    }

    private fun buildBoardMatrix(): List<List<Int>> {
        val matrix = MutableList(boardSize) { MutableList(boardSize) { 0 } }

        uiState.value.boardCells.forEach { cell ->
            matrix[cell.row][cell.col] = if (cell.state == CellState.SHIP) 1 else 0
        }

        return matrix
    }

    fun sendBoardToServer() {
        val messageMap = mapOf(
            "type" to "SET_BOARD",
            "data" to mapOf(
                "roomCode" to uiState.value.roomCode,
                "playerId" to uiState.value.playerId,
                "board" to buildBoardMatrix()
            )
        )

        SocketManager.send(gson.toJson(messageMap))

        _uiState.update {
            it.copy(
                statusText = "Durum: Tahta gönderildi, rakip bekleniyor...",
                readyEnabled = false
            )
        }
    }

    override fun onConnected() {
        _uiState.update {
            it.copy(statusText = "Durum: Bağlantı hazır")
        }
    }

    override fun onDisconnected() {
        _uiState.update {
            it.copy(statusText = "Durum: Bağlantı kesildi")
        }
    }

    override fun onMessage(message: String) {
        android.util.Log.d("PlacementVM", "received = $message")
        handleSocketMessage(message)
    }

    override fun onError(errorMessage: String) {
        _uiState.update {
            it.copy(statusText = "Hata: $errorMessage")
        }
    }

    private fun handleSocketMessage(message: String) {
        try {
            val baseMessage = gson.fromJson(message, BaseSocketMessage::class.java)

            when (baseMessage.type) {
                "BOARD_SET" -> {
                    val data = gson.fromJson(baseMessage.data, BoardSetData::class.java)
                    _uiState.update { it.copy(statusText = data.message) }
                }

                "GAME_STARTED" -> {
                    val data = gson.fromJson(baseMessage.data, GameStartedData::class.java)
                    _uiState.update {
                        it.copy(
                            statusText = data.message,
                            navigateToGame = true,
                            firstTurnPlayerId = data.firstTurnPlayerId,
                            ownBoardJson = gson.toJson(buildBoardMatrix())
                        )
                    }
                }

                "ERROR" -> {
                    val data = gson.fromJson(baseMessage.data, ErrorData::class.java)
                    _uiState.update {
                        it.copy(
                            statusText = "Hata: ${data.message}",
                            readyEnabled = true
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

    fun consumeGameNavigation() {
        _uiState.update { it.copy(navigateToGame = false) }
    }

    override fun onCleared() {
        super.onCleared()
        SocketManager.clearListener(this)
    }
}
