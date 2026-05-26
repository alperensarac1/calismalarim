package com.example.onlinequizjava.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlinequizjava.MainActivity;
import com.example.onlinequizjava.R;
import com.example.onlinequizjava.network.SocketEventListener;
import com.example.onlinequizjava.network.WebSocketManager;

import org.json.JSONObject;

public class CreateRoomFragment extends Fragment implements SocketEventListener {

    private EditText edtUsername;
    private EditText edtQuestionTime;
    private TextView txtStatus;

    private String pendingUsername = "";
    private int pendingQuestionTime = 20;
    private boolean shouldSendCreateRoomAfterConnect = false;

    public CreateRoomFragment() {
        super(R.layout.fragment_create_room);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        edtUsername = view.findViewById(R.id.edtUsername);
        edtQuestionTime = view.findViewById(R.id.edtQuestionTime);
        txtStatus = view.findViewById(R.id.txtStatus);

        Button btnCreateRoomNow = view.findViewById(R.id.btnCreateRoomNow);

        WebSocketManager.getInstance().setListener(this);

        btnCreateRoomNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createRoom();
            }
        });
    }

    private void createRoom() {
        String username = edtUsername.getText().toString().trim();
        String timeText = edtQuestionTime.getText().toString().trim();

        if (username.isEmpty()) {
            txtStatus.setText("Kullanıcı adı boş olamaz.");
            return;
        }

        int questionTime = 20;

        try {
            if (!timeText.isEmpty()) {
                questionTime = Integer.parseInt(timeText);
            }
        } catch (Exception e) {
            questionTime = 20;
        }

        if (questionTime < 5) {
            txtStatus.setText("Soru süresi en az 5 saniye olmalı.");
            return;
        }

        pendingUsername = username;
        pendingQuestionTime = questionTime;

        txtStatus.setText("Sunucuya bağlanılıyor...");

        if (WebSocketManager.getInstance().isConnected()) {
            sendCreateRoomMessage();
        } else {
            shouldSendCreateRoomAfterConnect = true;
            WebSocketManager.getInstance().connect();
        }
    }

    private void sendCreateRoomMessage() {
        shouldSendCreateRoomAfterConnect = false;

        String message = SocketMessageFactory.createRoom(
                pendingUsername,
                pendingQuestionTime
        );

        WebSocketManager.getInstance().sendMessage(message);

        txtStatus.setText("Oda oluşturma isteği gönderildi...");
    }

    @Override
    public void onSocketConnected() {
        txtStatus.setText("Sunucuya bağlandı.");

        if (shouldSendCreateRoomAfterConnect) {
            sendCreateRoomMessage();
        }
    }

    @Override
    public void onSocketMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            if ("room_created".equals(type)) {
                String roomCode = json.optString("room_code");
                String username = json.optString("username");
                int questionTime = json.optInt("question_time", pendingQuestionTime);

                txtStatus.setText("Oda oluşturuldu: " + roomCode);

                ((MainActivity) requireActivity()).openOwnerRoomFragment(
                        roomCode,
                        username,
                        questionTime
                );

            } else if ("error".equals(type)) {
                txtStatus.setText(json.optString("message", "Bilinmeyen hata oluştu."));
            }

        } catch (Exception e) {
            txtStatus.setText("JSON okuma hatası: " + e.getMessage());
        }
    }

    @Override
    public void onSocketDisconnected() {
        txtStatus.setText("Sunucu bağlantısı kapandı.");
    }

    @Override
    public void onSocketError(String error) {
        txtStatus.setText("Bağlantı hatası: " + error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WebSocketManager.getInstance().removeListener(this);
    }
}
