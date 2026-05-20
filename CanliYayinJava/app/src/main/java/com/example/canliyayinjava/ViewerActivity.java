package com.example.canliyayinjava;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canliyayinjava.adapter.ChatAdapter;
import com.example.canliyayinjava.model.ChatMessageModel;
import com.example.canliyayinjava.socket.LiveSocketListener;
import com.example.canliyayinjava.socket.LiveSocketManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewerActivity extends AppCompatActivity implements LiveSocketListener {

    private TextView tvViewerTitle;
    private TextView tvViewerCount;
    private TextView tvViewerStatus;
    private ImageView imgLiveFrame;
    private RecyclerView rvChat;
    private EditText edtMessage;
    private Button btnSendMessage;

    private LiveSocketManager socketManager;
    private ChatAdapter chatAdapter;

    private final List<ChatMessageModel> chatMessages = new ArrayList<>();

    private String roomId = "";
    private String roomTitle = "Canlı Yayın";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        roomId = getIntent().getStringExtra("room_id");
        roomTitle = getIntent().getStringExtra("room_title");

        if (roomId == null) roomId = "";
        if (roomTitle == null) roomTitle = "Canlı Yayın";

        initViews();
        setupChatRecyclerView();
        setupSendButton();

        socketManager = new LiveSocketManager(AppConfig.SERVER_URL, this);
        socketManager.connect();
    }

    private void initViews() {
        tvViewerTitle = findViewById(R.id.tvViewerTitle);
        tvViewerCount = findViewById(R.id.tvViewerCount);
        tvViewerStatus = findViewById(R.id.tvViewerStatus);
        imgLiveFrame = findViewById(R.id.imgLiveFrame);
        rvChat = findViewById(R.id.rvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        tvViewerTitle.setText(roomTitle);
    }

    private void setupChatRecyclerView() {
        chatAdapter = new ChatAdapter(chatMessages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);
    }

    private void setupSendButton() {
        btnSendMessage.setOnClickListener(v -> {
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
        });
    }

    @Override
    public void onConnected() {
        runOnUiThread(() ->
                tvViewerStatus.setText("Sunucuya bağlandı, odaya giriliyor...")
        );

        try {
            JSONObject json = new JSONObject();
            json.put("type", "join_room");
            json.put("room_id", roomId);
            json.put("username", "Java İzleyici");

            socketManager.sendJson(json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            if (type.equals("joined_room")) {
                runOnUiThread(() ->
                        tvViewerStatus.setText("Yayına bağlandı")
                );
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

            if (type.equals("video_frame")) {
                String base64Frame = json.getString("frame");
                showFrame(base64Frame);
            }

            if (type.equals("stream_ended")) {
                runOnUiThread(() -> {
                    tvViewerStatus.setText("Yayın sona erdi");
                    Toast.makeText(this, "Yayın sona erdi", Toast.LENGTH_SHORT).show();
                });
            }

            if (type.equals("error")) {
                String errorMessage = json.getString("message");

                runOnUiThread(() -> {
                    tvViewerStatus.setText(errorMessage);
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFrame(String base64Frame) {
        try {
            byte[] imageBytes = Base64.decode(base64Frame, Base64.DEFAULT);

            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.length
            );

            runOnUiThread(() ->
                    imgLiveFrame.setImageBitmap(bitmap)
            );

        } catch (Exception e) {
            runOnUiThread(() ->
                    tvViewerStatus.setText("Görüntü çözümlenemedi")
            );
        }
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() ->
                tvViewerStatus.setText("Hata: " + error)
        );
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() ->
                tvViewerStatus.setText("Bağlantı kapandı")
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (socketManager != null) {
            socketManager.disconnect();
        }
    }
}