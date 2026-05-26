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

class CreateRoomFragment : Fragment(R.layout.fragment_create_room), SocketEventListener {

    /*
        Oda oluşturma ekranı.

        Akış:
        1. Kullanıcı adını girer
        2. Soru süresini girer
        3. WebSocket bağlantısı kurulur
        4. create_room mesajı Python server'a gönderilir
        5. Server room_created dönerse OwnerRoomFragment açılır
    */

    private lateinit var edtUsername: EditText
    private lateinit var edtQuestionTime: EditText
    private lateinit var txtStatus: TextView

    private var pendingUsername: String = ""
    private var pendingQuestionTime: Int = 20
    private var shouldSendCreateRoomAfterConnect = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edtUsername = view.findViewById(R.id.edtUsername)
        edtQuestionTime = view.findViewById(R.id.edtQuestionTime)
        txtStatus = view.findViewById(R.id.txtStatus)

        val btnCreateRoomNow = view.findViewById<Button>(R.id.btnCreateRoomNow)

        WebSocketManager.setListener(this)

        btnCreateRoomNow.setOnClickListener {
            createRoom()
        }
    }

    private fun createRoom() {
        val username = edtUsername.text.toString().trim()
        val questionTimeText = edtQuestionTime.text.toString().trim()

        if (username.isEmpty()) {
            txtStatus.text = "Kullanıcı adı boş olamaz."
            return
        }

        val questionTime = questionTimeText.toIntOrNull() ?: 20

        if (questionTime < 5) {
            txtStatus.text = "Soru süresi en az 5 saniye olmalı."
            return
        }

        pendingUsername = username
        pendingQuestionTime = questionTime

        txtStatus.text = "Sunucuya bağlanılıyor..."

        if (WebSocketManager.isConnected()) {
            sendCreateRoomMessage()
        } else {
            shouldSendCreateRoomAfterConnect = true
            WebSocketManager.connect()
        }
    }

    private fun sendCreateRoomMessage() {
        shouldSendCreateRoomAfterConnect = false

        val message = SocketMessageFactory.createRoom(
            username = pendingUsername,
            questionTime = pendingQuestionTime
        )

        WebSocketManager.sendMessage(message)

        txtStatus.text = "Oda oluşturma isteği gönderildi..."
    }

    override fun onSocketConnected() {
        txtStatus.text = "Sunucuya bağlandı."

        if (shouldSendCreateRoomAfterConnect) {
            sendCreateRoomMessage()
        }
    }

    override fun onSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        when (type) {
            "room_created" -> {
                val roomCode = json.optString("room_code")
                val username = json.optString("username")
                val questionTime = json.optInt("question_time", pendingQuestionTime)

                txtStatus.text = "Oda oluşturuldu: $roomCode"

                (requireActivity() as MainActivity).openOwnerRoomFragment(
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