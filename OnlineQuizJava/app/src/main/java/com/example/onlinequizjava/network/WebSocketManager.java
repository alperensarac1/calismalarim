package com.example.onlinequizjava.network;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {

    /*
        Android Emulator içinden bilgisayardaki Python server'a bağlanmak için:

        ws://10.0.2.2:8765

        Eğer fiziksel telefon kullanırsan:
        ws://BILGISAYAR_IP_ADRESI:8765

        Örnek:
        ws://192.168.1.35:8765
    */

    private static final String SERVER_URL = "ws://10.0.2.2:8765";

    private static WebSocketManager instance;

    private final OkHttpClient client;
    private final Handler mainHandler;

    private WebSocket webSocket;
    private SocketEventListener listener;
    private boolean connected = false;

    private WebSocketManager() {
        client = new OkHttpClient();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }

        return instance;
    }

    public void setListener(SocketEventListener listener) {
        this.listener = listener;
    }

    public void removeListener(SocketEventListener oldListener) {
        if (this.listener == oldListener) {
            this.listener = null;
        }
    }

    public boolean isConnected() {
        return connected && webSocket != null;
    }

    public void connect() {
        if (isConnected()) {
            if (listener != null) {
                listener.onSocketConnected();
            }
            return;
        }

        Request request = new Request.Builder()
                .url(SERVER_URL)
                .build();

        webSocket = client.newWebSocket(
                request,
                new WebSocketListener() {

                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        connected = true;

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onSocketConnected();
                                }
                            }
                        });
                    }

                    @Override
                    public void onMessage(WebSocket webSocket, String text) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onSocketMessage(text);
                                }
                            }
                        });
                    }

                    @Override
                    public void onClosing(WebSocket webSocket, int code, String reason) {
                        connected = false;
                        webSocket.close(code, reason);

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onSocketDisconnected();
                                }
                            }
                        });
                    }

                    @Override
                    public void onClosed(WebSocket webSocket, int code, String reason) {
                        connected = false;

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onSocketDisconnected();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(
                            WebSocket webSocket,
                            Throwable t,
                            @Nullable Response response
                    ) {
                        connected = false;

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) {
                                    listener.onSocketError(
                                            t.getMessage() != null
                                                    ? t.getMessage()
                                                    : "WebSocket bağlantı hatası"
                                    );
                                }
                            }
                        });
                    }
                }
        );
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void disconnect() {
        connected = false;

        if (webSocket != null) {
            webSocket.close(1000, "Kullanıcı çıkış yaptı");
            webSocket = null;
        }
    }
}
