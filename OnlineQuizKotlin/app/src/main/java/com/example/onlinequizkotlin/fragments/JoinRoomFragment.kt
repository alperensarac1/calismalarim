package com.example.onlinequizkotlin.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.onlinequizkotlin.MainActivity
import com.example.onlinequizkotlin.R
import com.example.onlinequizkotlin.network.SocketEventListener
import com.example.onlinequizkotlin.network.SocketMessageFactory
import com.example.onlinequizkotlin.network.WebSocketManager
import org.json.JSONObject

class JoinRoomFragment : Fragment(R.layout.fragment_join_room), SocketEventListener {

    /*
        Odaya katılma ekranı.

        Akış:
        1. Kullanıcı adını girer
        2. Oda kodunu girer
        3. WebSocket bağlantısı kurulur
        4. join_room mesajı server'a gönderilir
        5. Server room_joined dönerse WaitingRoomFragment açılır
    */

    private lateinit var edtUsername: EditText
    private lateinit var edtRoomCode: EditText
    private lateinit var txtStatus: TextView

    private var pendingUsername: String = ""
    private var pendingRoomCode: String = ""
    private var shouldSendJoinRoomAfterConnect = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edtUsername = view.findViewById(R.id.edtUsername)
        edtRoomCode = view.findViewById(R.id.edtRoomCode)
        txtStatus = view.findViewById(R.id.txtStatus)

        val btnJoinRoomNow = view.findViewById<Button>(R.id.btnJoinRoomNow)

        WebSocketManager.setListener(this)

        btnJoinRoomNow.setOnClickListener {
            joinRoom()
        }
    }

    private fun joinRoom() {
        val username = edtUsername.text.toString().trim()
        val roomCode = edtRoomCode.text.toString().trim()

        if (username.isEmpty()) {
            txtStatus.text = "Kullanıcı adı boş olamaz."
            return
        }

        if (roomCode.isEmpty()) {
            txtStatus.text = "Oda kodu boş olamaz."
            return
        }

        pendingUsername = username
        pendingRoomCode = roomCode

        txtStatus.text = "Sunucuya bağlanılıyor..."

        if (WebSocketManager.isConnected()) {
            sendJoinRoomMessage()
        } else {
            shouldSendJoinRoomAfterConnect = true
            WebSocketManager.connect()
        }
    }

    private fun sendJoinRoomMessage() {
        shouldSendJoinRoomAfterConnect = false

        val message = SocketMessageFactory.joinRoom(
            roomCode = pendingRoomCode,
            username = pendingUsername
        )

        WebSocketManager.sendMessage(message)

        txtStatus.text = "Odaya katılma isteği gönderildi..."
    }

    override fun onSocketConnected() {
        txtStatus.text = "Sunucuya bağlandı."

        if (shouldSendJoinRoomAfterConnect) {
            sendJoinRoomMessage()
        }
    }

    override fun onSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        when (type) {
            "room_joined" -> {
                val roomCode = json.optString("room_code")
                val username = json.optString("username")
                val questionTime = json.optInt("question_time", 20)

                txtStatus.text = "Odaya katıldın."

                (requireActivity() as MainActivity).openWaitingRoomFragment(
                    roomCode = roomCode,
                    username = username,
                    questionTime = questionTime
                )
            }

            "error" -> {
                txtStatus.text = json.optString("message", "Bilinmeyen hata oluştu.")
            }
        }
    }

    override fun onSocketDisconnected() {
        txtStatus.text = "Sunucu bağlantısı kapandı."
    }

    override fun onSocketError(error: String) {
        txtStatus.text = "Bağlantı hatası: $error"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WebSocketManager.removeListener(this)
    }
}