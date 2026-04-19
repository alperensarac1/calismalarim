package com.example.amiralbattikotlin.ui.placement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amiralbattikotlin.R
import com.example.amiralbattikotlin.manager.SocketManager
import com.example.amiralbattikotlin.model.BaseSocketMessage
import com.example.amiralbattikotlin.model.BoardCell
import com.example.amiralbattikotlin.model.BoardSetData
import com.example.amiralbattikotlin.model.CellState
import com.example.amiralbattikotlin.model.ErrorData
import com.example.amiralbattikotlin.model.GameStartedData
import com.example.amiralbattikotlin.model.Ship
import com.example.amiralbattikotlin.model.ShipOrientation
import com.example.amiralbattikotlin.network.GameWebSocketClient
import com.example.amiralbattikotlin.ui.game.GameActivity
import com.google.gson.Gson


class ShipPlacementActivity : AppCompatActivity(), SocketManager.SocketEventListener {

    private lateinit var tvCurrentShip: TextView
    private lateinit var tvOrientation: TextView
    private lateinit var tvPlacementStatus: TextView
    private lateinit var btnRotate: Button
    private lateinit var btnResetBoard: Button
    private lateinit var btnReady: Button
    private lateinit var rvBoard: RecyclerView

    private lateinit var boardAdapter: BoardAdapter

    private val gson = Gson()

    private val boardSize = 10
    private val boardCells = mutableListOf<BoardCell>()

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
    private var currentOrientation = ShipOrientation.HORIZONTAL

    private var roomCode: String = ""
    private var playerId: String = ""
    private var playerName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ship_placement)

        roomCode = intent.getStringExtra("ROOM_CODE") ?: ""
        playerId = intent.getStringExtra("PLAYER_ID") ?: ""
        playerName = intent.getStringExtra("PLAYER_NAME") ?: ""

        initViews()
        createBoard()
        setupBoard()
        setupClicks()
        updateUI()
    }

    override fun onStart() {
        super.onStart()
        println("[ANDROID_PLACEMENT] onStart -> listener set")
        SocketManager.setListener(this)
    }

    override fun onStop() {
        super.onStop()
        println("[ANDROID_PLACEMENT] onStop -> listener clear request")
        SocketManager.clearListener(this)
    }

    private fun initViews() {
        tvCurrentShip = findViewById(R.id.tvCurrentShip)
        tvOrientation = findViewById(R.id.tvOrientation)
        tvPlacementStatus = findViewById(R.id.tvPlacementStatus)
        btnRotate = findViewById(R.id.btnRotate)
        btnResetBoard = findViewById(R.id.btnResetBoard)
        btnReady = findViewById(R.id.btnReady)
        rvBoard = findViewById(R.id.rvBoard)
    }

    private fun createBoard() {
        boardCells.clear()
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                boardCells.add(BoardCell(row, col, CellState.EMPTY))
            }
        }
    }

    private fun setupBoard() {
        boardAdapter = BoardAdapter(boardCells) { clickedCell ->
            placeCurrentShip(clickedCell.row, clickedCell.col)
        }

        rvBoard.layoutManager = GridLayoutManager(this, boardSize)
        rvBoard.adapter = boardAdapter
    }

    private fun setupClicks() {
        btnRotate.setOnClickListener {
            currentOrientation = if (currentOrientation == ShipOrientation.HORIZONTAL) {
                ShipOrientation.VERTICAL
            } else {
                ShipOrientation.HORIZONTAL
            }
            updateUI()
        }

        btnResetBoard.setOnClickListener {
            resetBoard()
        }

        btnReady.setOnClickListener {
            sendBoardToServer()
        }
    }

    private fun placeCurrentShip(startRow: Int, startCol: Int) {
        if (currentShipIndex >= shipsToPlace.size) {
            showToast("Tüm gemiler zaten yerleştirildi")
            return
        }

        val ship = shipsToPlace[currentShipIndex]

        if (!canPlaceShip(startRow, startCol, ship.size, currentOrientation)) {
            showToast("Gemi burada konumlanamaz. Başka gemiye temas ediyor veya taşma var.")
            return
        }

        for (i in 0 until ship.size) {
            val row = if (currentOrientation == ShipOrientation.VERTICAL) startRow + i else startRow
            val col = if (currentOrientation == ShipOrientation.HORIZONTAL) startCol + i else startCol

            val index = row * boardSize + col
            boardCells[index].state = CellState.SHIP
        }

        ship.placed = true
        currentShipIndex++

        boardAdapter.refreshBoard()
        updateUI()

        if (currentShipIndex >= shipsToPlace.size) {
            tvPlacementStatus.text = "Durum: Tüm gemiler yerleştirildi. Hazırım butonuna bas."
            btnReady.isEnabled = true
        }
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

            if (row >= boardSize || col >= boardSize) {
                return false
            }

            targetCells.add(row to col)
        }

        for ((row, col) in targetCells) {

            for (r in row - 1..row + 1) {
                for (c in col - 1..col + 1) {

                    if (r < 0 || r >= boardSize || c < 0 || c >= boardSize) {
                        continue
                    }

                    val neighborIndex = r * boardSize + c
                    if (boardCells[neighborIndex].state == CellState.SHIP) {
                        return false
                    }
                }
            }
        }

        return true
    }

    private fun resetBoard() {
        createBoard()
        shipsToPlace.forEach { it.placed = false }
        currentShipIndex = 0
        currentOrientation = ShipOrientation.HORIZONTAL
        btnReady.isEnabled = false
        tvPlacementStatus.text = "Durum: Gemileri yerleştir"

        boardAdapter.refreshBoard()
        updateUI()
    }

    private fun updateUI() {
        if (currentShipIndex < shipsToPlace.size) {
            tvCurrentShip.text = "Seçili gemi: ${shipsToPlace[currentShipIndex].size} hücrelik gemi"
        } else {
            tvCurrentShip.text = "Seçili gemi: Tüm gemiler yerleştirildi"
        }

        tvOrientation.text = if (currentOrientation == ShipOrientation.HORIZONTAL) {
            "Yön: Yatay"
        } else {
            "Yön: Dikey"
        }
    }

    private fun buildBoardMatrix(): List<List<Int>> {
        val matrix = MutableList(boardSize) { MutableList(boardSize) { 0 } }

        for (cell in boardCells) {
            matrix[cell.row][cell.col] = if (cell.state == CellState.SHIP) 1 else 0
        }

        return matrix
    }

    private fun sendBoardToServer() {
        val boardMatrix = buildBoardMatrix()

        val messageMap = mapOf(
            "type" to "SET_BOARD",
            "data" to mapOf(
                "roomCode" to roomCode,
                "playerId" to playerId,
                "board" to boardMatrix
            )
        )

        val json = gson.toJson(messageMap)

        println("[ANDROID_PLACEMENT] SET_BOARD gönderiliyor")
        println("[ANDROID_PLACEMENT] roomCode=$roomCode")
        println("[ANDROID_PLACEMENT] playerId=$playerId")
        println("[ANDROID_PLACEMENT] json=$json")

        tvPlacementStatus.text = "Durum: Tahta gönderiliyor..."
        btnReady.isEnabled = false

        SocketManager.send(json)
    }

    override fun onConnected() {
        runOnUiThread {
            showToast("Bağlantı hazır")
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            showToast("Bağlantı kesildi")
        }
    }

    override fun onMessage(message: String) {
        runOnUiThread {
            handleSocketMessage(message)
        }
    }

    override fun onError(errorMessage: String) {
        runOnUiThread {
            showToast(errorMessage)
            tvPlacementStatus.text = "Hata: $errorMessage"
        }
    }

    private fun handleSocketMessage(message: String) {
        println("[ANDROID_PLACEMENT] Gelen ham mesaj = $message")

        try {
            val baseMessage = gson.fromJson(message, BaseSocketMessage::class.java)
            println("[ANDROID_PLACEMENT] Mesaj tipi = ${baseMessage.type}")

            when (baseMessage.type) {
                "BOARD_SET" -> {
                    val data = gson.fromJson(baseMessage.data, BoardSetData::class.java)
                    println("[ANDROID_PLACEMENT] BOARD_SET -> ${data.message}")
                    tvPlacementStatus.text = data.message
                }

                "GAME_STARTED" -> {
                    val data = gson.fromJson(baseMessage.data, GameStartedData::class.java)
                    println("[ANDROID_PLACEMENT] GAME_STARTED -> firstTurnPlayerId=${data.firstTurnPlayerId}")

                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("ROOM_CODE", roomCode)
                    intent.putExtra("PLAYER_ID", playerId)
                    intent.putExtra("PLAYER_NAME", playerName)
                    intent.putExtra("FIRST_TURN_PLAYER_ID", data.firstTurnPlayerId)
                    intent.putExtra("OWN_BOARD_JSON", gson.toJson(buildBoardMatrix()))
                    startActivity(intent)
                    finish()
                }

                "ERROR" -> {
                    val data = gson.fromJson(baseMessage.data, ErrorData::class.java)
                    println("[ANDROID_PLACEMENT] ERROR -> ${data.message}")
                    tvPlacementStatus.text = "Hata: ${data.message}"
                    btnReady.isEnabled = true
                }

                else -> {
                    println("[ANDROID_PLACEMENT] İşlenmeyen mesaj tipi = ${baseMessage.type}")
                }
            }
        } catch (e: Exception) {
            println("[ANDROID_PLACEMENT] Mesaj parse hatası = ${e.message}")
            e.printStackTrace()
            tvPlacementStatus.text = "Mesaj okunamadı: ${e.message}"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}