package com.example.onlineradyokotlin.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.onlineradyokotlin.databinding.ActivityRadioPlayerBinding
import com.example.onlineradyokotlin.network.RadioSocketManager
import org.json.JSONObject
import kotlin.math.abs

class RadioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRadioPlayerBinding

    private var player: ExoPlayer? = null

    private var roomId: Int = -1
    private var roomName: String = ""

    private var currentMusicUrl: String? = null

    private val syncHandler = Handler(Looper.getMainLooper())

    private val syncRunnable = object : Runnable {
        override fun run() {
            if (roomId != -1) {
                RadioSocketManager.requestSync(roomId)
            }

            syncHandler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRadioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getIntExtra("roomId", -1)
        roomName = intent.getStringExtra("roomName") ?: "Oda"

        binding.tvRoomName.text = roomName

        setupPlayer()
        setupSocketListener()

        RadioSocketManager.joinRoom(roomId)

        syncHandler.postDelayed(syncRunnable, 5000)
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
    }

    private fun setupSocketListener() {
        RadioSocketManager.onMessageReceived = { message ->
            handleSocketMessage(message)
        }

        RadioSocketManager.onError = { error ->
            runOnUiThread {
                binding.tvStatus.text = "Bağlantı hatası: $error"
            }
        }
    }

    private fun handleSocketMessage(message: String) {
        val json = JSONObject(message)
        val type = json.optString("type")

        when (type) {
            "PLAYBACK_STATE" -> {
                val incomingRoomId = json.optInt("roomId")

                if (incomingRoomId != roomId) return

                val title = json.optString("title")
                val musicUrl = json.optString("musicUrl")
                val positionSeconds = json.optDouble("positionSeconds", 0.0)

                runOnUiThread {
                    playOrSyncMusic(
                        title = title,
                        musicUrl = musicUrl,
                        positionSeconds = positionSeconds
                    )
                }
            }

            "NO_MUSIC" -> {
                runOnUiThread {
                    binding.tvMusicTitle.text = "Bu odada şu an müzik yok"
                    binding.tvStatus.text = "Bekleniyor..."
                    player?.pause()
                }
            }
        }
    }

    private fun playOrSyncMusic(
        title: String,
        musicUrl: String,
        positionSeconds: Double
    ) {
        binding.tvMusicTitle.text = title
        binding.tvStatus.text = "Dinleniyor..."

        val targetPositionMs = (positionSeconds * 1000).toLong()

        if (currentMusicUrl != musicUrl) {
            currentMusicUrl = musicUrl

            val mediaItem = MediaItem.fromUri(musicUrl)

            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.seekTo(targetPositionMs)
            player?.play()

            return
        }

        val currentPosition = player?.currentPosition ?: 0L
        val difference = abs(currentPosition - targetPositionMs)

        if (difference > 1200) {
            player?.seekTo(targetPositionMs)
        }

        if (player?.isPlaying == false) {
            player?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        syncHandler.removeCallbacks(syncRunnable)

        player?.release()
        player = null
    }
}