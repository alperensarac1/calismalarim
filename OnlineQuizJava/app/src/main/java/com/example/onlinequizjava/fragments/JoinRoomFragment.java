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
import com.example.onlinequizjava.network.SocketMessageFactory;
import com.example.onlinequizjava.network.WebSocketManager;

import org.json.JSONObject;

public class JoinRoomFragment extends Fragment implements SocketEventListener {

    private EditText edtUsername;
    private EditText edtRoomCode;
    private TextView txtStatus;

    private String pendingUsername = "";
    private String pendingRoomCode = "";
    private boolean shouldSendJoinAfterConnect = false;

    public JoinRoomFragment() {
        super(R.layout.fragment_join_room);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        edtUsername = view.findViewById(R.id.edtUsername);
        edtRoomCode = view.findViewById(R.id.edtRoomCode);
        txtStatus = view.findViewById(R.id.txtStatus);

        Button btnJoinRoomNow = view.findViewById(R.id.btnJoinRoomNow);

        WebSocketManager.getInstance().setListener(this);

        btnJoinRoomNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinRoom();
            }
        });
    }

    private void joinRoom() {
        String username = edtUsername.getText().toString().trim();
        String roomCode = edtRoomCode.getText().toString().trim();

        if (username.isEmpty()) {
            txtStatus.setText("Kullanıcı adı boş olamaz.");
            return;
        }

        if (roomCode.isEmpty()) {
            txtStatus.setText("Oda kodu boş olamaz.");
            return;
        }

        pendingUsername = username;
        pendingRoomCode = roomCode;

        txtStatus.setText("Sunucuya bağlanılıyor...");

        if (WebSocketManager.getInstance().isConnected()) {
            sendJoinRoomMessage();
        } else {
            shouldSendJoinAfterConnect = true;
            WebSocketManager.getInstance().connect();
        }
    }

    private void sendJoinRoomMessage() {
        shouldSendJoinAfterConnect = false;

        String message = SocketMessageFactory.joinRoom(
                pendingRoomCode,
                pendingUsername
        );

        WebSocketManager.getInstance().sendMessage(message);

        txtStatus.setText("Odaya katılma isteği gönderildi...");
    }

    @Override
    public void onSocketConnected() {
        txtStatus.setText("Sunucuya bağlandı.");

        if (shouldSendJoinAfterConnect) {
            sendJoinRoomMessage();
        }
    }

    @Override
    public void onSocketMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            if ("room_joined".equals(type)) {
                String roomCode = json.optString("room_code");
                String username = json.optString("username");
                int questionTime = json.optInt("question_time", 20);

                ((MainActivity) requireActivity()).openWaitingRoomFragment(
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