package com.example.qryoklamakotlin.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.qryoklamakotlin.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SinavWebView : Fragment() {

    companion object {
        private const val ARG_STUDENT_NO = "arg_student_no"
        private const val CONFIG_URL = "https://alperensaracdeneme.com/okul/sinavsitesi.php"

        fun newInstance(studentNo: String?): SinavWebView {
            return SinavWebView().apply {
                arguments = Bundle().apply {
                    putString(ARG_STUDENT_NO, studentNo)
                }
            }
        }
    }

    private var webView: WebView? = null
    private var progressBar: ProgressBar? = null
    private var btnYenile: Button? = null

    private var studentNo: String? = null
    private var url: String? = null // gerçek URL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sinav_web_view, container, false)

        webView = view.findViewById(R.id.webViewSinav)
        progressBar = view.findViewById(R.id.progressBar)
        btnYenile = view.findViewById(R.id.btnRefresh)

        studentNo = arguments?.getString(ARG_STUDENT_NO, null)

        if (studentNo.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "Öğrenci numarası parametre olarak gelmedi!",
                Toast.LENGTH_LONG
            ).show()
            return view
        }

        setupWebView()

        // 1) config çek, 2) gerçek URL’yi yükle
        fetchConfigAndLoad()

        btnYenile?.setOnClickListener {
            val realUrl = url
            if (!realUrl.isNullOrBlank()) {
                webView?.loadUrl(realUrl)
            } else {
                Toast.makeText(
                    requireContext(),
                    "URL henüz hazırlanmadı, lütfen tekrar deneyin.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return view
    }

    /** sinavsitesi.php'den JSON alıp "giris" base URL'siyle gerçek URL'yi oluşturur. */
    private fun fetchConfigAndLoad() {
        progressBar?.visibility = View.VISIBLE

        Thread {
            var conn: HttpURLConnection? = null
            try {
                val configUrl = URL(CONFIG_URL)
                conn = (configUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                }

                val code = conn.responseCode
                val inputStream = if (code in 200..299) conn.inputStream else conn.errorStream

                val raw = BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    buildString {
                        var line: String?
                        while (true) {
                            line = reader.readLine() ?: break
                            append(line)
                        }
                    }
                }

                Log.d("SinavWebView", "Config response: $raw")

                // JSON: {"giris":"https://.../query-seat?student_number=","site":"..."}
                val json = JSONObject(raw)
                val baseUrl = json.optString("giris", null)

                if (baseUrl.isNullOrBlank()) {
                    throw Exception("Config JSON içinde 'giris' yok.")
                }

                val encodedNo = URLEncoder.encode(studentNo, "UTF-8")
                url = baseUrl + encodedNo

                activity?.runOnUiThread {
                    webView?.loadUrl(url!!)
                    progressBar?.visibility = View.GONE
                }

            } catch (e: Exception) {
                e.printStackTrace()
                val msg = e.message ?: "Bilinmeyen hata"
                activity?.runOnUiThread {
                    progressBar?.visibility = View.GONE
                    Toast.makeText(requireContext(), "Ayarlar alınamadı: $msg", Toast.LENGTH_LONG)
                        .show()
                }
            } finally {
                conn?.disconnect()
            }
        }.start()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val wv = webView ?: return

        val settings = wv.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        wv.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                progressBar?.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                progressBar?.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        wv.webChromeClient = WebChromeClient()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.destroy()
        webView = null
        progressBar = null
        btnYenile = null
    }
}
