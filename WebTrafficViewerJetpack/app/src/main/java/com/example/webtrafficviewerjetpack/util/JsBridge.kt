package com.example.webtrafficviewerjetpack.util

import android.webkit.JavascriptInterface

class JsBridge(
    private val onJsonCaptured: (String) -> Unit
) {
    @JavascriptInterface
    fun onRequestCaptured(json: String) {
        onJsonCaptured(json)
    }
}