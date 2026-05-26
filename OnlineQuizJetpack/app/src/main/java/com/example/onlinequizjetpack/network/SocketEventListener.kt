package com.example.onlinequizjetpack.network

/*
    WebSocketManager, gelen olayları ViewModel'e bu interface üzerinden bildirir.

    Neden doğrudan Composable ekranlara değil de ViewModel'e bildiriyoruz?

    Çünkü:
    - UI sadece ekrana odaklanmalı.
    - WebSocket ve iş mantığı ViewModel'de olmalı.
    - Ekran döndüğünde veya yeniden çizildiğinde bağlantı yönetimi bozulmamalı.
*/

interface SocketEventListener {

    fun onSocketConnected()

    fun onSocketMessage(message: String)

    fun onSocketDisconnected()

    fun onSocketError(error: String)
}