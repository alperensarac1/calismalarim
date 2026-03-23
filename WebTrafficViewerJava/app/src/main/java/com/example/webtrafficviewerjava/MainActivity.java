package com.example.webtrafficviewerjava;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webtrafficviewerjava.adapter.NetworkLogAdapter;
import com.example.webtrafficviewerjava.model.FilterOptions;
import com.example.webtrafficviewerjava.model.NetworkLog;
import com.example.webtrafficviewerjava.util.JsBridge;
import com.example.webtrafficviewerjava.util.RequestUtils;
import com.example.webtrafficviewerjava.web.TrackingWebViewClient;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etUrl;
    private Button btnLoad;
    private Button btnCopyAll;
    private CheckBox cbEnableFilter;
    private CheckBox cbOnlyApi;
    private CheckBox cbEnableJsHook;
    private CheckBox cbOnlyGet;
    private CheckBox cbOnlyPost;
    private EditText etSearchUrl;
    private WebView webView;
    private RecyclerView recyclerLogs;

    private NetworkLogAdapter logAdapter;

    private final List<NetworkLog> allLogs = new ArrayList<>();
    private final HashSet<String> seenRequests = new HashSet<>();
    private final OkHttpClient okHttpClient = new OkHttpClient();

    private FilterOptions filterOptions = new FilterOptions();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupRecycler();
        setupWebView();
        setupListeners();
        setDefaults();
    }

    private void bindViews() {
        etUrl = findViewById(R.id.etUrl);
        btnLoad = findViewById(R.id.btnLoad);
        btnCopyAll = findViewById(R.id.btnCopyAll);
        cbEnableFilter = findViewById(R.id.cbEnableFilter);
        cbOnlyApi = findViewById(R.id.cbOnlyApi);
        cbEnableJsHook = findViewById(R.id.cbEnableJsHook);
        cbOnlyGet = findViewById(R.id.cbOnlyGet);
        cbOnlyPost = findViewById(R.id.cbOnlyPost);
        etSearchUrl = findViewById(R.id.etSearchUrl);
        webView = findViewById(R.id.webView);
        recyclerLogs = findViewById(R.id.recyclerLogs);
    }

    private void setupRecycler() {
        logAdapter = new NetworkLogAdapter(this::showReplayableLogDialog);
        recyclerLogs.setLayoutManager(new LinearLayoutManager(this));
        recyclerLogs.setAdapter(logAdapter);
    }

    @SuppressLint("AddJavascriptInterface")
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.setWebChromeClient(new WebChromeClient());

        webView.addJavascriptInterface(
                new JsBridge(json -> runOnUiThread(() -> {
                    if (!filterOptions.isEnableJsHook()) return;
                    parseAndAddJsLog(json);
                })),
                "AndroidLogger"
        );

        webView.setWebViewClient(new TrackingWebViewClient(
                () -> filterOptions,
                log -> runOnUiThread(() -> addLogIfNeeded(log))
        ));
    }

    private void setupListeners() {
        btnLoad.setOnClickListener(v -> {
            String inputUrl = etUrl.getText().toString().trim();
            if (!inputUrl.isEmpty()) {
                hideKeyboard(etUrl);
                etUrl.clearFocus();
                webView.requestFocus();

                String finalUrl = normalizeUrl(inputUrl);

                seenRequests.clear();
                allLogs.clear();
                logAdapter.submitList(new ArrayList<>());

                webView.loadUrl(finalUrl);
            }
        });

        btnCopyAll.setOnClickListener(v -> copyAllLogsToClipboard());

        etUrl.setOnEditorActionListener((v, actionId, event) -> {
            btnLoad.performClick();
            return true;
        });

        etSearchUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOptions.setSearchQuery(s != null ? s.toString() : "");
                refreshRecyclerByState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        cbEnableFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterOptions.setEnableFilter(isChecked);
            refreshRecyclerByState();
        });

        cbOnlyApi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterOptions.setOnlyApiRequests(isChecked);
            refreshRecyclerByState();
        });

        cbEnableJsHook.setOnCheckedChangeListener((buttonView, isChecked) ->
                filterOptions.setEnableJsHook(isChecked)
        );

        cbOnlyGet.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbOnlyPost.setChecked(false);
                filterOptions.setShowOnlyGet(true);
                filterOptions.setShowOnlyPost(false);
            } else {
                filterOptions.setShowOnlyGet(false);
            }
            refreshRecyclerByState();
        });

        cbOnlyPost.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbOnlyGet.setChecked(false);
                filterOptions.setShowOnlyPost(true);
                filterOptions.setShowOnlyGet(false);
            } else {
                filterOptions.setShowOnlyPost(false);
            }
            refreshRecyclerByState();
        });
    }

    private void setDefaults() {
        etUrl.setText("https://example.com");
        cbEnableFilter.setChecked(true);
        cbOnlyApi.setChecked(false);
        cbEnableJsHook.setChecked(true);
        cbOnlyGet.setChecked(false);
        cbOnlyPost.setChecked(false);
    }

    private String normalizeUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        return "https://" + url;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addLogIfNeeded(NetworkLog log) {
        String key = log.getSource() + "_" + log.getMethod() + "_" + log.getUrl() + "_"
                + log.getRequestBody() + "_" + log.getTime();

        if (!seenRequests.contains(key)) {
            seenRequests.add(key);
            allLogs.add(0, log);
            refreshRecyclerByState();
        }
    }

    private void refreshRecyclerByState() {
        List<NetworkLog> filtered = applyFilters(allLogs);
        logAdapter.submitList(new ArrayList<>(filtered));

        if (!filtered.isEmpty()) {
            recyclerLogs.scrollToPosition(0);
        }
    }

    private List<NetworkLog> applyFilters(List<NetworkLog> sourceLogs) {
        List<NetworkLog> result = new ArrayList<>();

        for (NetworkLog log : sourceLogs) {
            boolean searchOk = filterOptions.getSearchQuery().trim().isEmpty()
                    || (log.getUrl() != null
                    && log.getUrl().toLowerCase().contains(filterOptions.getSearchQuery().toLowerCase()));

            if (!filterOptions.isEnableFilter()) {
                if (searchOk) result.add(log);
                continue;
            }

            boolean methodOk;
            if (filterOptions.isShowOnlyGet()) {
                methodOk = "GET".equalsIgnoreCase(log.getMethod());
            } else if (filterOptions.isShowOnlyPost()) {
                methodOk = "POST".equalsIgnoreCase(log.getMethod());
            } else {
                methodOk = "GET".equalsIgnoreCase(log.getMethod())
                        || "POST".equalsIgnoreCase(log.getMethod());
            }

            boolean ignoredOk = !RequestUtils.shouldIgnoreUrl(log.getUrl());

            boolean apiOk = true;
            if (filterOptions.isOnlyApiRequests()) {
                apiOk = RequestUtils.looksLikeApi(log.getUrl())
                        || "api".equalsIgnoreCase(log.getResourceType())
                        || "JS_HOOK".equalsIgnoreCase(log.getSource());
            }

            if (searchOk && methodOk && ignoredOk && apiOk) {
                result.add(log);
            }
        }

        return result;
    }

    private void parseAndAddJsLog(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            String url = obj.optString("url", "");
            String method = obj.optString("method", "GET");
            String body = obj.isNull("body") ? null : obj.optString("body", null);
            String source = obj.optString("source", "JS_HOOK");

            String host;
            try {
                host = Uri.parse(url).getHost();
                if (host == null) host = "Bilinmiyor";
            } catch (Exception e) {
                host = "Bilinmiyor";
            }

            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            NetworkLog log = new NetworkLog(
                    method,
                    url,
                    host,
                    time,
                    null,
                    false,
                    "api",
                    body,
                    source
            );

            addLogIfNeeded(log);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLogDetailDialog(NetworkLog log) {
        View view = getLayoutInflater().inflate(R.layout.dialog_network_log_detail, null);

        TextView tvDetailMethod = view.findViewById(R.id.tvDetailMethod);
        TextView tvDetailSource = view.findViewById(R.id.tvDetailSource);
        TextView tvDetailType = view.findViewById(R.id.tvDetailType);
        TextView tvDetailTime = view.findViewById(R.id.tvDetailTime);
        TextView tvDetailHost = view.findViewById(R.id.tvDetailHost);
        TextView tvDetailMainFrame = view.findViewById(R.id.tvDetailMainFrame);
        TextView tvDetailUrl = view.findViewById(R.id.tvDetailUrl);
        TextView tvDetailHeaders = view.findViewById(R.id.tvDetailHeaders);
        TextView tvDetailBody = view.findViewById(R.id.tvDetailBody);

        tvDetailMethod.setText("Method: " + log.getMethod());
        tvDetailSource.setText("Kaynak: " + log.getSource());
        tvDetailType.setText("Tip: " + log.getResourceType());
        tvDetailTime.setText("Zaman: " + log.getTime());
        tvDetailHost.setText("Host: " + log.getHost());
        tvDetailMainFrame.setText("Main Frame: " + (log.isMainFrame() ? "Evet" : "Hayır"));
        tvDetailUrl.setText(log.getUrl());
        tvDetailHeaders.setText(RequestUtils.formatHeaders(log.getHeaders()));
        tvDetailBody.setText(log.getRequestBody() != null ? log.getRequestBody() : "Body yok");

        new AlertDialog.Builder(this)
                .setTitle("İstek Detayı")
                .setView(view)
                .setPositiveButton("Kapat", null)
                .setNeutralButton("Kopyala", (dialog, which) -> copySingleLogToClipboard(log))
                .show();
    }

    private void showReplayableLogDialog(NetworkLog log) {
        View view = getLayoutInflater().inflate(R.layout.dialog_replay_request, null);

        TextView tvReplayMethod = view.findViewById(R.id.tvReplayMethod);
        EditText etReplayBaseUrl = view.findViewById(R.id.etReplayBaseUrl);
        EditText etReplayParams = view.findViewById(R.id.etReplayParams);
        CheckBox cbOpenInWebViewAfterReplay =
                view.findViewById(R.id.cbOpenInWebViewAfterReplay);

        tvReplayMethod.setText("Method: " + log.getMethod());

        String initialBaseUrl;
        String initialParams;

        if ("GET".equalsIgnoreCase(log.getMethod())) {
            String[] parts = RequestUtils.splitUrlAndQuery(log.getUrl());
            initialBaseUrl = parts[0];
            initialParams = parts[1];
        } else {
            initialBaseUrl = log.getUrl();
            initialParams = log.getRequestBody() != null ? log.getRequestBody().trim() : "";
        }

        etReplayBaseUrl.setText(initialBaseUrl);
        etReplayParams.setText(initialParams);

        new AlertDialog.Builder(this)
                .setTitle("İsteği İncele / Tekrar Dene")
                .setView(view)
                .setPositiveButton("Tekrar Dene", (dialog, which) -> {
                    String editedBaseUrl = etReplayBaseUrl.getText().toString().trim();
                    String editedParams = etReplayParams.getText().toString();
                    boolean openInWebView = cbOpenInWebViewAfterReplay.isChecked();

                    replayRequest(log, editedBaseUrl, editedParams, openInWebView);
                })
                .setNeutralButton("Detay", (dialog, which) -> showLogDetailDialog(log))
                .setNegativeButton("Kapat", null)
                .show();
    }

    private void replayRequest(NetworkLog originalLog,
                               String editedBaseUrl,
                               String editedParams,
                               boolean openInWebView) {
        if (editedBaseUrl.isEmpty()) {
            Toast.makeText(this, "Base URL boş olamaz", Toast.LENGTH_SHORT).show();
            return;
        }

        if (openInWebView) {
            openReplayInWebView(originalLog, editedBaseUrl, editedParams);
        }

        new Thread(() -> {
            try {
                Request request;

                if ("GET".equalsIgnoreCase(originalLog.getMethod())) {
                    String finalUrl = RequestUtils.buildFinalUrl(editedBaseUrl, editedParams);
                    request = new Request.Builder()
                            .url(finalUrl)
                            .get()
                            .build();
                } else {
                    String mediaTypeStr = RequestUtils.detectMediaType(editedParams);
                    MediaType mediaType = MediaType.parse(mediaTypeStr);
                    RequestBody requestBody = RequestBody.create(editedParams, mediaType);

                    request = new Request.Builder()
                            .url(editedBaseUrl)
                            .post(requestBody)
                            .build();
                }

                Response response = okHttpClient.newCall(request).execute();
                int responseCode = response.code();
                Map<String, List<String>> responseHeaders = response.headers().toMultimap();
                String responseBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> showReplayResponseDialog(
                        buildReplayRequestSummary(originalLog, editedBaseUrl, editedParams),
                        responseCode,
                        formatResponseHeaders(responseHeaders),
                        responseBody
                ));

            } catch (IOException e) {
                runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Replay Hatası")
                        .setMessage(e.getMessage() != null ? e.getMessage() : "Bilinmeyen hata")
                        .setPositiveButton("Kapat", null)
                        .show());
            }
        }).start();
    }

    private void openReplayInWebView(NetworkLog originalLog,
                                     String editedBaseUrl,
                                     String editedParams) {
        runOnUiThread(() -> {
            try {
                if ("GET".equalsIgnoreCase(originalLog.getMethod())) {
                    String finalUrl = RequestUtils.buildFinalUrl(editedBaseUrl, editedParams);

                    webView.stopLoading();
                    webView.clearHistory();
                    etUrl.setText(finalUrl);
                    webView.loadUrl(finalUrl);

                    Toast.makeText(this, "Yeni GET URL açılıyor", Toast.LENGTH_SHORT).show();

                } else if ("POST".equalsIgnoreCase(originalLog.getMethod())) {
                    String mediaType = RequestUtils.detectMediaType(editedParams);

                    if (mediaType.startsWith("application/x-www-form-urlencoded")) {
                        webView.stopLoading();
                        etUrl.setText(editedBaseUrl);
                        webView.postUrl(editedBaseUrl, editedParams.getBytes());

                        Toast.makeText(this, "POST isteği WebView içinde gönderiliyor",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(
                                this,
                                "POST body JSON/plain ise WebView'de birebir açmak uygun olmayabilir.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this,
                        "WebView'de açılırken hata oluştu: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildReplayRequestSummary(NetworkLog originalLog,
                                             String editedBaseUrl,
                                             String editedParams) {
        StringBuilder sb = new StringBuilder();
        sb.append("ORIGINAL METHOD: ").append(originalLog.getMethod()).append("\n");
        sb.append("SOURCE         : ").append(originalLog.getSource()).append("\n");
        sb.append("EDITED BASE URL: ").append(editedBaseUrl).append("\n");
        sb.append("EDITED PARAMS  :\n");
        sb.append(editedParams.trim().isEmpty() ? "yok" : editedParams);
        return sb.toString();
    }

    private String formatResponseHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) return "Header yok";

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(join(entry.getValue()))
                    .append("\n");
        }
        return sb.toString().trim();
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            sb.append(values.get(i));
            if (i < values.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private void showReplayResponseDialog(String requestInfo,
                                          int code,
                                          String headersText,
                                          String body) {
        StringBuilder message = new StringBuilder();
        message.append("HTTP CODE: ").append(code).append("\n\n");
        message.append("REQUEST:\n").append(requestInfo).append("\n\n");
        message.append("RESPONSE HEADERS:\n").append(headersText).append("\n\n");
        message.append("RESPONSE BODY:\n");
        message.append(body.length() > 5000 ? body.substring(0, 5000) : body);

        new AlertDialog.Builder(this)
                .setTitle("Replay Sonucu")
                .setMessage(message.toString())
                .setPositiveButton("Tamam", null)
                .setNeutralButton("Kopyala", (dialog, which) -> {
                    copyTextToClipboard("replay_result", message.toString());
                    Toast.makeText(this, "Replay sonucu kopyalandı", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private String formatSingleLog(NetworkLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("METHOD      : ").append(log.getMethod()).append("\n");
        sb.append("SOURCE      : ").append(log.getSource()).append("\n");
        sb.append("TYPE        : ").append(log.getResourceType()).append("\n");
        sb.append("TIME        : ").append(log.getTime()).append("\n");
        sb.append("HOST        : ").append(log.getHost()).append("\n");
        sb.append("MAIN_FRAME  : ").append(log.isMainFrame()).append("\n");
        sb.append("URL         : ").append(log.getUrl()).append("\n");
        sb.append("HEADERS     :\n");
        if (log.getHeaders() == null || log.getHeaders().isEmpty()) {
            sb.append("  - yok\n");
        } else {
            for (Map.Entry<String, String> entry : log.getHeaders().entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        sb.append("BODY        :\n");
        sb.append(log.getRequestBody() != null ? log.getRequestBody() : "yok");
        return sb.toString();
    }

    private String buildAllLogsText() {
        if (allLogs.isEmpty()) {
            return "Henüz kopyalanacak istek yok.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("TOPLAM ISTEK SAYISI: ").append(allLogs.size()).append("\n\n");

        for (int i = 0; i < allLogs.size(); i++) {
            sb.append("ISTEK #").append(i + 1).append("\n");
            sb.append(formatSingleLog(allLogs.get(i))).append("\n\n");
        }

        return sb.toString();
    }

    private void copyAllLogsToClipboard() {
        copyTextToClipboard("tum_istekler", buildAllLogsText());
        Toast.makeText(this, "Tüm istekler clipboard'a kopyalandı", Toast.LENGTH_SHORT).show();
    }

    private void copySingleLogToClipboard(NetworkLog log) {
        copyTextToClipboard("istek_detayi", formatSingleLog(log));
        Toast.makeText(this, "İstek detayı kopyalandı", Toast.LENGTH_SHORT).show();
    }

    private void copyTextToClipboard(String label, String text) {
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
        }
    }

    @Deprecated
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        webView.removeJavascriptInterface("AndroidLogger");
        webView.destroy();
        super.onDestroy();
    }
}