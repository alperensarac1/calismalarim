package com.example.onlinequizjava.network;

public interface SocketEventListener {

    /*
        WebSocketManager gelen olayları Fragment'lara bu interface ile bildirir.

        Her Fragment isterse bu interface'i implemente eder.
    */

    void onSocketConnected();

    void onSocketMessage(String message);

    void onSocketDisconnected();

    void onSocketError(String error);
}
