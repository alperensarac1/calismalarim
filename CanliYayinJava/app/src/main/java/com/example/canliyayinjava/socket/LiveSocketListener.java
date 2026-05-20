package com.example.canliyayinjava.socket;

public interface LiveSocketListener {

    void onConnected();

    void onMessage(String message);

    void onError(String error);

    void onDisconnected();
}
