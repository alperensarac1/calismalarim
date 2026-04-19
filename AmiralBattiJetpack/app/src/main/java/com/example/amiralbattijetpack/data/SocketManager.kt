package com.example.amiralbattijetpack.data

import com.example.amiralbattijetpack.config.AppConfig
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
    private var listener: SocketEventListener? = null
    private var isConnected = false

    fun setListener(newListener: SocketEventListener?) {
        listener = newListener
        android.util.Log.d("SocketManager", "setListener = ${newListener?.javaClass?.simpleName}")
    }

    fun clearListener(owner: SocketEventListener) {
        android.util.Log.d(
            "SocketManager",
            "clearListener requested by = ${owner.javaClass.simpleName}, current = ${listener?.javaClass?.simpleName}"
        )
        if (listener === owner) {
            listener = null
            android.util.Log.d("SocketManager", "listener cleared")
        } else {
            android.util.Log.d("SocketManager", "listener not cleared because owner is not current listener")
        }
    }

    fun connect() {
        if (isConnected) {
            listener?.onConnected()
            return
        }

        val request = Request.Builder()
            .url(AppConfig.getWebSocketUrl())
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                listener?.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                listener?.onMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                listener?.onDisconnected()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                listener?.onDisconnected()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                listener?.onError(t.message ?: "Bağlantı hatası")
            }
        })
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closed")
        webSocket = null
        isConnected = false
    }

    fun isSocketConnected(): Boolean = isConnected
}
