package com.example.qryoklamajava.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.qryoklamajava.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SinavWebView extends Fragment {

    private static final String ARG_STUDENT_NO = "arg_student_no";
    // JSON config dosyanın TAM yolu
    private static final String CONFIG_URL = "https://alperensaracdeneme.com/okul/sinavsitesi.php";

    private WebView webView;
    private ProgressBar progressBar;
    private Button btnYenile;

    private String studentNo;
    private String url; // examseatfinder URL + studentNo

    public static SinavWebView newInstance(String studentNo) {
        SinavWebView fragment = new SinavWebView();
        Bundle args = new Bundle();
        args.putString(ARG_STUDENT_NO, studentNo);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sinav_web_view, container, false);

        webView = view.findViewById(R.id.webViewSinav);
        progressBar = view.findViewById(R.id.progressBar);
        btnYenile = view.findViewById(R.id.btnRefresh);

        if (getArguments() != null) {
            studentNo = getArguments().getString(ARG_STUDENT_NO, null);
        }

        if (studentNo == null || studentNo.trim().isEmpty()) {
            Toast.makeText(requireContext(),
                    "Öğrenci numarası parametre olarak gelmedi!",
                    Toast.LENGTH_LONG).show();
            return view;
        }

        setupWebView();

        // 1) Önce JSON config’i çek, 2) Sonra gerçek URL’yi yükle
        fetchConfigAndLoad();

        btnYenile.setOnClickListener(v -> {
            if (url != null) {
                webView.loadUrl(url);   // son hesaplanan gerçek URL
            } else {
                Toast.makeText(requireContext(),
                        "URL henüz hazırlanmadı, lütfen tekrar deneyin.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /** sinavsitesi.php'den JSON alıp "giris" base URL'siyle gerçek URL'yi oluşturur. */
    private void fetchConfigAndLoad() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL configUrl = new URL(CONFIG_URL);
                conn = (HttpURLConnection) configUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String raw = sb.toString();
                Log.d("SinavWebView", "Config response: " + raw);

                // JSON: {"giris":"https://examseatfinder.onrender.com/query-seat?student_number=","site":"..."}
                JSONObject json = new JSONObject(raw);
                String baseUrl = json.optString("giris", null);

                if (baseUrl == null || baseUrl.isEmpty()) {
                    throw new Exception("Config JSON içinde 'giris' yok.");
                }

                String encodedNo = URLEncoder.encode(studentNo, "UTF-8");
                url = baseUrl + encodedNo;   // ör: https://examseatfinder...student_number=123

                requireActivity().runOnUiThread(() -> {
                    webView.loadUrl(url);
                    progressBar.setVisibility(View.GONE);
                });

            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage();
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Ayarlar alınamadı: " + msg,
                            Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webView != null) {
            webView.destroy();
        }
    }
}
