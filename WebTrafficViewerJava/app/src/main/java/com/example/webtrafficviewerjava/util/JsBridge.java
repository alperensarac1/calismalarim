package com.example.webtrafficviewerjava.util;

import android.webkit.JavascriptInterface;

public class JsBridge {

    public interface OnJsonCapturedListener {
        void onJsonCaptured(String json);
    }

    private final OnJsonCapturedListener listener;

    public JsBridge(OnJsonCapturedListener listener) {
        this.listener = listener;
    }

    @JavascriptInterface
    public void onRequestCaptured(String json) {
        if (listener != null) {
            listener.onJsonCaptured(json);
        }
    }
}
