package com.example.canliyayinjetpack

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.canliyayinjetpack.model.RoomModel
import com.example.canliyayinjetpack.socket.LiveSocketListener
import com.example.canliyayinjetpack.socket.LiveSocketManager
import org.json.JSONObject

@Composable
fun RoomListScreen(
    onBackClick: () -> Unit,
    onRoomClick: (RoomModel) -> Unit
) {
    var rooms by remember { mutableStateOf<List<RoomModel>>(emptyList()) }
    var statusText by remember { mutableStateOf("Sunucuya bağlanıyor...") }

    var socketManager by remember {
        mutableStateOf<LiveSocketManager?>(null)
    }

    DisposableEffect(Unit) {
        val manager = LiveSocketManager(
            serverUrl = AppConfig.SERVER_URL,
            listener = object : LiveSocketListener {

                override fun onConnected() {
                    statusText = "Sunucuya bağlandı"

                    val json = JSONObject()
                    json.put("type", "get_rooms")

                    socketManager?.sendJson(json)
                }

                override fun onMessage(message: String) {
                    val json = JSONObject(message)
                    val type = json.getString("type")

                    if (type == "rooms_list") {
                        val roomsArray = json.getJSONArray("rooms")
                        val newRooms = mutableListOf<RoomModel>()

                        for (i in 0 until roomsArray.length()) {
                            val item = roomsArray.getJSONObject(i)

                            newRooms.add(
                                RoomModel(
                                    roomId = item.getString("room_id"),
                                    title = item.getString("title"),
                                    broadcasterName = item.getString("broadcaster_name"),
                                    createdAt = item.getString("created_at"),
                                    viewerCount = item.getInt("viewer_count")
                                )
                            )
                        }

                        rooms = newRooms
                    }

                    if (type == "error") {
                        statusText = json.getString("message")
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
            .padding(16.dp)
    ) {

        Button(onClick = onBackClick) {
            Text("Geri")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aktif Yayınlar",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = statusText)

        Spacer(modifier = Modifier.height(16.dp))

        if (rooms.isEmpty()) {
            Text("Aktif yayın yok")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(rooms) { room ->
                    RoomCard(
                        room = room,
                        onClick = {
                            onRoomClick(room)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RoomCard(
    room: RoomModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = room.title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Yayıncı: ${room.broadcasterName}")
            Text("İzleyici: ${room.viewerCount}")
            Text("Başlama: ${room.createdAt}")
        }
    }
}