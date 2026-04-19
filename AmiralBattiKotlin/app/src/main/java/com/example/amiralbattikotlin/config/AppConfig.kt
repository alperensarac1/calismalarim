package com.example.amiralbattikotlin.config

object AppConfig {

    // Senin Mac IP adresin
    var SERVER_IP = "10.0.2.2"

    // Node.js portu
    var SERVER_PORT = 8080

    fun getWebSocketUrl(): String {
        return "ws://$SERVER_IP:$SERVER_PORT"
    }

    fun getHttpBaseUrl(): String {
        return "http://$SERVER_IP:$SERVER_PORT/"
    }
}