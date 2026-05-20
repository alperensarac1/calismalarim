package com.example.canliyayinjetpack

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.canliyayinjetpack.model.ChatMessageModel
import com.example.canliyayinjetpack.socket.LiveSocketListener
import com.example.canliyayinjetpack.socket.LiveSocketManager
import org.json.JSONObject

@Composable
fun ViewerScreen(
    roomId: String,
    roomTitle: String,
    onBackClick: () -> Unit
) {
    var statusText by remember { mutableStateOf("Bağlanıyor...") }
    var viewerCount by remember { mutableStateOf(0) }
    var messageText by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf<List<ChatMessageModel>>(emptyList()) }
    var liveBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    var socketManager by remember {
        mutableStateOf<LiveSocketManager?>(null)
    }

    DisposableEffect(roomId) {
        val manager = LiveSocketManager(
            serverUrl = AppConfig.SERVER_URL,
            listener = object : LiveSocketListener {

                override fun onConnected() {
                    statusText = "Sunucuya bağlandı, odaya giriliyor..."

                    val json = JSONObject()
                    json.put("type", "join_room")
                    json.put("room_id", roomId)
                    json.put("username", "Compose İzleyici")

                    socketManager?.sendJson(json)
                }

                override fun onMessage(message: String) {
                    val json = JSONObject(message)
                    val type = json.getString("type")

                    when (type) {
                        "joined_room" -> {
                            statusText = "Yayına bağlandı"
                        }

                        "viewer_count" -> {
                            viewerCount = json.getInt("viewer_count")
                        }

                        "chat_message" -> {
                            val chat = ChatMessageModel(
                                roomId = json.getString("room_id"),
                                username = json.getString("username"),
                                message = json.getString("message"),
                                createdAt = json.getString("created_at")
                            )

                            chatMessages = chatMessages + chat
                        }

                        "video_frame" -> {
                            val base64Frame = json.getString("frame")
                            liveBitmap = base64ToBitmap(base64Frame)
                        }

                        "stream_ended" -> {
                            statusText = "Yayın sona erdi"
                        }

                        "error" -> {
                            statusText = json.getString("message")
                        }
                    }
                }

                override fun onError(error: String) {
                    statusText = "Hata: $error"
                }

                override fun onDisconnected() {
                    statusText = "Bağlantı kapandı"
                }
            }
        )

        socketManager = manager
        manager.connect()

        onDispose {
            manager.disconnect()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(12.dp)
    ) {

        Button(onClick = onBackClick) {
            Text("Geri")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = roomTitle,
            color = Color.White,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "İzleyici: $viewerCount",
            color = Color.White
        )

        Text(
            text = statusText,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFF111111))
        ) {
            liveBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Canlı yayın görüntüsü",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Canlı Sohbet",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(chatMessages) { chat ->
                ChatMessageItem(chat)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Mesaj yaz...")
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val msg = messageText.trim()

                    if (msg.isNotEmpty()) {
                        val json = JSONObject()
                        json.put("type", "chat_message")
                        json.put("message", msg)

                        socketManager?.sendJson(json)
                        messageText = ""
                    }
                }
            ) {
                Text("Gönder")
            }
        }
    }
}

@Composable
fun ChatMessageItem(chat: ChatMessageModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = chat.username,
            color = Color(0xFF93C5FD),
            style = MaterialTheme.typography.labelMedium
        )

        Text(
            text = chat.message,
            color = Color.White
        )
    }
}

fun base64ToBitmap(base64Frame: String): android.graphics.Bitmap? {
    return try {
        val imageBytes = Base64.decode(base64Frame, Base64.DEFAULT)

        BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size
        )
    } catch (e: Exception) {
        null
    }
}