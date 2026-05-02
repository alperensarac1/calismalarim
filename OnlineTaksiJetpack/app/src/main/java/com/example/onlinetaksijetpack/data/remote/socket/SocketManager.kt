package com.example.onlinetaksijetpack.data.remote.socket


import com.example.onlinetaksijetpack.util.Constants
import okhttp3.*
import okio.ByteString

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
    }

    fun connect(token: String) {
        if (isConnected) return

        val request = Request.Builder()
            .url("${Constants.WS_BASE_URL}?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                listener?.onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                listener?.onMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                listener?.onMessage(bytes.utf8())
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
                listener?.onError(t.message ?: "Socket hatası")
            }
        })
    }

    fun sendPing() {
        webSocket?.send("""{"event":"PING","data":{}}""")
    }

    fun disconnect() {
        isConnected = false
        webSocket?.close(1000, "Client disconnected")
        webSocket = null
    }

    fun isSocketConnected(): Boolean = isConnected
}