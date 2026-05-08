package com.example.onlineradyojava.network;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RadioSocketManager {

    private static final String SERVER_URL = "ws://192.168.1.10:8765";

    private static RadioSocketManager instance;

    private OkHttpClient client;
    private WebSocket webSocket;

    private OnConnectedListener onConnectedListener;
    private OnMessageListener onMessageListener;
    private OnErrorListener onErrorListener;

    private RadioSocketManager() {
        client = new OkHttpClient();
    }

    public static RadioSocketManager getInstance() {
        if (instance == null) {
            instance = new RadioSocketManager();
        }
        return instance;
    }

    public void connect() {
        if (webSocket != null) return;

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                if (onConnectedListener != null) {
                    onConnectedListener.onConnected();
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (onMessageListener != null) {
                    onMessageListener.onMessage(text);
                }
            }

            @Override
            public void onFailure(
                    WebSocket webSocket,
                    Throwable t,
                    @Nullable Response response
            ) {
                if (onErrorListener != null) {
                    onErrorListener.onError(t.getMessage());
                }

                RadioSocketManager.this.webSocket = null;
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                RadioSocketManager.this.webSocket = null;
            }
        });
    }

    public void send(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void getRooms() {
        send("{\"type\":\"GET_ROOMS\"}");
    }

    public void joinRoom(int roomId) {
        send("{\"type\":\"JOIN_ROOM\",\"roomId\":" + roomId + "}");
    }

    public void requestSync(int roomId) {
        send("{\"type\":\"SYNC_REQUEST\",\"roomId\":" + roomId + "}");
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Closed");
            webSocket = null;
        }
    }

    public void setOnConnectedListener(OnConnectedListener listener) {
        this.onConnectedListener = listener;
    }

    public void setOnMessageListener(OnMessageListener listener) {
        this.onMessageListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        this.onErrorListener = listener;
    }

    public interface OnConnectedListener {
        void onConnected();
    }

    public interface OnMessageListener {
        void onMessage(String message);
    }

    public interface OnErrorListener {
        void onError(String error);
    }
}
