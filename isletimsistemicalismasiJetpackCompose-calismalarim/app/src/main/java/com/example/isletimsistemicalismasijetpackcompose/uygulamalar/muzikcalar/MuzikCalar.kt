package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.muzikcalar

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.isletimsistemicalismasijetpackcompose.R
import kotlinx.coroutines.delay

@Composable
fun MuzikCalar(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Şarkı Listesi ve İsimleri
    val songList = listOf(
        R.raw.vidya,
        R.raw.shine,
        R.raw.disfigure
    )
    val songNames = listOf("Vidya", "Shine", "Disfigure")

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentSongIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0) }
    var songDuration by remember { mutableStateOf(0) }

    LaunchedEffect(mediaPlayer) {
        while (isPlaying) {
            currentPosition = mediaPlayer?.currentPosition ?: 0
            delay(1000)
        }
    }
    // Şarkı Başlatma ve Güncelleme İşlemi
    fun startSong(index: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, songList[index])
        mediaPlayer?.start()
        isPlaying = true
        songDuration = mediaPlayer?.duration ?: 0

    }

    // Şarkıyı Durdur
    fun stopSong() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        currentPosition = 0
    }

    // Bir Sonraki Şarkıya Geç

    fun playNext() {
        currentSongIndex = (currentSongIndex + 1) % songList.size
        startSong(currentSongIndex)
    }

    // Bir Önceki Şarkıya Geç


    fun playPrevious() {
        currentSongIndex = (currentSongIndex - 1 + songList.size) % songList.size
        startSong(currentSongIndex)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = songNames[currentSongIndex],
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        // İlerleme Çubuğu
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = {
                currentPosition = it.toInt()
                mediaPlayer?.seekTo(currentPosition)
            },
            valueRange = 0f..songDuration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { playPrevious() }) {
                Image(painter = painterResource(R.drawable.ic_back), contentDescription = "Back")
            }

            IconButton(onClick = {
                if (isPlaying) {
                    mediaPlayer?.pause()
                    isPlaying = false
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                }
            }) {
                Image(
                    painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = "Oynat/Duraklat"
                )
            }

            IconButton(onClick = { playNext() }) {
                Image(painter = painterResource(R.drawable.next), contentDescription = "Sonraki Şarkı")
            }
        }

        LazyColumn {
            items(songNames.size) { index ->
                Text(
                    text = songNames[index],
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentSongIndex = index
                            startSong(currentSongIndex)
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}
