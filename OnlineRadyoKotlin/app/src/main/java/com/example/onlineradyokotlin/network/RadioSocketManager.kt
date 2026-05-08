package com.example.onlineradyokotlin.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object RadioSocketManager {

    // Buradaki IP bilgisayarının yerel ağ IP adresi olacak.
    // Python server hangi bilgisayarda çalışıyorsa onun IP'sini yaz.
    private const val SERVER_URL = "ws://192.168.1.10:8765"

    private val client = OkHttpClient()

    private var webSocket: WebSocket? = null

    var onConnected: (() -> Unit)? = null
    var onMessageReceived: ((String) -> Unit)? = null
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
                    onMessageReceived?.invoke(text)
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    onError?.invoke(t.message ?: "WebSocket bağlantı hatası")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    this@RadioSocketManager.webSocket = null
                }
            }
        )
    }

    fun send(message: String) {
        webSocket?.send(message)
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

    fun close() {
        webSocket?.close(1000, "Closed")
        webSocket = null
    }
}