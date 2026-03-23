package com.example.webtrafficviewerkotlin

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.webtrafficviewerkotlin.adapter.NetworkLogAdapter
import com.example.webtrafficviewerkotlin.model.FilterOptions
import com.example.webtrafficviewerkotlin.model.NetworkLog
import com.example.webtrafficviewerkotlin.util.JsBridge
import com.example.webtrafficviewerkotlin.web.TrackingWebViewClient
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var etUrl: EditText
    private lateinit var btnLoad: Button
    private lateinit var btnCopyAll: Button
    private lateinit var cbEnableFilter: CheckBox
    private lateinit var cbOnlyApi: CheckBox
    private lateinit var cbEnableJsHook: CheckBox
    private lateinit var cbOnlyGet: CheckBox
    private lateinit var cbOnlyPost: CheckBox
    private lateinit var webView: WebView
    private lateinit var recyclerLogs: RecyclerView
    private lateinit var etSearchUrl: EditText
    private lateinit var logAdapter: NetworkLogAdapter

    private val allLogs = mutableListOf<NetworkLog>()

    private val seenRequests = HashSet<String>()

    private val okHttpClient by lazy { OkHttpClient() }

    private var searchQuery: String = ""
    private data class UiFilterState(
        val enableFilter: Boolean = true,
        val onlyApiRequests: Boolean = false,
        val enableJsHook: Boolean = true,
        val showOnlyGet: Boolean = false,
        val showOnlyPost: Boolean = false
    )

    private var filterState = UiFilterState()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUrl = findViewById(R.id.etUrl)
        btnLoad = findViewById(R.id.btnLoad)
        btnCopyAll = findViewById(R.id.btnCopyAll)
        cbEnableFilter = findViewById(R.id.cbEnableFilter)
        cbOnlyApi = findViewById(R.id.cbOnlyApi)
        cbEnableJsHook = findViewById(R.id.cbEnableJsHook)
        cbOnlyGet = findViewById(R.id.cbOnlyGet)
        cbOnlyPost = findViewById(R.id.cbOnlyPost)
        webView = findViewById(R.id.webView)
        recyclerLogs = findViewById(R.id.recyclerLogs)
        etSearchUrl = findViewById(R.id.etSearchUrl)
        logAdapter = NetworkLogAdapter { log ->
            showReplayableLogDialog(log)
        }

        recyclerLogs.layoutManager = LinearLayoutManager(this)
        recyclerLogs.adapter = logAdapter

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.webChromeClient = WebChromeClient()
        webView.isFocusable = true
        webView.isFocusableInTouchMode = true

        etUrl.isFocusable = true
        etUrl.isFocusableInTouchMode = true

        // JS tarafından yakalanan fetch/xhr loglarını alma
        webView.addJavascriptInterface(
            JsBridge { json ->
                if (!filterState.enableJsHook) return@JsBridge

                runOnUiThread {
                    parseAndAddJsLog(json)
                }
            },
            "AndroidLogger"
        )

        webView.webViewClient = TrackingWebViewClient(
            getFilterOptions = {
                FilterOptions(
                    enableFilter = false,
                    onlyApiRequests = false
                )
            },
            onRequestCaptured = { log ->
                runOnUiThread {
                    addLogIfNeeded(log)
                }
            }
        )

        btnLoad.setOnClickListener {
            val inputUrl = etUrl.text.toString().trim()

            if (inputUrl.isNotEmpty()) {
                hideKeyboard(etUrl)
                etUrl.clearFocus()
                webView.requestFocus()

                val finalUrl = normalizeUrl(inputUrl)

                seenRequests.clear()
                allLogs.clear()
                logAdapter.submitList(emptyList())

                webView.loadUrl(finalUrl)
            }
        }
        etSearchUrl.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString().orEmpty()
                refreshRecyclerByState()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        etUrl.setOnEditorActionListener { _, _, _ ->
            btnLoad.performClick()
            true
        }

        btnCopyAll.setOnClickListener {
            copyAllLogsToClipboard()
        }

        etUrl.setText("https://example.com")
        cbEnableFilter.isChecked = true
        cbOnlyApi.isChecked = false
        cbEnableJsHook.isChecked = true
        cbOnlyGet.isChecked = false
        cbOnlyPost.isChecked = false

        filterState = UiFilterState(
            enableFilter = cbEnableFilter.isChecked,
            onlyApiRequests = cbOnlyApi.isChecked,
            enableJsHook = cbEnableJsHook.isChecked,
            showOnlyGet = cbOnlyGet.isChecked,
            showOnlyPost = cbOnlyPost.isChecked
        )

        setupFilterListeners()
    }

    private fun setupFilterListeners() {
        cbEnableFilter.setOnCheckedChangeListener { _, isChecked ->
            filterState = filterState.copy(enableFilter = isChecked)
            refreshRecyclerByState()
        }

        cbOnlyApi.setOnCheckedChangeListener { _, isChecked ->
            filterState = filterState.copy(onlyApiRequests = isChecked)
            refreshRecyclerByState()
        }

        cbEnableJsHook.setOnCheckedChangeListener { _, isChecked ->
            filterState = filterState.copy(enableJsHook = isChecked)
        }

        cbOnlyGet.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && cbOnlyPost.isChecked) {
                cbOnlyPost.setOnCheckedChangeListener(null)
                cbOnlyPost.isChecked = false
                cbOnlyPost.setOnCheckedChangeListener { _, postChecked ->
                    filterState = if (postChecked) {
                        cbOnlyGet.setOnCheckedChangeListener(null)
                        cbOnlyGet.isChecked = false
                        cbOnlyGet.setOnCheckedChangeListener { _, getChecked ->
                            filterState = filterState.copy(showOnlyGet = getChecked, showOnlyPost = false)
                            refreshRecyclerByState()
                        }
                        filterState.copy(showOnlyPost = true, showOnlyGet = false)
                    } else {
                        filterState.copy(showOnlyPost = false)
                    }
                    refreshRecyclerByState()
                }
            }

            filterState = if (isChecked) {
                filterState.copy(showOnlyGet = true, showOnlyPost = false)
            } else {
                filterState.copy(showOnlyGet = false)
            }

            refreshRecyclerByState()
        }

        cbOnlyPost.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && cbOnlyGet.isChecked) {
                cbOnlyGet.setOnCheckedChangeListener(null)
                cbOnlyGet.isChecked = false
                cbOnlyGet.setOnCheckedChangeListener { _, getChecked ->
                    if (getChecked && cbOnlyPost.isChecked) {
                        cbOnlyPost.setOnCheckedChangeListener(null)
                        cbOnlyPost.isChecked = false
                        cbOnlyPost.setOnCheckedChangeListener { _, postChecked ->
                            filterState = if (postChecked) {
                                cbOnlyGet.setOnCheckedChangeListener(null)
                                cbOnlyGet.isChecked = false
                                cbOnlyGet.setOnCheckedChangeListener { _, againGetChecked ->
                                    filterState = filterState.copy(showOnlyGet = againGetChecked, showOnlyPost = false)
                                    refreshRecyclerByState()
                                }
                                filterState.copy(showOnlyPost = true, showOnlyGet = false)
                            } else {
                                filterState.copy(showOnlyPost = false)
                            }
                            refreshRecyclerByState()
                        }
                    }

                    filterState = if (getChecked) {
                        filterState.copy(showOnlyGet = true, showOnlyPost = false)
                    } else {
                        filterState.copy(showOnlyGet = false)
                    }

                    refreshRecyclerByState()
                }
            }

            filterState = if (isChecked) {
                filterState.copy(showOnlyPost = true, showOnlyGet = false)
            } else {
                filterState.copy(showOnlyPost = false)
            }

            refreshRecyclerByState()
        }
    }

    private fun normalizeUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun addLogIfNeeded(log: NetworkLog) {
        val key = "${log.source}_${log.method}_${log.url}_${log.requestBody}_${log.time}"

        if (!seenRequests.contains(key)) {
            seenRequests.add(key)
            allLogs.add(0, log)
            refreshRecyclerByState()
        }
    }

    private fun refreshRecyclerByState() {
        val filtered = applyFilters(allLogs)
        logAdapter.submitList(filtered.toList())

        if (filtered.isNotEmpty()) {
            recyclerLogs.scrollToPosition(0)
        }
    }

    private fun applyFilters(sourceLogs: List<NetworkLog>): List<NetworkLog> {
        return sourceLogs.filter { log ->

            // URL arama filtresi
            val searchOk = if (searchQuery.isBlank()) {
                true
            } else {
                log.url.contains(searchQuery, ignoreCase = true)
            }

            // Eğer genel filtre kapalıysa sadece arama filtresi çalışsın
            if (!filterState.enableFilter) {
                return@filter searchOk
            }

            val methodOk = when {
                filterState.showOnlyGet -> log.method.equals("GET", ignoreCase = true)
                filterState.showOnlyPost -> log.method.equals("POST", ignoreCase = true)
                else -> log.method.equals("GET", ignoreCase = true) ||
                        log.method.equals("POST", ignoreCase = true)
            }

            val ignoredOk = !shouldIgnoreUrl(log.url)

            val apiOk = if (filterState.onlyApiRequests) {
                looksLikeApi(log.url) ||
                        log.resourceType.equals("api", ignoreCase = true) ||
                        log.source.equals("JS_HOOK", ignoreCase = true)
            } else {
                true
            }

            searchOk && methodOk && ignoredOk && apiOk
        }
    }

    // JS hook'tan gelen json logunu modele çevir
    private fun parseAndAddJsLog(json: String) {
        try {
            val obj = JSONObject(json)

            val url = obj.optString("url", "")
            val method = obj.optString("method", "GET")
            val body = if (obj.isNull("body")) null else obj.optString("body", null)
            val source = obj.optString("source", "JS_HOOK")

            val host = try {
                Uri.parse(url).host ?: "Bilinmiyor"
            } catch (e: Exception) {
                "Bilinmiyor"
            }

            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            val log = NetworkLog(
                method = method,
                url = url,
                host = host,
                time = time,
                headers = emptyMap(),
                isMainFrame = false,
                resourceType = "api",
                requestBody = body,
                source = source
            )

            addLogIfNeeded(log)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // İstek detayı dialogu
    private fun showLogDetailDialog(log: NetworkLog) {
        val view = layoutInflater.inflate(R.layout.dialog_network_log_detail, null)

        val tvDetailMethod = view.findViewById<TextView>(R.id.tvDetailMethod)
        val tvDetailSource = view.findViewById<TextView>(R.id.tvDetailSource)
        val tvDetailType = view.findViewById<TextView>(R.id.tvDetailType)
        val tvDetailTime = view.findViewById<TextView>(R.id.tvDetailTime)
        val tvDetailHost = view.findViewById<TextView>(R.id.tvDetailHost)
        val tvDetailMainFrame = view.findViewById<TextView>(R.id.tvDetailMainFrame)
        val tvDetailUrl = view.findViewById<TextView>(R.id.tvDetailUrl)
        val tvDetailHeaders = view.findViewById<TextView>(R.id.tvDetailHeaders)
        val tvDetailBody = view.findViewById<TextView>(R.id.tvDetailBody)

        tvDetailMethod.text = "Method: ${log.method}"
        tvDetailSource.text = "Kaynak: ${log.source}"
        tvDetailType.text = "Tip: ${log.resourceType}"
        tvDetailTime.text = "Zaman: ${log.time}"
        tvDetailHost.text = "Host: ${log.host}"
        tvDetailMainFrame.text = "Main Frame: ${if (log.isMainFrame) "Evet" else "Hayır"}"
        tvDetailUrl.text = log.url
        tvDetailHeaders.text = formatHeaders(log.headers)
        tvDetailBody.text = log.requestBody ?: "Body yok"

        AlertDialog.Builder(this)
            .setTitle("İstek Detayı")
            .setView(view)
            .setPositiveButton("Kapat", null)
            .setNeutralButton("Kopyala") { _, _ ->
                copySingleLogToClipboard(log)
            }
            .show()
    }

    private fun showReplayableLogDialog(log: NetworkLog) {
        val view = layoutInflater.inflate(R.layout.dialog_replay_request, null)

        val tvReplayMethod = view.findViewById<TextView>(R.id.tvReplayMethod)
        val etReplayBaseUrl = view.findViewById<EditText>(R.id.etReplayBaseUrl)
        val etReplayParams = view.findViewById<EditText>(R.id.etReplayParams)
        val cbOpenInWebViewAfterReplay =
            view.findViewById<CheckBox>(R.id.cbOpenInWebViewAfterReplay)

        tvReplayMethod.text = "Method: ${log.method}"

        val initialBaseUrl: String
        val initialParams: String

        if (log.method.equals("GET", ignoreCase = true)) {
            val parts = splitUrlAndQuery(log.url)
            initialBaseUrl = parts.first
            initialParams = parts.second
        } else {
            initialBaseUrl = log.url
            initialParams = log.requestBody?.trim().orEmpty()
        }

        etReplayBaseUrl.setText(initialBaseUrl)
        etReplayParams.setText(initialParams)

        AlertDialog.Builder(this)
            .setTitle("İsteği İncele / Tekrar Dene")
            .setView(view)
            .setPositiveButton("Tekrar Dene") { _, _ ->
                val editedBaseUrl = etReplayBaseUrl.text.toString().trim()
                val editedParams = etReplayParams.text.toString()
                val openInWebView = cbOpenInWebViewAfterReplay.isChecked

                replayRequest(
                    originalLog = log,
                    editedBaseUrl = editedBaseUrl,
                    editedParams = editedParams,
                    openInWebView = openInWebView
                )
            }
            .setNeutralButton("Detay") { _, _ ->
                showLogDetailDialog(log)
            }
            .setNegativeButton("Kapat", null)
            .show()
    }

    // GET için url ve query ayırma
    private fun splitUrlAndQuery(fullUrl: String): Pair<String, String> {
        val index = fullUrl.indexOf("?")
        return if (index == -1) {
            Pair(fullUrl, "")
        } else {
            Pair(
                fullUrl.substring(0, index),
                fullUrl.substring(index + 1)
            )
        }
    }

    private fun buildFinalUrl(baseUrl: String, query: String): String {
        if (query.isBlank()) return baseUrl

        val normalizedBase = baseUrl.trim()

        // Query zaten key=value&key2=value2 gibiyse olduğu gibi ekle
        // Ama boşluk gibi karakterler varsa encode etmeye çalış
        val encodedQuery = query
            .split("&")
            .joinToString("&") { part ->
                val eqIndex = part.indexOf("=")
                if (eqIndex == -1) {
                    java.net.URLEncoder.encode(part, "UTF-8")
                } else {
                    val key = part.substring(0, eqIndex)
                    val value = part.substring(eqIndex + 1)
                    key + "=" + java.net.URLEncoder.encode(value, "UTF-8")
                }
            }

        return if (normalizedBase.contains("?")) {
            "$normalizedBase&$encodedQuery"
        } else {
            "$normalizedBase?$encodedQuery"
        }
    }

    private fun replayRequest(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String,
        openInWebView: Boolean
    ) {
        if (editedBaseUrl.isBlank()) {
            Toast.makeText(this, "Base URL boş olamaz", Toast.LENGTH_SHORT).show()
            return
        }

        // Önce istenirse WebView tarafını güncelle
        if (openInWebView) {
            openReplayInWebView(
                originalLog = originalLog,
                editedBaseUrl = editedBaseUrl,
                editedParams = editedParams
            )
        }

        // Sonra OkHttp ile isteği ayrıca test et
        thread {
            try {
                val requestBuilder = Request.Builder()

                val request = if (originalLog.method.equals("GET", ignoreCase = true)) {
                    val finalUrl = buildFinalUrl(editedBaseUrl, editedParams)
                    requestBuilder
                        .url(finalUrl)
                        .get()
                        .build()
                } else {
                    val mediaType = detectMediaType(editedParams)
                    val requestBody = editedParams.toRequestBody(mediaType.toMediaTypeOrNull())

                    requestBuilder
                        .url(editedBaseUrl)
                        .post(requestBody)
                        .build()
                }

                val response = okHttpClient.newCall(request).execute()
                val responseCode = response.code
                val responseHeaders = response.headers.toMultimap()
                val responseBody = response.body?.string().orEmpty()

                runOnUiThread {
                    showReplayResponseDialog(
                        requestInfo = buildReplayRequestSummary(
                            originalLog = originalLog,
                            editedBaseUrl = editedBaseUrl,
                            editedParams = editedParams
                        ),
                        code = responseCode,
                        headersText = formatResponseHeaders(responseHeaders),
                        body = responseBody
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Replay Hatası")
                        .setMessage(e.message ?: "Bilinmeyen hata")
                        .setPositiveButton("Kapat", null)
                        .show()
                }
            }
        }
    }

    private fun detectMediaType(bodyText: String): String {
        val trimmed = bodyText.trim()

        return when {
            trimmed.startsWith("{") || trimmed.startsWith("[") ->
                "application/json; charset=utf-8"

            trimmed.contains("=") && trimmed.contains("&") ->
                "application/x-www-form-urlencoded; charset=utf-8"

            trimmed.contains("=") ->
                "application/x-www-form-urlencoded; charset=utf-8"

            else ->
                "text/plain; charset=utf-8"
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

    private fun showReplayResponseDialog(
        requestInfo: String,
        code: Int,
        headersText: String,
        body: String
    ) {
        val message = buildString {
            appendLine("HTTP CODE: $code")
            appendLine()
            appendLine("REQUEST:")
            appendLine(requestInfo)
            appendLine()
            appendLine("RESPONSE HEADERS:")
            appendLine(headersText)
            appendLine()
            appendLine("RESPONSE BODY:")
            appendLine(body.take(5000))
        }

        AlertDialog.Builder(this)
            .setTitle("Replay Sonucu")
            .setMessage(message)
            .setPositiveButton("Tamam", null)
            .setNeutralButton("Kopyala") { _, _ ->
                copyTextToClipboard("replay_result", message)
                Toast.makeText(this, "Replay sonucu kopyalandı", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
    private fun openReplayInWebView(
        originalLog: NetworkLog,
        editedBaseUrl: String,
        editedParams: String
    ) {
        runOnUiThread {
            try {
                if (originalLog.method.equals("GET", ignoreCase = true)) {
                    val finalUrl = buildFinalUrl(editedBaseUrl, editedParams)

                    webView.stopLoading()
                    webView.clearHistory()
                    etUrl.setText(finalUrl)
                    webView.loadUrl(finalUrl)

                    Toast.makeText(
                        this,
                        "Yeni GET URL açılıyor",
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (originalLog.method.equals("POST", ignoreCase = true)) {
                    val mediaType = detectMediaType(editedParams)

                    if (mediaType.startsWith("application/x-www-form-urlencoded")) {
                        webView.stopLoading()
                        etUrl.setText(editedBaseUrl)

                        val postBytes = editedParams.toByteArray(Charsets.UTF_8)
                        webView.postUrl(editedBaseUrl, postBytes)

                        Toast.makeText(
                            this,
                            "POST isteği WebView içinde gönderiliyor",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Toast.makeText(
                            this,
                            "POST body JSON/plain ise WebView içinde birebir açmak uygun olmayabilir. HTTP replay sonucu gösterildi.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "WebView'de açılırken hata oluştu: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun shouldIgnoreUrl(url: String): Boolean {
        val lower = url.lowercase()

        val ignoredExtensions = listOf(
            ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg",
            ".css", ".js", ".map",
            ".woff", ".woff2", ".ttf", ".otf",
            ".ico", ".mp4", ".webm", ".mp3", ".aac", ".m4a"
        )

        return ignoredExtensions.any { lower.contains(it) }
    }

    private fun looksLikeApi(url: String): Boolean {
        val lower = url.lowercase()

        val apiKeywords = listOf(
            "/api/",
            "graphql",
            ".json",
            "ajax",
            "rest",
            "v1/",
            "v2/",
            "endpoint"
        )

        return apiKeywords.any { lower.contains(it) }
    }

    private fun formatHeaders(headers: Map<String, String>): String {
        if (headers.isEmpty()) return "Header yok"

        val sb = StringBuilder()
        headers.forEach { (key, value) ->
            sb.append(key).append(": ").append(value).append("\n")
        }
        return sb.toString().trim()
    }

    private fun formatSingleLog(log: NetworkLog): String {
        return buildString {
            appendLine("========================================")
            appendLine("METHOD      : ${log.method}")
            appendLine("SOURCE      : ${log.source}")
            appendLine("TYPE        : ${log.resourceType}")
            appendLine("TIME        : ${log.time}")
            appendLine("HOST        : ${log.host}")
            appendLine("MAIN_FRAME  : ${log.isMainFrame}")
            appendLine("URL         : ${log.url}")
            appendLine("HEADERS     :")
            if (log.headers.isEmpty()) {
                appendLine("  - yok")
            } else {
                log.headers.forEach { (key, value) ->
                    appendLine("  $key: $value")
                }
            }
            appendLine("BODY        :")
            appendLine(log.requestBody ?: "yok")
        }
    }

    private fun buildAllLogsText(): String {
        val logs = allLogs.toList()

        if (logs.isEmpty()) {
            return "Henüz kopyalanacak istek yok."
        }

        return buildString {
            appendLine("TOPLAM ISTEK SAYISI: ${logs.size}")
            appendLine()

            logs.forEachIndexed { index, log ->
                appendLine("ISTEK #${index + 1}")
                appendLine(formatSingleLog(log))
                appendLine()
            }
        }
    }

    private fun copyAllLogsToClipboard() {
        val text = buildAllLogsText()
        copyTextToClipboard("tum_istekler", text)
        Toast.makeText(this, "Tüm istekler clipboard'a kopyalandı", Toast.LENGTH_SHORT).show()
    }

    private fun copySingleLogToClipboard(log: NetworkLog) {
        val text = formatSingleLog(log)
        copyTextToClipboard("istek_detayi", text)
        Toast.makeText(this, "İstek detayı kopyalandı", Toast.LENGTH_SHORT).show()
    }

    private fun copyTextToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface("AndroidLogger")
        webView.destroy()
        super.onDestroy()
    }
}