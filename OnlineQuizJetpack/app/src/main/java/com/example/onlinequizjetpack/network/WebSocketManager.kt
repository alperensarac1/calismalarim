package com.example.onlinequizjetpack.network

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

object WebSocketManager {

    /*
        Android Emulator içinden bilgisayarda çalışan Python server'a bağlanmak için:

        ws://10.0.2.2:8765

        Fiziksel telefon kullanırsan:
        ws://BILGISAYAR_IP_ADRESI:8765
    */

    private const val SERVER_URL = "ws://10.0.2.2:8765"

    private val client = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    private var webSocket: WebSocket? = null

    private var listener: SocketEventListener? = null

    private var connected: Boolean = false

    fun setListener(newListener: SocketEventListener) {
        listener = newListener
    }

    fun removeListener(oldListener: SocketEventListener) {
        if (listener == oldListener) {
            listener = null
        }
    }

    fun isConnected(): Boolean {
        return connected && webSocket != null
    }

    fun connect() {
        if (isConnected()) {
            listener?.onSocketConnected()
            return
        }

        val request = Request.Builder()
            .url(SERVER_URL)
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    connected = true

                    mainHandler.post {
                        listener?.onSocketConnected()
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    mainHandler.post {
                        listener?.onSocketMessage(text)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    connected = false
                    webSocket.close(code, reason)

                    mainHandler.post {
                        listener?.onSocketDisconnected()
                    }
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    connected = false

                    mainHandler.post {
                        listener?.onSocketDisconnected()
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    connected = false

                    mainHandler.post {
                        listener?.onSocketError(t.message ?: "WebSocket bağlantı hatası")
                    }
                }
            }
        )
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        connected = false
        webSocket?.close(1000, "Kullanıcı çıkış yaptı")
        webSocket = null
    }
}