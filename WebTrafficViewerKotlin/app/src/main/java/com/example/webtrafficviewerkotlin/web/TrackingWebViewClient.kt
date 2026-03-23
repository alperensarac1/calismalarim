package com.example.webtrafficviewerkotlin.web


import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.webtrafficviewerkotlin.model.FilterOptions
import com.example.webtrafficviewerkotlin.model.NetworkLog
import com.example.webtrafficviewerkotlin.util.JsHookProvider
import com.example.webtrafficviewerkotlin.util.RequestFilterUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingWebViewClient(
    private val getFilterOptions: () -> FilterOptions,
    private val onRequestCaptured: (NetworkLog) -> Unit
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {

        if (request != null) {
            val url = request.url.toString()
            val method = request.method ?: "UNKNOWN"
            val options = getFilterOptions()

            if (options.enableFilter) {
                if (method != "GET" && method != "POST") {
                    return super.shouldInterceptRequest(view, request)
                }

                if (RequestFilterUtils.shouldIgnore(url)) {
                    return super.shouldInterceptRequest(view, request)
                }

                if (options.onlyApiRequests && !RequestFilterUtils.looksLikeApi(url)) {
                    return super.shouldInterceptRequest(view, request)
                }
            }

            val host = request.url.host ?: "Bilinmiyor"
            val headers = request.requestHeaders ?: emptyMap()
            val isMainFrame = request.isForMainFrame
            val resourceType = RequestFilterUtils.guessResourceType(url)
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            val log = NetworkLog(
                method = method,
                url = url,
                host = host,
                time = time,
                headers = headers,
                isMainFrame = isMainFrame,
                resourceType = resourceType,
                requestBody = null,
                source = "WEBVIEW"
            )

            onRequestCaptured(log)
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        view?.evaluateJavascript(JsHookProvider.script, null)
    }
}