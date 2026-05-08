package com.example.onlineradyojava.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;

import com.example.onlineradyojava.databinding.ActivityRadioPlayerBinding;
import com.example.onlineradyojava.network.RadioSocketManager;

import org.json.JSONObject;

public class RadioPlayerActivity extends AppCompatActivity {

    private ActivityRadioPlayerBinding binding;

    private ExoPlayer player;
    private RadioSocketManager socketManager;

    private int roomId = -1;
    private String roomName = "";

    private String currentMusicUrl = null;

    private Handler syncHandler = new Handler(Looper.getMainLooper());

    private Runnable syncRunnable = new Runnable() {
        @Override
        public void run() {
            if (roomId != -1) {
                socketManager.requestSync(roomId);
            }

            syncHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRadioPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        socketManager = RadioSocketManager.getInstance();

        roomId = getIntent().getIntExtra("roomId", -1);
        roomName = getIntent().getStringExtra("roomName");

        if (roomName == null) {
            roomName = "Oda";
        }

        binding.tvRoomName.setText(roomName);

        setupPlayer();
        setupSocketListener();

        socketManager.joinRoom(roomId);

        syncHandler.postDelayed(syncRunnable, 5000);
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);
    }

    private void setupSocketListener() {
        socketManager.setOnMessageListener(message -> {
            handleSocketMessage(message);
        });

        socketManager.setOnErrorListener(error -> {
            runOnUiThread(() -> {
                binding.tvStatus.setText("Bağlantı hatası: " + error);
            });
        });
    }

    private void handleSocketMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String type = jsonObject.optString("type");

            if (type.equals("PLAYBACK_STATE")) {
                int incomingRoomId = jsonObject.optInt("roomId");

                if (incomingRoomId != roomId) return;

                String title = jsonObject.optString("title");
                String musicUrl = jsonObject.optString("musicUrl");
                double positionSeconds = jsonObject.optDouble("positionSeconds", 0.0);

                runOnUiThread(() -> {
                    playOrSyncMusic(title, musicUrl, positionSeconds);
                });
            }

            else if (type.equals("NO_MUSIC")) {
                runOnUiThread(() -> {
                    binding.tvMusicTitle.setText("Bu odada şu an müzik yok");
                    binding.tvStatus.setText("Bekleniyor...");

                    if (player != null) {
                        player.pause();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playOrSyncMusic(
            String title,
            String musicUrl,
            double positionSeconds
    ) {
        binding.tvMusicTitle.setText(title);
        binding.tvStatus.setText("Dinleniyor...");

        long targetPositionMs = (long) (positionSeconds * 1000);

        if (currentMusicUrl == null || !currentMusicUrl.equals(musicUrl)) {
            currentMusicUrl = musicUrl;

            MediaItem mediaItem = MediaItem.fromUri(musicUrl);

            player.setMediaItem(mediaItem);
            player.prepare();
            player.seekTo(targetPositionMs);
            player.play();

            return;
        }

        long currentPosition = player.getCurrentPosition();
        long difference = Math.abs(currentPosition - targetPositionMs);

        if (difference > 1200) {
            player.seekTo(targetPositionMs);
        }

        if (!player.isPlaying()) {
            player.play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        syncHandler.removeCallbacks(syncRunnable);

        if (player != null) {
            player.release();
            player = null;
        }
    }
}