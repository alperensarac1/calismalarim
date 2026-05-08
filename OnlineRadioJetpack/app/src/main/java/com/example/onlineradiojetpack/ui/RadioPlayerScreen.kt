package com.example.onlineradiojetpack.ui

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.onlineradiojetpack.network.RadioSocketManager
import kotlinx.coroutines.delay
import org.json.JSONObject
import kotlin.math.abs

@Composable
fun RadioPlayerScreen(
    roomId: Int,
    roomName: String
) {
    val context = LocalContext.current

    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    var musicTitle by remember { mutableStateOf("Çalan müzik bekleniyor...") }
    var status by remember { mutableStateOf("Odaya bağlanılıyor...") }
    var currentMusicUrl by remember { mutableStateOf<String?>(null) }

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    fun playOrSyncMusic(
        title: String,
        musicUrl: String,
        positionSeconds: Double
    ) {
        musicTitle = title
        status = "Dinleniyor..."

        val targetPositionMs = (positionSeconds * 1000).toLong()

        if (currentMusicUrl != musicUrl) {
            currentMusicUrl = musicUrl

            val mediaItem = MediaItem.fromUri(musicUrl)

            player.setMediaItem(mediaItem)
            player.prepare()
            player.seekTo(targetPositionMs)
            player.play()
            return
        }

        val diff = abs(player.currentPosition - targetPositionMs)

        if (diff > 1200) {
            player.seekTo(targetPositionMs)
        }

        if (!player.isPlaying) {
            player.play()
        }
    }

    LaunchedEffect(roomId) {
        RadioSocketManager.onMessage = { message ->

            mainHandler.post {
                try {
                    val json = JSONObject(message)
                    val type = json.optString("type")

                    when (type) {
                        "PLAYBACK_STATE" -> {
                            val incomingRoomId = json.optInt("roomId")

                            if (incomingRoomId == roomId) {
                                playOrSyncMusic(
                                    title = json.optString("title"),
                                    musicUrl = json.optString("musicUrl"),
                                    positionSeconds = json.optDouble("positionSeconds", 0.0)
                                )
                            }
                        }

                        "NO_MUSIC" -> {
                            musicTitle = "Bu odada şu an müzik yok"
                            status = "Bekleniyor..."
                            player.pause()
                        }
                    }

                } catch (e: Exception) {
                    status = "Mesaj işleme hatası: ${e.message}"
                }
            }
        }

        RadioSocketManager.onError = { error ->
            mainHandler.post {
                status = "Bağlantı hatası: $error"
            }
        }

        RadioSocketManager.connect()
        RadioSocketManager.joinRoom(roomId)

        while (true) {
            delay(5000)
            RadioSocketManager.requestSync(roomId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mainHandler.post {
                player.release()
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = roomName,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = musicTitle,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                factory = {
                    PlayerView(it).apply {
                        this.player = player
                        useController = false
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
            )
        }
    }
}