package com.example.webtrafficviewerkotlin.util

object JsHookProvider {

    val script = """
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
                let headers = {};
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

                    if (init && init.headers) {
                        headers = init.headers;
                    }

                    if (init && init.body !== undefined) {
                        body = safeStringify(init.body);
                    }
                } catch (e) {}

                notifyAndroid({
                    source: 'JS_HOOK',
                    type: 'fetch',
                    url: url,
                    method: method,
                    headers: headers,
                    body: body
                });

                return originalFetch.apply(this, arguments);
            };

            const originalOpen = XMLHttpRequest.prototype.open;
            const originalSend = XMLHttpRequest.prototype.send;
            const originalSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;

            XMLHttpRequest.prototype.open = function(method, url) {
                this.__req_method = method || 'GET';
                this.__req_url = url || '';
                this.__req_headers = {};
                return originalOpen.apply(this, arguments);
            };

            XMLHttpRequest.prototype.setRequestHeader = function(key, value) {
                try {
                    this.__req_headers = this.__req_headers || {};
                    this.__req_headers[key] = value;
                } catch (e) {}
                return originalSetRequestHeader.apply(this, arguments);
            };

            XMLHttpRequest.prototype.send = function(body) {
                notifyAndroid({
                    source: 'JS_HOOK',
                    type: 'xhr',
                    url: this.__req_url || '',
                    method: this.__req_method || 'GET',
                    headers: this.__req_headers || {},
                    body: safeStringify(body)
                });

                return originalSend.apply(this, arguments);
            };
        })();
    """.trimIndent()
}