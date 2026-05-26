package com.example.onlinequizjava.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlinequizjava.MainActivity;
import com.example.onlinequizjava.R;
import com.example.onlinequizjava.network.SocketEventListener;
import com.example.onlinequizjava.network.WebSocketManager;

import org.json.JSONArray;
import org.json.JSONObject;

public class WaitingRoomFragment extends Fragment implements SocketEventListener {

    private TextView txtWaitingInfo;
    private TextView txtPlayers;

    private String roomCode = "";
    private String username = "";
    private int questionTime = 20;

    public WaitingRoomFragment() {
        super(R.layout.fragment_waiting_room);
    }

    public static WaitingRoomFragment newInstance(
            String roomCode,
            String username,
            int questionTime
    ) {
        WaitingRoomFragment fragment = new WaitingRoomFragment();

        Bundle bundle = new Bundle();
        bundle.putString("roomCode", roomCode);
        bundle.putString("username", username);
        bundle.putInt("questionTime", questionTime);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = requireArguments();

        roomCode = args.getString("roomCode", "");
        username = args.getString("username", "");
        questionTime = args.getInt("questionTime", 20);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        txtWaitingInfo = view.findViewById(R.id.txtWaitingInfo);
        txtPlayers = view.findViewById(R.id.txtPlayers);

        WebSocketManager.getInstance().setListener(this);

        txtWaitingInfo.setText(
                "Kullanıcı: " + username + "\n" +
                        "Oda Kodu: " + roomCode + "\n" +
                        "Soru Süresi: " + questionTime + " saniye\n\n" +
                        "Oda sahibi quizi başlatınca sorular ekrana gelecek."
        );
    }

    @Override
    public void onSocketConnected() {

    }

    @Override
    public void onSocketMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.optString("type");

            if ("player_list_updated".equals(type)) {
                JSONArray players = json.optJSONArray("players");
                txtPlayers.setText(buildPlayerText(players));

            } else if ("quiz_started".equals(type)) {
                txtWaitingInfo.setText("Quiz başladı.");

                ((MainActivity) requireActivity()).openQuizFragment(
                        roomCode,
                        username,
                        questionTime,
                        false
                );

            } else if ("error".equals(type)) {
                txtWaitingInfo.setText(json.optString("message", "Bilinmeyen hata oluştu."));
            }

        } catch (Exception e) {
            txtWaitingInfo.setText("JSON okuma hatası: " + e.getMessage());
        }
    }

    private String buildPlayerText(JSONArray players) {
        if (players == null || players.length() == 0) {
            return "Oyuncular yükleniyor...";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Odada bulunan oyuncular:\n\n");

        for (int i = 0; i < players.length(); i++) {
            builder.append(i + 1)
                    .append(". ")
                    .append(players.optString(i))
                    .append("\n");
        }

        return builder.toString();
    }

    @Override
    public void onSocketDisconnected() {
        txtWaitingInfo.setText("Sunucu bağlantısı kapandı.");
    }

    @Override
    public void onSocketError(String error) {
        txtWaitingInfo.setText("Bağlantı hatası: " + error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        WebSocketManager.getInstance().removeListener(this);
    }
}
