package com.example.memesharejetpack.view

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.memesharejetpack.model.GonderiModel

@Composable
fun PostCard(
    item: GonderiModel,
    currentUserId: Int
) {
    val base = "https://alperensaracdeneme.com/meme/"
    val fullUrl = base + item.mediaUrl

    val alignEnd = item.userId == currentUserId

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.width(220.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                when (item.mediaType) {
                    "image" -> ImagePost(fullUrl)
                    "video" -> VideoPost(fullUrl)
                    else -> Box(Modifier.size(200.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Kullanıcı #${item.userId}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = item.uploadedAt,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagePost(url: String) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(200.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun VideoPost(url: String) {
    var playing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!playing) {
            // Thumbnail (Glide.frame muadili için Coil doğrudan frame çekmiyor;
            // basitçe ilk frame yerine direkt videonun URL'ini göstermeyebilir,
            // istersen sunucudan poster url döndürebilirsin)
            // Burada placeholder olarak siyah arkaplan + Play:
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            FilledTonalButton(onClick = { playing = true }) { Text("▶") }
        } else {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoPath(url)
                        setOnCompletionListener {
                            playing = false
                        }
                        start()
                    }
                },
                modifier = Modifier.matchParentSize()
            )
        }
    }
}
