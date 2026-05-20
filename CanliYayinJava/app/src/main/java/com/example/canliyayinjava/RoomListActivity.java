package com.example.canliyayinjava;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canliyayinjava.adapter.RoomAdapter;
import com.example.canliyayinjava.model.RoomModel;
import com.example.canliyayinjava.socket.LiveSocketListener;
import com.example.canliyayinjava.socket.LiveSocketManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoomListActivity extends AppCompatActivity implements LiveSocketListener {

    private RecyclerView rvRooms;
    private TextView tvEmptyRooms;

    private RoomAdapter roomAdapter;
    private LiveSocketManager socketManager;

    private final List<RoomModel> roomList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        rvRooms = findViewById(R.id.rvRooms);
        tvEmptyRooms = findViewById(R.id.tvEmptyRooms);

        setupRecyclerView();

        socketManager = new LiveSocketManager(AppConfig.SERVER_URL, this);
        socketManager.connect();
    }

    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(roomList, room -> {
            Intent intent = new Intent(RoomListActivity.this, ViewerActivity.class);
            intent.putExtra("room_id", room.getRoomId());
            intent.putExtra("room_title", room.getTitle());
            startActivity(intent);
        });

        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(roomAdapter);
    }

    @Override
    public void onConnected() {
        runOnUiThread(() ->
                Toast.makeText(this, "Sunucuya bağlandı", Toast.LENGTH_SHORT).show()
        );

        try {
            JSONObject json = new JSONObject();
            json.put("type", "get_rooms");
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

            if (type.equals("rooms_list")) {
                JSONArray roomsArray = json.getJSONArray("rooms");
                List<RoomModel> newRooms = new ArrayList<>();

                for (int i = 0; i < roomsArray.length(); i++) {
                    JSONObject item = roomsArray.getJSONObject(i);

                    RoomModel room = new RoomModel(
                            item.getString("room_id"),
                            item.getString("title"),
                            item.getString("broadcaster_name"),
                            item.getString("created_at"),
                            item.getInt("viewer_count")
                    );

                    newRooms.add(room);
                }

                runOnUiThread(() -> {
                    roomAdapter.updateRooms(newRooms);

                    if (newRooms.isEmpty()) {
                        tvEmptyRooms.setVisibility(View.VISIBLE);
                        rvRooms.setVisibility(View.GONE);
                    } else {
                        tvEmptyRooms.setVisibility(View.GONE);
                        rvRooms.setVisibility(View.VISIBLE);
                    }
                });
            }

            if (type.equals("error")) {
                runOnUiThread(() -> {
                    try {
                        Toast.makeText(
                                this,
                                json.getString("message"),
                                Toast.LENGTH_SHORT
                        ).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() ->
                Toast.makeText(
                        this,
                        "Sunucuya bağlanılamadı: " + error,
                        Toast.LENGTH_LONG
                ).show()
        );
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() ->
                Toast.makeText(this, "Bağlantı kapandı", Toast.LENGTH_SHORT).show()
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
