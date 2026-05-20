package com.example.canliyayinjetpack


import android.Manifest
import android.graphics.Bitmap
import android.util.Base64
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.canliyayinjetpack.model.ChatMessageModel
import com.example.canliyayinjetpack.socket.LiveSocketListener
import com.example.canliyayinjetpack.socket.LiveSocketManager
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

@Composable
fun BroadcasterScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var statusText by remember { mutableStateOf("Sunucuya bağlanıyor...") }
    var broadcastTitle by remember { mutableStateOf("") }
    var roomId by remember { mutableStateOf<String?>(null) }
    var viewerCount by remember { mutableStateOf(0) }
    var messageText by remember { mutableStateOf("") }
    var chatMessages by remember { mutableStateOf<List<ChatMessageModel>>(emptyList()) }

    var socketManager by remember { mutableStateOf<LiveSocketManager?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var lastFrameTime by remember { mutableLongStateOf(0L) }
    val frameIntervalMs = 200L

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                statusText = "Kamera izni verilmedi"
            }
        }

    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(Unit) {
        val manager = LiveSocketManager(
            serverUrl = AppConfig.SERVER_URL,
            listener = object : LiveSocketListener {

                override fun onConnected() {
                    statusText = "Sunucuya bağlandı. Başlık yazıp yayını başlat."
                }

                override fun onMessage(message: String) {
                    val json = JSONObject(message)
                    val type = json.getString("type")

                    when (type) {
                        "room_created" -> {
                            roomId = json.getString("room_id")
                            statusText = "Yayın başladı"
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
            cameraExecutor.shutdown()
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
            text = statusText,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = broadcastTitle,
            onValueChange = { broadcastTitle = it },
            enabled = roomId == null,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Yayın başlığı yaz...")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = roomId == null,
            onClick = {
                val title = broadcastTitle.trim()

                if (title.isNotEmpty()) {
                    val json = JSONObject()
                    json.put("type", "create_room")
                    json.put("title", title)
                    json.put("broadcaster_name", "Compose Yayıncı")

                    socketManager?.sendJson(json)
                    statusText = "Oda oluşturuluyor..."
                } else {
                    statusText = "Yayın başlığı yazmalısın"
                }
            }
        ) {
            Text("Yayını Başlat")
        }

        Text(
            text = "İzleyici: $viewerCount",
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    scaleType = PreviewView.ScaleType.FILL_CENTER

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build()

                        preview.setSurfaceProvider(surfaceProvider)

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            try {
                                val currentTime = System.currentTimeMillis()

                                if (currentTime - lastFrameTime < frameIntervalMs) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                lastFrameTime = currentTime

                                if (roomId == null) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                val bitmap = imageProxyToBitmap(imageProxy)

                                val resizedBitmap = Bitmap.createScaledBitmap(
                                    bitmap,
                                    320,
                                    240,
                                    true
                                )

                                val base64Frame = bitmapToBase64(resizedBitmap)

                                val json = JSONObject()
                                json.put("type", "video_frame")
                                json.put("frame", base64Frame)

                                socketManager?.sendJson(json)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                        cameraProvider.unbindAll()

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )

                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

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

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBackClick
        ) {
            Text("Yayını Bitir")
        }
    }
}

fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val planeProxy = imageProxy.planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    buffer.rewind()

    val bitmap = Bitmap.createBitmap(
        imageProxy.width,
        imageProxy.height,
        Bitmap.Config.ARGB_8888
    )

    bitmap.copyPixelsFromBuffer(buffer)

    return bitmap
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()

    bitmap.compress(
        Bitmap.CompressFormat.JPEG,
        45,
        outputStream
    )

    val byteArray = outputStream.toByteArray()

    return Base64.encodeToString(
        byteArray,
        Base64.NO_WRAP
    )
}