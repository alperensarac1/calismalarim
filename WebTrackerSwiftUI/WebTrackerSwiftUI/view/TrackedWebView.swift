//
//  TrackedWebView.swift
//  WebTrackerSwiftUI
//
//  Created by Alperen Saraç on 21.03.2026.
//

import Foundation
import SwiftUI
import WebKit

struct TrackedWebView: UIViewRepresentable {
    let urlString: String
    let onJsMessage: (String) -> Void
    let onNavigationRequestCaptured: (NetworkLog) -> Void
    let onWebViewCreated: (WKWebView) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(
            onJsMessage: onJsMessage,
            onNavigationRequestCaptured: onNavigationRequestCaptured
        )
    }

    func makeUIView(context: Context) -> WKWebView {
        let contentController = WKUserContentController()
        contentController.add(context.coordinator.messageHandler, name: "iosLogger")

        let js = """
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

            function notifyiOS(data) {
                try {
                    window.webkit.messageHandlers.iosLogger.postMessage(JSON.stringify(data));
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

                notifyiOS({
                    source: 'JS_HOOK',
                    type: 'fetch',
                    url: url,
                    method: method,
                    body: body
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
                notifyiOS({
                    source: 'JS_HOOK',
                    type: 'xhr',
                    url: this.__req_url || '',
                    method: this.__req_method || 'GET',
                    body: safeStringify(body)
                });
                return originalSend.apply(this, arguments);
            };
        })();
        """

        let userScript = WKUserScript(
            source: js,
            injectionTime: .atDocumentEnd,
            forMainFrameOnly: false
        )
        contentController.addUserScript(userScript)

        let config = WKWebViewConfiguration()
        config.userContentController = contentController

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = context.coordinator
        onWebViewCreated(webView)

        if let url = URL(string: normalizedUrl(urlString)) {
            webView.load(URLRequest(url: url))
        }

        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        let current = uiView.url?.absoluteString ?? ""
        let target = normalizedUrl(urlString)

        if !target.isEmpty, current != target, let url = URL(string: target) {
            uiView.load(URLRequest(url: url))
        }
    }

    private func normalizedUrl(_ value: String) -> String {
        if value.hasPrefix("http://") || value.hasPrefix("https://") {
            return value
        }
        return "https://\(value)"
    }

    final class Coordinator: NSObject, WKNavigationDelegate {
        let messageHandler: WebViewMessageHandler
        let onNavigationRequestCaptured: (NetworkLog) -> Void

        init(
            onJsMessage: @escaping (String) -> Void,
            onNavigationRequestCaptured: @escaping (NetworkLog) -> Void
        ) {
            self.messageHandler = WebViewMessageHandler(onJsonReceived: onJsMessage)
            self.onNavigationRequestCaptured = onNavigationRequestCaptured
        }

        func webView(_ webView: WKWebView, decidePolicyFor navigationAction: WKNavigationAction, decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            let request = navigationAction.request
            let method = request.httpMethod ?? "GET"
            let url = request.url?.absoluteString ?? ""
            let host = request.url?.host ?? "Bilinmiyor"
            let headers = request.allHTTPHeaderFields ?? [:]
            let body: String? = {
                guard let data = request.httpBody else { return nil }
                return String(data: data, encoding: .utf8)
            }()

            let log = NetworkLog(
                method: method,
                url: url,
                host: host,
                time: RequestUtils.currentTimeString(),
                headers: headers,
                isMainFrame: navigationAction.targetFrame?.isMainFrame ?? false,
                resourceType: RequestUtils.guessResourceType(url),
                requestBody: body,
                source: "WEBVIEW"
            )

            onNavigationRequestCaptured(log)
            decisionHandler(.allow)
        }
    }
}
