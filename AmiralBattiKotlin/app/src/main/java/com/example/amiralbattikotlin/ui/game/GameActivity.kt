package com.example.amiralbattikotlin.ui.game

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.amiralbattikotlin.R
import com.example.amiralbattikotlin.manager.SocketManager
import com.example.amiralbattikotlin.model.BaseSocketMessage
import com.example.amiralbattikotlin.model.BoardCell
import com.example.amiralbattikotlin.model.CellState
import com.example.amiralbattikotlin.model.ErrorData
import com.example.amiralbattikotlin.model.FireResultData
import com.example.amiralbattikotlin.model.RematchPlayerInfo
import com.example.amiralbattikotlin.model.RematchStartedData
import com.example.amiralbattikotlin.model.RematchStatusData
import com.example.amiralbattikotlin.network.GameWebSocketClient
import com.example.amiralbattikotlin.ui.lobby.LobbyActivity
import com.example.amiralbattikotlin.ui.placement.BoardAdapter
import com.example.amiralbattikotlin.ui.placement.ShipPlacementActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GameActivity : AppCompatActivity(), SocketManager.SocketEventListener {

    private lateinit var tvTurnInfo: TextView
    private lateinit var tvGameStatus: TextView
    private lateinit var rvOwnBoard: RecyclerView
    private lateinit var rvEnemyBoard: RecyclerView
    private lateinit var ownBoardAdapter: BoardAdapter
    private lateinit var enemyBoardAdapter: BoardAdapter

    private val gson = Gson()
    private val boardSize = 10

    private val ownBoardCells = mutableListOf<BoardCell>()
    private val enemyBoardCells = mutableListOf<BoardCell>()

    private var roomCode: String = ""
    private var playerId: String = ""
    private var playerName: String = ""
    private var currentTurnPlayerId: String = ""

    private var isFireRequestPending = false
    private var isRematchRequested = false
    private var isGameOverDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        roomCode = intent.getStringExtra("ROOM_CODE") ?: ""
        playerId = intent.getStringExtra("PLAYER_ID") ?: ""
        playerName = intent.getStringExtra("PLAYER_NAME") ?: ""
        currentTurnPlayerId = intent.getStringExtra("FIRST_TURN_PLAYER_ID") ?: ""

        initViews()
        buildOwnBoardFromIntent()
        buildEnemyBoard()
        setupBoards()
        updateTurnText()
    }

    override fun onStart() {
        super.onStart()
        SocketManager.setListener(this)
    }

    override fun onStop() {
        super.onStop()
        SocketManager.setListener(null)
    }

    private fun initViews() {
        tvTurnInfo = findViewById(R.id.tvTurnInfo)
        tvGameStatus = findViewById(R.id.tvGameStatus)
        rvOwnBoard = findViewById(R.id.rvOwnBoard)
        rvEnemyBoard = findViewById(R.id.rvEnemyBoard)
    }

    private fun buildOwnBoardFromIntent() {
        ownBoardCells.clear()

        val json = intent.getStringExtra("OWN_BOARD_JSON") ?: "[]"
        val type = object : TypeToken<List<List<Int>>>() {}.type
        val matrix: List<List<Int>> = gson.fromJson(json, type)

        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val value = matrix[row][col]
                val state = if (value == 1) CellState.SHIP else CellState.EMPTY
                ownBoardCells.add(BoardCell(row, col, state))
            }
        }
    }

    private fun buildEnemyBoard() {
        enemyBoardCells.clear()
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                enemyBoardCells.add(BoardCell(row, col, CellState.EMPTY))
            }
        }
    }

    private fun setupBoards() {
        ownBoardAdapter = BoardAdapter(ownBoardCells) { }

        enemyBoardAdapter = BoardAdapter(enemyBoardCells) { clickedCell ->
            if (currentTurnPlayerId != playerId) {
                showToast("Sıra sende değil")
                return@BoardAdapter
            }

            sendFire(clickedCell.row, clickedCell.col)
        }

        rvOwnBoard.layoutManager = GridLayoutManager(this, boardSize)
        rvOwnBoard.adapter = ownBoardAdapter

        rvEnemyBoard.layoutManager = GridLayoutManager(this, boardSize)
        rvEnemyBoard.adapter = enemyBoardAdapter
    }

    private fun sendFire(row: Int, col: Int) {
        if (isFireRequestPending) {
            showToast("Önce önceki atışın sonucunu bekle")
            return
        }

        val index = row * boardSize + col
        val state = enemyBoardCells[index].state

        if (state == CellState.HIT || state == CellState.MISS) {
            showToast("Bu hücreye zaten ateş ettin")
            return
        }

        val messageMap = mapOf(
            "type" to "FIRE",
            "data" to mapOf(
                "roomCode" to roomCode,
                "playerId" to playerId,
                "row" to row,
                "col" to col
            )
        )

        isFireRequestPending = true
        SocketManager.send(gson.toJson(messageMap))
        tvGameStatus.text = "Atış gönderildi..."
    }

    private fun handleFireResult(data: FireResultData) {
        isFireRequestPending = false

        val shooterIsMe = data.shooterPlayerId == playerId
        val index = data.row * boardSize + data.col

        if (shooterIsMe) {
            enemyBoardCells[index].state = if (data.hit) CellState.HIT else CellState.MISS
            enemyBoardAdapter.refreshBoard()
        } else {
            val ownCell = ownBoardCells[index]
            ownCell.state = if (data.hit) CellState.HIT else CellState.MISS
            ownBoardAdapter.refreshBoard()
        }

        tvGameStatus.text = data.message

        if (data.gameOver) {
            isRematchRequested = false
            val isWinner = data.winnerPlayerId == playerId

            tvTurnInfo.text = if (isWinner) {
                "Oyun bitti: Kazandın"
            } else {
                "Oyun bitti: Kaybettin"
            }

            showGameOverDialog(isWinner)
            return
        }

        currentTurnPlayerId = data.nextTurnPlayerId ?: ""
        updateTurnText()
    }

    private fun updateTurnText() {
        tvTurnInfo.text = if (currentTurnPlayerId == playerId) {
            "Sıra sende"
        } else {
            "Rakibin sırası"
        }
    }

    override fun onConnected() {
        runOnUiThread {
            showToast("Bağlantı aktif")
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            showToast("Bağlantı kesildi")
            tvGameStatus.text = "Bağlantı kesildi"
        }
    }

    override fun onMessage(message: String) {
        runOnUiThread {
            handleSocketMessage(message)
        }
    }

    override fun onError(errorMessage: String) {
        runOnUiThread {
            tvGameStatus.text = "Hata: $errorMessage"
            showToast(errorMessage)
        }
    }
    private fun showGameOverDialog(isWinner: Boolean) {
        if (isGameOverDialogShown) return
        isGameOverDialogShown = true

        val title = if (isWinner) "Tebrikler" else "Oyun Bitti"
        val message = if (isWinner) {
            "Rakibin tüm gemilerini batırdın."
        } else {
            "Tüm gemilerin batırıldı."
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("$message\n\nYeniden oynamak ister misin?")
            .setCancelable(false)
            .setPositiveButton("Yeniden Oyna") { _, _ ->
                requestRematch()
            }
            .setNegativeButton("Lobiye Dön") { _, _ ->
                val intent = Intent(this, LobbyActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .show()
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
                    tvGameStatus.text = "${data.message}\n${formatRematchPlayers(data.players)}"
                }

                "REMATCH_STARTED" -> {
                    val data = gson.fromJson(baseMessage.data, RematchStartedData::class.java)

                    val intent = Intent(this, ShipPlacementActivity::class.java)
                    intent.putExtra("ROOM_CODE", roomCode)
                    intent.putExtra("PLAYER_ID", playerId)
                    intent.putExtra("PLAYER_NAME", playerName)
                    startActivity(intent)
                    finish()
                }

                "ERROR" -> {
                    val data = gson.fromJson(baseMessage.data, ErrorData::class.java)
                    isFireRequestPending = false
                    tvGameStatus.text = "Hata: ${data.message}"
                }

                "PLAYER_LEFT" -> {
                    tvGameStatus.text = "Rakip oyundan ayrıldı"

                    AlertDialog.Builder(this)
                        .setTitle("Rakip Ayrıldı")
                        .setMessage("Rakip oyundan çıktı. Lobiye dönmek ister misin?")
                        .setCancelable(false)
                        .setPositiveButton("Lobiye Dön") { _, _ ->
                            val intent = Intent(this, LobbyActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .show()
                }
            }
        } catch (e: Exception) {
            tvGameStatus.text = "Mesaj okunamadı: ${e.message}"
        }
    }
    private fun requestRematch() {
        if (isRematchRequested) {
            showToast("Zaten yeniden oyun isteği gönderdin")
            return
        }

        val messageMap = mapOf(
            "type" to "REQUEST_REMATCH",
            "data" to mapOf(
                "roomCode" to roomCode,
                "playerId" to playerId
            )
        )

        isRematchRequested = true
        SocketManager.send(gson.toJson(messageMap))
        tvGameStatus.text = "Yeniden oyun isteği gönderildi. Rakip bekleniyor..."
    }
    private fun formatRematchPlayers(players: List<RematchPlayerInfo>): String {
        return players.joinToString(" | ") { player ->
            if (player.wantsRematch) {
                "${player.name}: hazır"
            } else {
                "${player.name}: bekleniyor"
            }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}