package com.example.amiralbattikotlin.manager

import com.example.amiralbattikotlin.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object SocketManager {

    interface SocketEventListener {
        fun onConnected()
        fun onDisconnected()
        fun onMessage(message: String)
        fun onError(errorMessage: String)
    }

    private val client = OkHttpClient()

    private var webSocket: WebSocket? = null

    @Volatile
    private var listener: SocketEventListener? = null

    @Volatile
    private var isConnected = false

    fun setListener(newListener: SocketEventListener?) {
        println("[SOCKET_MANAGER] setListener -> ${newListener?.javaClass?.simpleName ?: "null"}")
        listener = newListener
    }

    fun clearListener(target: SocketEventListener) {
        if (listener === target) {
            println("[SOCKET_MANAGER] clearListener -> ${target.javaClass.simpleName}")
            listener = null
        } else {
            println("[SOCKET_MANAGER] clearListener atlandı, aktif listener başka ekran")
        }
    }

    fun connect() {
        if (isConnected) {
            println("[SOCKET_MANAGER] zaten bağlı")
            listener?.onConnected()
            return
        }

        val request = Request.Builder()
            .url(AppConfig.getWebSocketUrl())
            .build()

        println("[SOCKET_MANAGER] connect -> ${AppConfig.getWebSocketUrl()}")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("[SOCKET_MANAGER] onOpen")
                isConnected = true
                listener?.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("[SOCKET_MANAGER] onMessage -> $text")
                listener?.onMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("[SOCKET_MANAGER] onClosing -> code=$code reason=$reason")
                isConnected = false
                listener?.onDisconnected()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("[SOCKET_MANAGER] onClosed -> code=$code reason=$reason")
                isConnected = false
                listener?.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("[SOCKET_MANAGER] onFailure -> ${t.message}")
                isConnected = false
                listener?.onError(t.message ?: "Bilinmeyen bağlantı hatası")
            }
        })
    }

    fun send(message: String) {
        println("[SOCKET_MANAGER] send -> $message")
        webSocket?.send(message)
    }

    fun disconnect() {
        println("[SOCKET_MANAGER] disconnect")
        webSocket?.close(1000, "Client closed")
        webSocket = null
        isConnected = false
        listener = null
    }

    fun isSocketConnected(): Boolean {
        return isConnected
    }
}