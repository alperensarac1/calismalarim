package com.example.qryoklamajava.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.qryoklamajava.R;
import com.example.qryoklamajava.data.Prefs;
import com.example.qryoklamajava.databinding.FragmentStudentAttendanceBinding;


public class StudentAttendanceFragment extends Fragment {


    FragmentStudentAttendanceBinding binding;
    private static final String BASE = "https://alperensaracdeneme.com";
    private static final String PATH = "/qryoklama/student/attendance.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentAttendanceBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Öğrenci no (Prefs)
        Prefs prefs = new Prefs(requireContext());
        String studentNo = prefs.getStudentNo();
        if (studentNo == null || studentNo.trim().isEmpty()) {
            // Güvenlik: Öğrenci no yoksa boş sayfa açma, kullanıcıyı yönlendir
            binding.webAttendance.loadData("<html><body style='font-family:sans-serif;padding:16px'>Öğrenci numarası bulunamadı.</body></html>",
                    "text/html", "utf-8");
            return;
        }

        // URL’yi güvenli oluştur
        String url = Uri.parse(BASE)
                .buildUpon()
                .appendEncodedPath(PATH.replaceFirst("^/", "")) // tek slash
                .appendQueryParameter("student_no", studentNo)
                .build()
                .toString();

        // WebView ayarları
        WebSettings s = binding.webAttendance.getSettings();
        s.setJavaScriptEnabled(true);          // Bootstrap / interactivity için
        s.setDomStorageEnabled(true);          // localStorage/DOMStorage
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        // Uygulama içinde kalsın
        binding.webAttendance.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                binding.progress.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                binding.progress.setVisibility(View.GONE);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Tüm tıklamalar WebView içinde kalsın
                view.loadUrl(request.getUrl().toString());
                return true;
            }
            @Override public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Üretimde kullanıcıya uyarı göstermeyi tercih edin.
                handler.proceed(); // Geçici: kendi alan adın düzgün SSL ise sorun olmaz
            }
        });

        // (Opsiyonel) JS alert/confirm destekleri
        binding.webAttendance.setWebChromeClient(new WebChromeClient());

        // Yükle
        binding.webAttendance.loadUrl(url);

        // Fragment içinde geri tuşu desteği (isteğe bağlı)
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && binding.webAttendance.canGoBack()) {
                binding.webAttendance.goBack();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        if (binding.webAttendance != null) {
            binding.webAttendance.loadUrl("about:blank");
            binding.webAttendance.stopLoading();
            binding.webAttendance.setWebChromeClient(null);
            binding.webAttendance.setWebViewClient(null);
            binding.webAttendance .destroy();
        }
        super.onDestroyView();
    }
}
