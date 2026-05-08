package com.example.onlineradiojetpack.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object RadioSocketManager {

    private const val SERVER_URL = "ws://10.0.2.2:8765"

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    var onConnected: (() -> Unit)? = null
    var onMessage: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    fun connect() {
        if (webSocket != null) return

        val request = Request.Builder()
            .url(SERVER_URL)
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    onConnected?.invoke()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    onMessage?.invoke(text)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    onError?.invoke(t.message ?: "WebSocket hatası")
                    this@RadioSocketManager.webSocket = null
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    this@RadioSocketManager.webSocket = null
                }
            }
        )
    }

    fun getRooms() {
        send("""{"type":"GET_ROOMS"}""")
    }

    fun joinRoom(roomId: Int) {
        send("""{"type":"JOIN_ROOM","roomId":$roomId}""")
    }

    fun requestSync(roomId: Int) {
        send("""{"type":"SYNC_REQUEST","roomId":$roomId}""")
    }

    private fun send(message: String) {
        webSocket?.send(message)
    }
}