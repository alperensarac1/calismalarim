package com.example.onlinequizkotlin.network


interface SocketEventListener {

    /*
        WebSocketManager gelen olayları bu interface üzerinden Fragment'lara bildirir.

        Her Fragment bu interface'i implemente ederek:
        - bağlantı kuruldu mu
        - mesaj geldi mi
        - hata oldu mu
        - bağlantı kapandı mı

        gibi olayları yakalayabilir.
    */

    fun onSocketConnected()

    fun onSocketMessage(message: String)

    fun onSocketDisconnected()

    fun onSocketError(error: String)
}