package com.example.qryoklamajetpack.view

import android.app.Activity
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.qryoklamajetpack.data.Prefs
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Composable
fun SinavWebViewScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val studentNo = prefs.getStudentNo()

    if (studentNo.isNullOrBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Öğrenci numarası bulunamadı.")
        }
        return
    }

    val configUrl = "https://alperensaracdeneme.com/okul/sinavsitesi.php"
    var realUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    fun loadConfig() {
        loading = true
        Thread {
            try {
                val conn = (URL(configUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }
                val raw = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val json = JSONObject(raw)
                val base = json.optString("giris", "")
                val encoded = URLEncoder.encode(studentNo, "UTF-8")
                val u = base + encoded

                (ctx as Activity).runOnUiThread {
                    realUrl = u
                    loading = false
                }
            } catch (e: Exception) {
                (ctx as Activity).runOnUiThread {
                    loading = false
                    Toast.makeText(ctx, "Ayarlar alınamadı: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    LaunchedEffect(Unit) { loadConfig() }

    Column(Modifier.fillMaxSize()) {

        Button(
            onClick = { loadConfig() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Yenile") }

        Box(Modifier.fillMaxSize()) {

            val url = realUrl
            if (url != null) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                                    loading = true
                                }
                                override fun onPageFinished(view: WebView, url: String) {
                                    loading = false
                                }
                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                    view.loadUrl(request.url.toString())
                                    return true
                                }
                            }
                            webChromeClient = WebChromeClient()
                            loadUrl(url)
                        }
                    },
                    update = { wv ->
                        if (wv.url != url) wv.loadUrl(url)
                    }
                )
            }

            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}
