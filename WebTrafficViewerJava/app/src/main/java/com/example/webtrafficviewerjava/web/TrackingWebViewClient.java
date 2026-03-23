package com.example.webtrafficviewerjava.web;


import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.webtrafficviewerjava.model.FilterOptions;
import com.example.webtrafficviewerjava.model.NetworkLog;
import com.example.webtrafficviewerjava.util.RequestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TrackingWebViewClient extends WebViewClient {

    public interface FilterProvider {
        FilterOptions getFilterOptions();
    }

    public interface OnRequestCapturedListener {
        void onRequestCaptured(NetworkLog log);
    }

    private final FilterProvider filterProvider;
    private final OnRequestCapturedListener listener;

    public TrackingWebViewClient(FilterProvider filterProvider,
                                 OnRequestCapturedListener listener) {
        this.filterProvider = filterProvider;
        this.listener = listener;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (request != null) {
            String url = request.getUrl().toString();
            String method = request.getMethod() != null ? request.getMethod() : "UNKNOWN";
            Uri uri = request.getUrl();
            String host = uri.getHost() != null ? uri.getHost() : "Bilinmiyor";
            Map<String, String> headers = request.getRequestHeaders();
            boolean isMainFrame = request.isForMainFrame();
            String resourceType = RequestUtils.guessResourceType(url);
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            NetworkLog log = new NetworkLog(
                    method,
                    url,
                    host,
                    time,
                    headers,
                    isMainFrame,
                    resourceType,
                    null,
                    "WEBVIEW"
            );

            if (listener != null) {
                listener.onRequestCaptured(log);
            }
        }

        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        String jsCode =
                "(function() {" +
                        "if (window.__ALPEREN_HOOK_INSTALLED__) return;" +
                        "window.__ALPEREN_HOOK_INSTALLED__ = true;" +

                        "function safeStringify(value) {" +
                        "try {" +
                        "if (typeof value === 'string') return value;" +
                        "return JSON.stringify(value);" +
                        "} catch (e) {" +
                        "return '[stringify_error]';" +
                        "}" +
                        "}" +

                        "function notifyAndroid(data) {" +
                        "try {" +
                        "if (window.AndroidLogger && window.AndroidLogger.onRequestCaptured) {" +
                        "window.AndroidLogger.onRequestCaptured(JSON.stringify(data));" +
                        "}" +
                        "} catch (e) {}" +
                        "}" +

                        "const originalFetch = window.fetch;" +
                        "window.fetch = function(input, init) {" +
                        "let url = '';" +
                        "let method = 'GET';" +
                        "let headers = {};" +
                        "let body = null;" +

                        "try {" +
                        "if (typeof input === 'string') {" +
                        "url = input;" +
                        "} else if (input && input.url) {" +
                        "url = input.url;" +
                        "}" +

                        "if (init && init.method) {" +
                        "method = init.method;" +
                        "} else if (input && input.method) {" +
                        "method = input.method;" +
                        "}" +

                        "if (init && init.body !== undefined) {" +
                        "body = safeStringify(init.body);" +
                        "}" +
                        "} catch (e) {}" +

                        "notifyAndroid({" +
                        "source:'JS_HOOK'," +
                        "type:'fetch'," +
                        "url:url," +
                        "method:method," +
                        "body:body" +
                        "});" +

                        "return originalFetch.apply(this, arguments);" +
                        "};" +

                        "const originalOpen = XMLHttpRequest.prototype.open;" +
                        "const originalSend = XMLHttpRequest.prototype.send;" +

                        "XMLHttpRequest.prototype.open = function(method, url) {" +
                        "this.__req_method = method || 'GET';" +
                        "this.__req_url = url || '';" +
                        "return originalOpen.apply(this, arguments);" +
                        "};" +

                        "XMLHttpRequest.prototype.send = function(body) {" +
                        "notifyAndroid({" +
                        "source:'JS_HOOK'," +
                        "type:'xhr'," +
                        "url:this.__req_url || ''," +
                        "method:this.__req_method || 'GET'," +
                        "body:safeStringify(body)" +
                        "});" +
                        "return originalSend.apply(this, arguments);" +
                        "};" +
                        "})();";

        view.evaluateJavascript(jsCode, null);
    }
}
