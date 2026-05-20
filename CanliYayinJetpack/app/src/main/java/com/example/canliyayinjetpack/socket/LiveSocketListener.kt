package com.example.canliyayinjetpack.socket


interface LiveSocketListener {

    fun onConnected()

    fun onMessage(message: String)

    fun onError(error: String)

    fun onDisconnected()
}