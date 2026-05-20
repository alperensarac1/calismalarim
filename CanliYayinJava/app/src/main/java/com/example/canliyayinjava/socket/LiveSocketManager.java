package com.example.canliyayinjava.socket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class LiveSocketManager {

    private final String serverUrl;
    private final LiveSocketListener listener;

    private final OkHttpClient client;
    private WebSocket webSocket;

    public LiveSocketManager(String serverUrl, LiveSocketListener listener) {
        this.serverUrl = serverUrl;
        this.listener = listener;
        this.client = new OkHttpClient();
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(serverUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                listener.onConnected();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                listener.onMessage(text);
            }

            @Override
            public void onFailure(
                    @NonNull WebSocket webSocket,
                    @NonNull Throwable t,
                    @Nullable Response response
            ) {
                listener.onError(t.getMessage() != null ? t.getMessage() : "WebSocket hatası");
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                listener.onDisconnected();
            }
        });
    }

    public void sendJson(JSONObject jsonObject) {
        if (webSocket != null) {
            webSocket.send(jsonObject.toString());
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Kullanıcı ayrıldı");
            webSocket = null;
        }
    }
}
