package com.example.onlinetaksijava.data.remote.socket;

import androidx.annotation.Nullable;

import com.example.onlinetaksijava.util.Constants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class SocketManager {

    public interface SocketEventListener {
        void onConnected();
        void onDisconnected();
        void onMessage(String message);
        void onError(String errorMessage);
    }

    private static final OkHttpClient client = new OkHttpClient();
    private static WebSocket webSocket;
    private static SocketEventListener listener;
    private static boolean isConnected = false;

    public static void setListener(@Nullable SocketEventListener newListener) {
        listener = newListener;
    }

    public static void connect(String token) {
        if (isConnected) return;

        Request request = new Request.Builder()
                .url(Constants.WS_BASE_URL + "?token=" + token)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                isConnected = true;
                if (listener != null) listener.onConnected();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (listener != null) listener.onMessage(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                if (listener != null) listener.onMessage(bytes.utf8());
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                if (listener != null) listener.onDisconnected();
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                isConnected = false;
                if (listener != null) listener.onDisconnected();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                isConnected = false;
                if (listener != null) listener.onError(t.getMessage() != null ? t.getMessage() : "Socket hatası");
            }
        });
    }

    public static void sendRawJson(String jsonText) {
        if (webSocket != null) {
            webSocket.send(jsonText);
        }
    }

    public static void sendPing() {
        sendRawJson("{\"event\":\"PING\",\"data\":{}}");
    }

    public static void disconnect() {
        isConnected = false;
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnected");
            webSocket = null;
        }
    }

    public static boolean isSocketConnected() {
        return isConnected;
    }
}
