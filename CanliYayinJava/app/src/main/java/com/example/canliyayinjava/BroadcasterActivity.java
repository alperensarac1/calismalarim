package com.example.canliyayinjava;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canliyayinjava.adapter.ChatAdapter;
import com.example.canliyayinjava.model.ChatMessageModel;
import com.example.canliyayinjava.socket.LiveSocketListener;
import com.example.canliyayinjava.socket.LiveSocketManager;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BroadcasterActivity extends AppCompatActivity implements LiveSocketListener {

    private TextView tvBroadcastStatus;
    private TextView tvViewerCount;
    private EditText edtBroadcastTitle;
    private Button btnStartBroadcast;
    private PreviewView previewView;
    private RecyclerView rvChat;
    private EditText edtMessage;
    private Button btnSendMessage;
    private Button btnStopBroadcast;

    private LiveSocketManager socketManager;
    private ChatAdapter chatAdapter;

    private final List<ChatMessageModel> chatMessages = new ArrayList<>();

    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();

    private String roomId = null;

    private long lastFrameTime = 0L;
    private final long frameIntervalMs = 200L;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Kamera izni gerekli", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcaster);

        initViews();
        setupChatRecyclerView();
        setupButtons();

        socketManager = new LiveSocketManager(AppConfig.SERVER_URL, this);
        socketManager.connect();

        checkCameraPermission();
    }

    private void initViews() {
        tvBroadcastStatus = findViewById(R.id.tvBroadcastStatus);
        tvViewerCount = findViewById(R.id.tvViewerCount);
        edtBroadcastTitle = findViewById(R.id.edtBroadcastTitle);
        btnStartBroadcast = findViewById(R.id.btnStartBroadcast);
        previewView = findViewById(R.id.previewView);
        rvChat = findViewById(R.id.rvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnStopBroadcast = findViewById(R.id.btnStopBroadcast);
    }

    private void setupChatRecyclerView() {
        chatAdapter = new ChatAdapter(chatMessages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);
    }

    private void setupButtons() {
        btnStartBroadcast.setOnClickListener(v -> startBroadcastRoom());

        btnSendMessage.setOnClickListener(v -> sendChatMessage());

        btnStopBroadcast.setOnClickListener(v -> finish());
    }

    private void startBroadcastRoom() {
        String title = edtBroadcastTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Yayın başlığı yazmalısın", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("type", "create_room");
            json.put("title", title);
            json.put("broadcaster_name", "Java Yayıncı");

            socketManager.sendJson(json);

            tvBroadcastStatus.setText("Oda oluşturuluyor...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendChatMessage() {
        String message = edtMessage.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("type", "chat_message");
            json.put("message", message);

            socketManager.sendJson(json);

            edtMessage.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkCameraPermission() {
        boolean granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;

        if (granted) {
            startCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onConnected() {
        runOnUiThread(() ->
                tvBroadcastStatus.setText("Sunucuya bağlandı. Başlık yazıp yayını başlat.")
        );
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            if (type.equals("room_created")) {
                roomId = json.getString("room_id");

                runOnUiThread(() -> {
                    tvBroadcastStatus.setText("Yayın başladı");
                    edtBroadcastTitle.setEnabled(false);
                    btnStartBroadcast.setEnabled(false);
                });
            }

            if (type.equals("viewer_count")) {
                int count = json.getInt("viewer_count");

                runOnUiThread(() ->
                        tvViewerCount.setText("İzleyici: " + count)
                );
            }

            if (type.equals("chat_message")) {
                ChatMessageModel chatMessage = new ChatMessageModel(
                        json.getString("username"),
                        json.getString("message"),
                        json.getString("created_at")
                );

                runOnUiThread(() -> {
                    chatAdapter.addMessage(chatMessage);
                    rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                });
            }

            if (type.equals("error")) {
                String errorMessage = json.getString("message");

                runOnUiThread(() ->
                        tvBroadcastStatus.setText(errorMessage)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        try {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastFrameTime < frameIntervalMs) {
                imageProxy.close();
                return;
            }

            lastFrameTime = currentTime;

            if (roomId == null) {
                imageProxy.close();
                return;
            }

            Bitmap bitmap = imageProxyToBitmap(imageProxy);

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    320,
                    240,
                    true
            );

            String base64Frame = bitmapToBase64(resizedBitmap);

            JSONObject json = new JSONObject();
            json.put("type", "video_frame");
            json.put("frame", base64Frame);

            socketManager.sendJson(json);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            imageProxy.close();
        }
    }

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy planeProxy = imageProxy.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        buffer.rewind();

        Bitmap bitmap = Bitmap.createBitmap(
                imageProxy.getWidth(),
                imageProxy.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        bitmap.copyPixelsFromBuffer(buffer);

        return bitmap;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                45,
                outputStream
        );

        byte[] byteArray = outputStream.toByteArray();

        return Base64.encodeToString(
                byteArray,
                Base64.NO_WRAP
        );
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() ->
                tvBroadcastStatus.setText("Hata: " + error)
        );
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() ->
                tvBroadcastStatus.setText("Bağlantı kapandı")
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (socketManager != null) {
            socketManager.disconnect();
        }

        cameraExecutor.shutdown();
    }
}
