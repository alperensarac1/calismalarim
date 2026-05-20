package com.example.canliyayinkotlin

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canliyayinkotlin.adapter.ChatAdapter
import com.example.canliyayinkotlin.databinding.ActivityViewerBinding
import com.example.canliyayinkotlin.model.ChatMessageModel
import com.example.canliyayinkotlin.socket.LiveSocketListener
import com.example.canliyayinkotlin.socket.LiveSocketManager
import org.json.JSONObject

class ViewerActivity : AppCompatActivity(), LiveSocketListener {

    private lateinit var binding: ActivityViewerBinding
    private lateinit var socketManager: LiveSocketManager
    private lateinit var chatAdapter: ChatAdapter

    private val serverUrl = "ws://10.208.181.112:8765"

    private var roomId: String = ""
    private var roomTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("room_id") ?: ""
        roomTitle = intent.getStringExtra("room_title") ?: "Canlı Yayın"

        binding.tvViewerTitle.text = roomTitle

        setupChatRecyclerView()
        setupSendButton()

        socketManager = LiveSocketManager(serverUrl, this)
        socketManager.connect()
    }

    private fun setupChatRecyclerView() {
        chatAdapter = ChatAdapter(mutableListOf())

        binding.rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }

        binding.rvChat.adapter = chatAdapter
    }

    private fun setupSendButton() {
        binding.btnSendMessage.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()

            if (message.isEmpty()) {
                return@setOnClickListener
            }

            val json = JSONObject()
            json.put("type", "chat_message")
            json.put("message", message)

            socketManager.sendJson(json)

            binding.edtMessage.setText("")
        }
    }

    override fun onConnected() {
        runOnUiThread {
            binding.tvViewerStatus.text = "Sunucuya bağlandı, odaya giriliyor..."
        }

        val json = JSONObject()
        json.put("type", "join_room")
        json.put("room_id", roomId)
        json.put("username", "Android İzleyici")

        socketManager.sendJson(json)
    }

    override fun onMessage(message: String) {
        val json = JSONObject(message)

        when (json.getString("type")) {

            "joined_room" -> {
                runOnUiThread {
                    binding.tvViewerStatus.text = "Yayına bağlandı"
                }
            }

            "viewer_count" -> {
                val count = json.getInt("viewer_count")

                runOnUiThread {
                    binding.tvViewerCount.text = "İzleyici: $count"
                }
            }

            "chat_message" -> {
                val chatMessage = ChatMessageModel(
                    username = json.getString("username"),
                    message = json.getString("message"),
                    createdAt = json.getString("created_at")
                )

                runOnUiThread {
                    chatAdapter.addMessage(chatMessage)
                    binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }

            "video_frame" -> {
                val base64Frame = json.getString("frame")
                showFrame(base64Frame)
            }

            "stream_ended" -> {
                runOnUiThread {
                    binding.tvViewerStatus.text = "Yayın sona erdi"
                    Toast.makeText(this, "Yayın sona erdi", Toast.LENGTH_SHORT).show()
                }
            }

            "error" -> {
                runOnUiThread {
                    binding.tvViewerStatus.text = json.getString("message")
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFrame(base64Frame: String) {
        try {
            val imageBytes = Base64.decode(base64Frame, Base64.DEFAULT)

            val bitmap = BitmapFactory.decodeByteArray(
                imageBytes,
                0,
                imageBytes.size
            )

            runOnUiThread {
                binding.imgLiveFrame.setImageBitmap(bitmap)
            }

        } catch (e: Exception) {
            runOnUiThread {
                binding.tvViewerStatus.text = "Görüntü çözümlenemedi"
            }
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            binding.tvViewerStatus.text = "Hata: $error"
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            binding.tvViewerStatus.text = "Bağlantı kapandı"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.disconnect()
    }
}