package com.example.onlinetaksi.data.remote.model

data class WsMessage(
    val event: String,
    val data: Map<String, Any>?
)