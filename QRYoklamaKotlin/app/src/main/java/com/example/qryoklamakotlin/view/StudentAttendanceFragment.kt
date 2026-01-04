package com.example.qryoklamakotlin.view


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.example.qryoklamakotlin.data.Prefs
import com.example.qryoklamakotlin.databinding.FragmentStudentAttendanceBinding

class StudentAttendanceFragment : Fragment() {

    private var _binding: FragmentStudentAttendanceBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val BASE = "https://alperensaracdeneme.com"
        private const val PATH = "/qryoklama/student/attendance.php"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Öğrenci no (Prefs)
        val prefs = Prefs(requireContext())
        val studentNo = prefs.getStudentNo()

        if (studentNo.isNullOrBlank()) {
            binding.webAttendance.loadData(
                "<html><body style='font-family:sans-serif;padding:16px'>Öğrenci numarası bulunamadı.</body></html>",
                "text/html",
                "utf-8"
            )
            return
        }

        // URL’yi güvenli oluştur
        val url = Uri.parse(BASE)
            .buildUpon()
            .appendEncodedPath(PATH.replaceFirst("^/".toRegex(), "")) // tek slash
            .appendQueryParameter("student_no", studentNo)
            .build()
            .toString()

        // WebView ayarları
        val s = binding.webAttendance.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadsImagesAutomatically = true
        s.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.useWideViewPort = true
        s.loadWithOverviewMode = true

        // Uygulama içinde kalsın
        binding.webAttendance.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                binding.progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                binding.progress.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                // Üretimde: kullanıcıya uyarı göstermek daha iyi olabilir
                handler.proceed()
            }
        }

        // (Opsiyonel) JS alert/confirm destekleri
        binding.webAttendance.webChromeClient = WebChromeClient()

        // Yükle
        binding.webAttendance.loadUrl(url)

        // Fragment içinde geri tuşu desteği (WebView back)
        view.isFocusableInTouchMode = true
        view.requestFocus()
        view.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK &&
                event.action == KeyEvent.ACTION_UP &&
                binding.webAttendance.canGoBack()
            ) {
                binding.webAttendance.goBack()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        _binding?.webAttendance?.let { wv ->
            wv.loadUrl("about:blank")
            wv.stopLoading()
            wv.webChromeClient = null
            wv.webViewClient = WebViewClient()
            wv.destroy()
        }
        _binding = null
        super.onDestroyView()
    }
}
