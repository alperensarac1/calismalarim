package com.example.qryoklamajetpack.view

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.qryoklamajetpack.data.Prefs

@Composable
fun StudentAttendanceScreen() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val studentNo = prefs.getStudentNo()

    if (studentNo.isNullOrBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Öğrenci numarası bulunamadı.")
        }
        return
    }

    val url = remember(studentNo) {
        Uri.parse("https://alperensaracdeneme.com")
            .buildUpon()
            .appendEncodedPath("qryoklama/student/attendance.php")
            .appendQueryParameter("student_no", studentNo)
            .build()
            .toString()
    }

    var loading by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    val s = settings
                    s.javaScriptEnabled = true
                    s.domStorageEnabled = true
                    s.databaseEnabled = true
                    s.loadsImagesAutomatically = true
                    s.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    s.builtInZoomControls = true
                    s.displayZoomControls = false
                    s.useWideViewPort = true
                    s.loadWithOverviewMode = true

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
                        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                            handler.proceed()
                        }
                    }

                    webChromeClient = WebChromeClient()
                    loadUrl(url)
                }
            }
        )

        if (loading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
    }
}
