package com.example.onlineradyojava.ui;



import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.onlineradyojava.data.RadioRoom;
import com.example.onlineradyojava.databinding.ActivityRoomListBinding;
import com.example.onlineradyojava.network.RadioSocketManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoomListActivity extends AppCompatActivity {

    private ActivityRoomListBinding binding;
    private RoomAdapter roomAdapter;
    private RadioSocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRoomListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        socketManager = RadioSocketManager.getInstance();

        setupRecyclerView();
        setupSocket();
    }

    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(room -> {
            Intent intent = new Intent(
                    RoomListActivity.this,
                    RadioPlayerActivity.class
            );

            intent.putExtra("roomId", room.getId());
            intent.putExtra("roomName", room.getRoomName());

            startActivity(intent);
        });

        binding.rvRooms.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRooms.setAdapter(roomAdapter);
    }

    private void setupSocket() {
        socketManager.setOnConnectedListener(() -> {
            runOnUiThread(() -> {
                binding.tvConnectionStatus.setText("Sunucuya bağlandı");
            });

            socketManager.getRooms();
        });

        socketManager.setOnMessageListener(message -> {
            handleSocketMessage(message);
        });

        socketManager.setOnErrorListener(error -> {
            runOnUiThread(() -> {
                binding.tvConnectionStatus.setText("Hata: " + error);
            });
        });

        socketManager.connect();
    }

    private void handleSocketMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String type = jsonObject.optString("type");

            if (type.equals("ROOM_LIST") || type.equals("ROOM_UPDATED")) {
                JSONArray roomsArray = jsonObject.getJSONArray("rooms");

                List<RadioRoom> rooms = new ArrayList<>();

                for (int i = 0; i < roomsArray.length(); i++) {
                    JSONObject roomJson = roomsArray.getJSONObject(i);

                    RadioRoom room = new RadioRoom(
                            roomJson.getInt("id"),
                            roomJson.getString("roomName"),
                            roomJson.optString("currentMusic", null),
                            roomJson.optBoolean("isPlaying", false),
                            roomJson.optInt("listenerCount", 0)
                    );

                    rooms.add(room);
                }

                runOnUiThread(() -> {
                    roomAdapter.updateRooms(rooms);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        socketManager.getRooms();
    }
}
