package com.example.onlineradiojetpack.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.onlineradiojetpack.data.RadioRoom
import com.example.onlineradiojetpack.network.RadioSocketManager
import org.json.JSONObject

@Composable
fun RoomListScreen(
    onRoomClick: (RadioRoom) -> Unit
) {
    val rooms = remember { mutableStateListOf<RadioRoom>() }
    var status by remember { mutableStateOf("Sunucuya bağlanılıyor...") }

    LaunchedEffect(Unit) {
        RadioSocketManager.onConnected = {
            status = "Sunucuya bağlandı"
            RadioSocketManager.getRooms()
        }

        RadioSocketManager.onError = { error ->
            status = "Hata: $error"
        }

        RadioSocketManager.onMessage = { message ->
            val json = JSONObject(message)
            val type = json.optString("type")

            if (type == "ROOM_LIST" || type == "ROOM_UPDATED") {
                val array = json.getJSONArray("rooms")
                rooms.clear()

                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)

                    rooms.add(
                        RadioRoom(
                            id = item.getInt("id"),
                            roomName = item.getString("roomName"),
                            currentMusic = item.optString("currentMusic", null),
                            isPlaying = item.optBoolean("isPlaying", false),
                            listenerCount = item.optInt("listenerCount", 0)
                        )
                    )
                }
            }
        }

        RadioSocketManager.connect()
        RadioSocketManager.getRooms()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "SyncRadio Odaları",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(rooms) { room ->
                    RoomCard(
                        room = room,
                        onClick = { onRoomClick(room) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Socket kapatılmıyor çünkü player ekranında da aynı bağlantı kullanılacak.
        }
    }
}

@Composable
fun RoomCard(
    room: RadioRoom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = room.roomName,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (room.currentMusic.isNullOrBlank()) {
                    "Şu an: Müzik yok"
                } else {
                    "Şu an: ${room.currentMusic}"
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Dinleyici: ${room.listenerCount}"
            )
        }
    }
}