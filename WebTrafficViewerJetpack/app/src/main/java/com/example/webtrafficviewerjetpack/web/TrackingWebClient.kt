package com.example.webtrafficviewerjetpack.web

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.webtrafficviewerjetpack.model.NetworkLog
import com.example.webtrafficviewerjetpack.util.RequestUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingWebViewClient(
    private val onRequestCaptured: (NetworkLog) -> Unit
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        if (request != null) {
            val url = request.url.toString()
            val method = request.method ?: "UNKNOWN"
            val host = request.url.host ?: "Bilinmiyor"
            val headers = request.requestHeaders ?: emptyMap()
            val isMainFrame = request.isForMainFrame
            val resourceType = RequestUtils.guessResourceType(url)
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

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        val jsCode = """
            (function() {
                if (window.__ALPEREN_HOOK_INSTALLED__) return;
                window.__ALPEREN_HOOK_INSTALLED__ = true;

                function safeStringify(value) {
                    try {
                        if (typeof value === 'string') return value;
                        return JSON.stringify(value);
                    } catch (e) {
                        return '[stringify_error]';
                    }
                }

                function notifyAndroid(data) {
                    try {
                        if (window.AndroidLogger && window.AndroidLogger.onRequestCaptured) {
                            window.AndroidLogger.onRequestCaptured(JSON.stringify(data));
                        }
                    } catch (e) {}
                }

                const originalFetch = window.fetch;
                window.fetch = function(input, init) {
                    let url = '';
                    let method = 'GET';
                    let body = null;

                    try {
                        if (typeof input === 'string') {
                            url = input;
                        } else if (input && input.url) {
                            url = input.url;
                        }

                        if (init && init.method) {
                            method = init.method;
                        } else if (input && input.method) {
                            method = input.method;
                        }

                        if (init && init.body !== undefined) {
                            body = safeStringify(init.body);
                        }
                    } catch (e) {}

                    notifyAndroid({
                        source:'JS_HOOK',
                        type:'fetch',
                        url:url,
                        method:method,
                        body:body
                    });

                    return originalFetch.apply(this, arguments);
                };

                const originalOpen = XMLHttpRequest.prototype.open;
                const originalSend = XMLHttpRequest.prototype.send;

                XMLHttpRequest.prototype.open = function(method, url) {
                    this.__req_method = method || 'GET';
                    this.__req_url = url || '';
                    return originalOpen.apply(this, arguments);
                };

                XMLHttpRequest.prototype.send = function(body) {
                    notifyAndroid({
                        source:'JS_HOOK',
                        type:'xhr',
                        url:this.__req_url || '',
                        method:this.__req_method || 'GET',
                        body:safeStringify(body)
                    });
                    return originalSend.apply(this, arguments);
                };
            })();
        """.trimIndent()

        view?.evaluateJavascript(jsCode, null)
    }
}