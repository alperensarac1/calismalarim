package com.example.amiralbattijetpack.config

object AppConfig {
    var SERVER_IP = "10.19.82.112"
    var SERVER_PORT = 8080

    fun getWebSocketUrl(): String = "ws://$SERVER_IP:$SERVER_PORT"
    fun getHttpBaseUrl(): String = "http://$SERVER_IP:$SERVER_PORT/"
}
