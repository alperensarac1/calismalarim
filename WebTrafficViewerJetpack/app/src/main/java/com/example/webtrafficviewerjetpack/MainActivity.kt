package com.example.webtrafficviewerjetpack

import com.example.webtrafficviewerjetpack.model.NetworkLog
import com.example.webtrafficviewerjetpack.util.RequestUtils


import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.webtrafficviewerjetpack.util.JsBridge
import com.example.webtrafficviewerjetpack.viewmodel.MainViewModel
import com.example.webtrafficviewerjetpack.web.TrackingWebViewClient

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var webViewRef: WebView? = null
    private val okHttpClient by lazy { OkHttpClient() }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(
                viewModel = viewModel,
                onWebViewReady = { webView ->
                    webViewRef = webView

                    webView.settings.javaScriptEnabled = true
                    webView.settings.domStorageEnabled = true
                    webView.settings.loadsImagesAutomatically = true
                    webView.settings.allowFileAccess = false
                    webView.settings.allowContentAccess = false
                    webView.webChromeClient = WebChromeClient()

                    webView.addJavascriptInterface(
                        JsBridge { json ->
                            runOnUiThread {
                                if (!viewModel.filterOptions.value.enableJsHook) return@runOnUiThread
                                viewModel.parseAndAddJsLog(json)
                            }
                        },
                        "AndroidLogger"
                    )

                    webView.webViewClient = TrackingWebViewClient(
                        onRequestCaptured = { log ->
                            runOnUiThread {
                                viewModel.addLogIfNeeded(log)
                            }
                        }
                    )
                },
                onLoadUrl = { url ->
                    val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                        url
                    } else {
                        "https://$url"
                    }

                    viewModel.clearLogs()
                    viewModel.updateUrlText(finalUrl)
                    webViewRef?.loadUrl(finalUrl)
                },
                onReplayRequest = { log, baseUrl, params, openInWebView, onResult ->
                    replayRequest(
                        originalLog = log,
                        editedBaseUrl = baseUrl,
                        editedParams = params,
                        openInWebView = openInWebView,
                        onResult = onResult
                    )
                }
            )
        }
    }

    private fun replayRequest(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String,
        openInWebView: Boolean,
        onResult: (String) -> Unit
    ) {
        if (editedBaseUrl.isBlank()) {
            onResult("Base URL boş olamaz")
            return
        }

        if (openInWebView) {
            openReplayInWebView(
                originalLog = originalLog,
                editedBaseUrl = editedBaseUrl,
                editedParams = editedParams
            )
        }

        Thread {
            try {
                val request = if (originalLog.method.equals("GET", ignoreCase = true)) {
                    val finalUrl = RequestUtils.buildFinalUrl(editedBaseUrl, editedParams)
                    Request.Builder()
                        .url(finalUrl)
                        .get()
                        .build()
                } else {
                    val mediaType = RequestUtils.detectMediaType(editedParams).toMediaTypeOrNull()
                    val requestBody = editedParams.toRequestBody(mediaType)
                    Request.Builder()
                        .url(editedBaseUrl)
                        .post(requestBody)
                        .build()
                }

                val response = okHttpClient.newCall(request).execute()
                val responseCode = response.code
                val responseHeaders = response.headers.toMultimap()
                val responseBody = response.body?.string().orEmpty()

                val message = buildString {
                    appendLine("HTTP CODE: $responseCode")
                    appendLine()
                    appendLine("REQUEST:")
                    appendLine(buildReplayRequestSummary(originalLog, editedBaseUrl, editedParams))
                    appendLine()
                    appendLine("RESPONSE HEADERS:")
                    appendLine(formatResponseHeaders(responseHeaders))
                    appendLine()
                    appendLine("RESPONSE BODY:")
                    appendLine(responseBody.take(5000))
                }

                runOnUiThread {
                    onResult(message)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    onResult(e.message ?: "Bilinmeyen hata")
                }
            }
        }.start()
    }

    private fun openReplayInWebView(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ) {
        runOnUiThread {
            try {
                if (originalLog.method.equals("GET", ignoreCase = true)) {
                    val finalUrl = RequestUtils.buildFinalUrl(editedBaseUrl, editedParams)
                    webViewRef?.stopLoading()
                    webViewRef?.clearHistory()
                    viewModel.updateUrlText(finalUrl)
                    webViewRef?.loadUrl(finalUrl)
                } else if (originalLog.method.equals("POST", ignoreCase = true)) {
                    val mediaType = RequestUtils.detectMediaType(editedParams)
                    if (mediaType.startsWith("application/x-www-form-urlencoded")) {
                        webViewRef?.stopLoading()
                        viewModel.updateUrlText(editedBaseUrl)
                        webViewRef?.postUrl(editedBaseUrl, editedParams.toByteArray(Charsets.UTF_8))
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun buildReplayRequestSummary(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ): String {
        return buildString {
            appendLine("ORIGINAL METHOD: ${originalLog.method}")
            appendLine("SOURCE         : ${originalLog.source}")
            appendLine("EDITED BASE URL: $editedBaseUrl")
            appendLine("EDITED PARAMS  :")
            appendLine(editedParams.ifBlank { "yok" })
        }
    }

    private fun formatResponseHeaders(headers: Map<String, List<String>>): String {
        if (headers.isEmpty()) return "Header yok"

        return buildString {
            headers.forEach { (key, values) ->
                append(key)
                append(": ")
                append(values.joinToString(", "))
                append("\n")
            }
        }.trim()
    }

    override fun onDestroy() {
        webViewRef?.removeJavascriptInterface("AndroidLogger")
        webViewRef?.destroy()
        webViewRef = null
        super.onDestroy()
    }
}