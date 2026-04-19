package com.example.amiralbattikotlin.ui.lobby


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amiralbattikotlin.R
import com.example.amiralbattikotlin.manager.SocketManager
import com.example.amiralbattikotlin.model.BaseSocketMessage
import com.example.amiralbattikotlin.model.ErrorData
import com.example.amiralbattikotlin.model.JoinedRoomData
import com.example.amiralbattikotlin.model.PlayerInfo
import com.example.amiralbattikotlin.model.PlayerJoinedData
import com.example.amiralbattikotlin.model.RoomCreatedData
import com.example.amiralbattikotlin.network.GameWebSocketClient
import com.example.amiralbattikotlin.repository.GameRepository
import com.example.amiralbattikotlin.ui.placement.ShipPlacementActivity
import com.google.gson.Gson

class LobbyActivity : AppCompatActivity(), SocketManager.SocketEventListener {

    private lateinit var etPlayerName: EditText
    private lateinit var etRoomCode: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnCreateRoom: Button
    private lateinit var btnJoinRoom: Button
    private lateinit var tvRoomInfo: TextView
    private lateinit var tvPlayers: TextView
    private lateinit var tvStatus: TextView

    private val gson = Gson()

    private var currentRoomCode: String = ""
    private var currentPlayerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        initViews()

        btnConnect.setOnClickListener {
            tvStatus.text = "Durum: Sunucuya bağlanılıyor..."
            SocketManager.setListener(this)
            SocketManager.connect()
        }

        btnCreateRoom.setOnClickListener {
            val playerName = etPlayerName.text.toString().trim()

            if (playerName.isEmpty()) {
                showToast("Oyuncu adı gir")
                return@setOnClickListener
            }

            val messageMap = mapOf(
                "type" to "CREATE_ROOM",
                "data" to mapOf("playerName" to playerName)
            )

            SocketManager.send(gson.toJson(messageMap))
        }

        btnJoinRoom.setOnClickListener {
            val playerName = etPlayerName.text.toString().trim()
            val roomCode = etRoomCode.text.toString().trim()

            if (playerName.isEmpty() || roomCode.isEmpty()) {
                showToast("Oyuncu adı ve oda kodu gir")
                return@setOnClickListener
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
    }

    override fun onStart() {
        super.onStart()
        SocketManager.setListener(this)
    }

    override fun onStop() {
        super.onStop()
        println("[ANDROID_LOBBY] onStop -> listener clear request")
        SocketManager.clearListener(this)
    }

    private fun initViews() {
        etPlayerName = findViewById(R.id.etPlayerName)
        etRoomCode = findViewById(R.id.etRoomCode)
        btnConnect = findViewById(R.id.btnConnect)
        btnCreateRoom = findViewById(R.id.btnCreateRoom)
        btnJoinRoom = findViewById(R.id.btnJoinRoom)
        tvRoomInfo = findViewById(R.id.tvRoomInfo)
        tvPlayers = findViewById(R.id.tvPlayers)
        tvStatus = findViewById(R.id.tvStatus)
    }

    override fun onConnected() {
        runOnUiThread {
            tvStatus.text = "Durum: Sunucuya bağlandı"
            showToast("Sunucuya bağlandın")
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            tvStatus.text = "Durum: Bağlantı kesildi"
        }
    }

    override fun onMessage(message: String) {
        runOnUiThread {
            handleSocketMessage(message)
        }
    }

    override fun onError(errorMessage: String) {
        runOnUiThread {
            tvStatus.text = "Hata: $errorMessage"
            showToast(errorMessage)
        }
    }

    private fun handleSocketMessage(message: String) {
        try {
            val baseMessage = gson.fromJson(message, BaseSocketMessage::class.java)

            when (baseMessage.type) {
                "ROOM_CREATED" -> {
                    val data = gson.fromJson(baseMessage.data, RoomCreatedData::class.java)
                    currentRoomCode = data.roomCode
                    currentPlayerId = data.playerId

                    etRoomCode.setText(data.roomCode)
                    tvRoomInfo.text = "Oda: ${data.roomCode}"
                    tvPlayers.text = "Oyuncular: ${formatPlayers(data.players)}"
                    tvStatus.text = data.message
                }

                "JOINED_ROOM" -> {
                    val data = gson.fromJson(baseMessage.data, JoinedRoomData::class.java)
                    currentRoomCode = data.roomCode
                    currentPlayerId = data.playerId

                    etRoomCode.setText(data.roomCode)
                    tvRoomInfo.text = "Oda: ${data.roomCode}"
                    tvPlayers.text = "Oyuncular: ${formatPlayers(data.players)}"
                    tvStatus.text = data.message
                }

                "PLAYER_JOINED" -> {
                    val data = gson.fromJson(baseMessage.data, PlayerJoinedData::class.java)

                    tvRoomInfo.text = "Oda: ${data.roomCode}"
                    tvPlayers.text = "Oyuncular: ${formatPlayers(data.players)}"
                    tvStatus.text = data.message

                    if (data.players.size == 2) {
                        showToast("Rakip bağlandı")

                        val intent = Intent(this, ShipPlacementActivity::class.java)
                        intent.putExtra("ROOM_CODE", currentRoomCode)
                        intent.putExtra("PLAYER_ID", currentPlayerId)
                        intent.putExtra("PLAYER_NAME", etPlayerName.text.toString().trim())
                        startActivity(intent)
                    }
                }

                "PLAYER_LEFT" -> {
                    val data = gson.fromJson(baseMessage.data, PlayerJoinedData::class.java)
                    tvPlayers.text = "Oyuncular: ${formatPlayers(data.players)}"
                    tvStatus.text = data.message
                }

                "ERROR" -> {
                    val data = gson.fromJson(baseMessage.data, ErrorData::class.java)
                    tvStatus.text = "Hata: ${data.message}"
                    showToast(data.message)
                }

                else -> {
                    tvStatus.text = "Bilinmeyen mesaj: $message"
                }
            }
        } catch (e: Exception) {
            tvStatus.text = "Mesaj ayrıştırılamadı: ${e.message}"
        }
    }

    private fun formatPlayers(players: List<PlayerInfo>): String {
        if (players.isEmpty()) return "-"
        return players.joinToString(" | ") { it.name }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}