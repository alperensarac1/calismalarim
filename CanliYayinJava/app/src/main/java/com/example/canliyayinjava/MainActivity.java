package com.example.canliyayinjava;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnStartBroadcast;
    private Button btnWatchBroadcasts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartBroadcast = findViewById(R.id.btnStartBroadcast);
        btnWatchBroadcasts = findViewById(R.id.btnWatchBroadcasts);

        btnStartBroadcast.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BroadcasterActivity.class);
            startActivity(intent);
        });

        btnWatchBroadcasts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
            startActivity(intent);
        });
    }
}