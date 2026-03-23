package com.example.webtrafficviewerkotlin.model

data class NetworkLog(
    val method: String,
    val url: String,
    val host: String,
    val time: String,
    val headers: Map<String, String> = emptyMap(),
    val isMainFrame: Boolean = false,
    val resourceType: String = "unknown",
    val requestBody: String? = null,
    val source: String = "WEBVIEW" // WEBVIEW veya JS_HOOK
)