package com.example.memesharejetpack.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.memesharejatpack.util.VideoUploader
import com.example.memesharejetpack.model.GonderiModel
import com.example.memesharejetpack.service.ApiClient
import com.example.memesharejetpack.viewmodel.OdaViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdaScreen(
    roomId: Int,
    userId: Int,
    odaViewModel: OdaViewModel = viewModel()
) {
    val context = LocalContext.current

    // Sunucudan gelen gönderiler (Fragment'te gonderiList vardı)
    var posts by remember { mutableStateOf<List<GonderiModel>>(emptyList()) }

    // Upload sonucu (image için mevcut ViewModel’inden okunuyor)
    val uploadMsg by odaViewModel.uploadResult.observeAsState()

    // Medya seçimi state
    var shareDialogVisible by remember { mutableStateOf(false) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedIsVideo by remember { mutableStateOf(false) }
    var pickedThumb by remember { mutableStateOf<Bitmap?>(null) } // video için önizleme

    // Document picker: image + video
    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // Persist read
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {}

            val mime = context.contentResolver.getType(uri)
            val isVideo = mime?.startsWith("video") == true
            pickedUri = uri
            pickedIsVideo = isVideo

            if (isVideo) {
                // Basit thumbnail
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    pickedThumb = retriever.getFrameAtTime(1_000_000)
                    retriever.release()
                } catch (e: Exception) {
                    Log.e("OdaScreen", "Video thumb alınamadı: ${e.message}")
                    pickedThumb = null
                }
            } else {
                pickedThumb = null
            }
            shareDialogVisible = true
        }
    }

    // İlk açılışta medyaları çek
    LaunchedEffect(roomId) { refreshPosts(roomId) { posts = it } }

    // Upload sonucu bildirimi + liste yenileme
    LaunchedEffect(uploadMsg) {
        uploadMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            if (msg.contains("yüklendi") || msg.contains("yükleme hatası")) {
                refreshPosts(roomId) { posts = it }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Oda #$roomId") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { pickMediaLauncher.launch(arrayOf("image/*", "video/*")) },
                content = { Text("Paylaş") }
            )
        }
    ) { padding ->
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Henüz gönderi yok") }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { item ->
                    PostCard(
                        item = item,
                        currentUserId = userId
                    )
                }
            }
        }
    }

    // Paylaşım diyaloğu
    if (shareDialogVisible) {
        ShareDialog(
            pickedUri = pickedUri,
            isVideo = pickedIsVideo,
            videoThumb = pickedThumb,
            onDismiss = { shareDialogVisible = false },
            onSend = { uri, caption, isVid ->
                if (uri != null) {
                    if (isVid) {
                        // VideoUploader (mevcuttaki)
                        VideoUploader.uploadVideo(
                            videoName = UUID.randomUUID().toString(),
                            videoUri = uri,
                            activity = (context as Activity),
                            roomId = roomId,
                            userId = userId,
                            caption = caption,
                            uploadUrl = "https://alperensaracdeneme.com/meme/media-upload-video.php"
                        )
                    } else {
                        // Mevcut ViewModel fonksiyonu
                        odaViewModel.uploadImage(uri, roomId, userId, caption)
                    }
                }
                shareDialogVisible = false
            }
        )
    }
}

// Sunucudan medya listeleme (Fragment’teki loadMediaList eşleniği)
private fun refreshPosts(
    roomId: Int,
    onLoaded: (List<GonderiModel>) -> Unit
) {
    ApiClient.getService().getAllMedia(roomId)
        .enqueue(object : Callback<List<GonderiModel>> {
            override fun onResponse(
                call: Call<List<GonderiModel>>,
                response: Response<List<GonderiModel>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    onLoaded(response.body()!!)
                } else onLoaded(emptyList())
            }

            override fun onFailure(call: Call<List<GonderiModel>>, t: Throwable) {
                onLoaded(emptyList())
            }
        })
}
