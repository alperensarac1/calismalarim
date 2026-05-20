package com.example.canliyayinkotlin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canliyayinkotlin.adapter.ChatAdapter
import com.example.canliyayinkotlin.databinding.ActivityBroadcasterBinding
import com.example.canliyayinkotlin.model.ChatMessageModel
import com.example.canliyayinkotlin.socket.LiveSocketListener
import com.example.canliyayinkotlin.socket.LiveSocketManager
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class BroadcasterActivity : AppCompatActivity(), LiveSocketListener {

    private lateinit var binding: ActivityBroadcasterBinding
    private lateinit var socketManager: LiveSocketManager
    private lateinit var chatAdapter: ChatAdapter

    private val serverUrl = "ws://10.208.181.112:8765"

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private var roomId: String? = null

    private var lastFrameTime = 0L
    private val frameIntervalMs = 200L

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBroadcasterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupChatRecyclerView()
        setupSendButton()

        socketManager = LiveSocketManager(serverUrl, this)
        socketManager.connect()

        binding.btnStopBroadcast.setOnClickListener {
            finish()
        }
        binding.btnStartBroadcast.setOnClickListener {
            val title = binding.edtBroadcastTitle.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Yayın başlığı yazmalısın", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject()
            json.put("type", "create_room")
            json.put("title", title)
            json.put("broadcaster_name", "Android Yayıncı")

            socketManager.sendJson(json)

            binding.tvBroadcastStatus.text = "Oda oluşturuluyor..."
        }
        checkCameraPermission()
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

    private fun checkCameraPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onConnected() {
        runOnUiThread {
            binding.tvBroadcastStatus.text = "Sunucuya bağlandı. Yayın başlığı girip başlat."
        }
    }

    override fun onMessage(message: String) {
        val json = JSONObject(message)

        when (json.getString("type")) {

            "room_created" -> {
                roomId = json.getString("room_id")

                runOnUiThread {
                    binding.tvBroadcastStatus.text = "Yayın başladı"
                    binding.edtBroadcastTitle.isEnabled = false
                    binding.btnStartBroadcast.isEnabled = false
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

            "error" -> {
                runOnUiThread {
                    binding.tvBroadcastStatus.text = json.getString("message")
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()

            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                analyzeFrame(imageProxy)
            }

            val cameraSelector = try {
                if (cameraProvider.hasCamera(androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
                }
            } catch (e: Exception) {
                androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            }

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeFrame(imageProxy: ImageProxy) {
        try {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastFrameTime < frameIntervalMs) {
                imageProxy.close()
                return
            }

            lastFrameTime = currentTime

            if (roomId == null) {
                imageProxy.close()
                return
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

            socketManager.sendJson(json)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
        val planeProxy = imageProxy.planes[0]
        val buffer = planeProxy.buffer
        buffer.rewind()

        val bitmap = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )

        bitmap.copyPixelsFromBuffer(buffer)

        return bitmap
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
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

    override fun onError(error: String) {
        runOnUiThread {
            binding.tvBroadcastStatus.text = "Hata: $error"
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            binding.tvBroadcastStatus.text = "Bağlantı kapandı"
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        socketManager.disconnect()
        cameraExecutor.shutdown()
    }
}