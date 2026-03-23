package com.example.webtrafficviewerjetpack.viewmodel


import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.webtrafficviewerjetpack.model.FilterOptions
import com.example.webtrafficviewerjetpack.model.NetworkLog
import com.example.webtrafficviewerjetpack.util.RequestUtils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    private val allLogs = mutableListOf<NetworkLog>()
    private val seenRequests = hashSetOf<String>()

    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    private val _visibleLogs = MutableStateFlow<List<NetworkLog>>(emptyList())
    val visibleLogs: StateFlow<List<NetworkLog>> = _visibleLogs.asStateFlow()

    private val _urlText = MutableStateFlow("https://example.com")
    val urlText: StateFlow<String> = _urlText.asStateFlow()

    fun updateUrlText(value: String) {
        _urlText.value = value
    }

    fun clearLogs() {
        allLogs.clear()
        seenRequests.clear()
        refreshVisibleLogs()
    }

    fun addLogIfNeeded(log: NetworkLog) {
        val key = "${log.source}_${log.method}_${log.url}_${log.requestBody}_${log.time}"

        if (!seenRequests.contains(key)) {
            seenRequests.add(key)
            allLogs.add(0, log)
            refreshVisibleLogs()
        }
    }

    fun parseAndAddJsLog(json: String) {
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

    fun updateEnableFilter(value: Boolean) {
        _filterOptions.value = _filterOptions.value.copy(enableFilter = value)
        refreshVisibleLogs()
    }

    fun updateOnlyApi(value: Boolean) {
        _filterOptions.value = _filterOptions.value.copy(onlyApiRequests = value)
        refreshVisibleLogs()
    }

    fun updateEnableJsHook(value: Boolean) {
        _filterOptions.value = _filterOptions.value.copy(enableJsHook = value)
    }

    fun updateOnlyGet(value: Boolean) {
        _filterOptions.value = if (value) {
            _filterOptions.value.copy(showOnlyGet = true, showOnlyPost = false)
        } else {
            _filterOptions.value.copy(showOnlyGet = false)
        }
        refreshVisibleLogs()
    }

    fun updateOnlyPost(value: Boolean) {
        _filterOptions.value = if (value) {
            _filterOptions.value.copy(showOnlyPost = true, showOnlyGet = false)
        } else {
            _filterOptions.value.copy(showOnlyPost = false)
        }
        refreshVisibleLogs()
    }

    fun updateSearchQuery(value: String) {
        _filterOptions.value = _filterOptions.value.copy(searchQuery = value)
        refreshVisibleLogs()
    }

    fun getAllLogsText(): String {
        if (allLogs.isEmpty()) return "Henüz kopyalanacak istek yok."

        return buildString {
            appendLine("TOPLAM ISTEK SAYISI: ${allLogs.size}")
            appendLine()

            allLogs.forEachIndexed { index, log ->
                appendLine("ISTEK #${index + 1}")
                appendLine(formatSingleLog(log))
                appendLine()
            }
        }
    }

    fun formatSingleLog(log: NetworkLog): String {
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

    private fun refreshVisibleLogs() {
        val filters = _filterOptions.value

        val result = allLogs.filter { log ->
            val searchOk = if (filters.searchQuery.isBlank()) {
                true
            } else {
                log.url.contains(filters.searchQuery, ignoreCase = true)
            }

            if (!filters.enableFilter) {
                return@filter searchOk
            }

            val methodOk = when {
                filters.showOnlyGet -> log.method.equals("GET", ignoreCase = true)
                filters.showOnlyPost -> log.method.equals("POST", ignoreCase = true)
                else -> log.method.equals("GET", ignoreCase = true) ||
                        log.method.equals("POST", ignoreCase = true)
            }

            val ignoredOk = !RequestUtils.shouldIgnoreUrl(log.url)

            val apiOk = if (filters.onlyApiRequests) {
                RequestUtils.looksLikeApi(log.url) ||
                        log.resourceType.equals("api", ignoreCase = true) ||
                        log.source.equals("JS_HOOK", ignoreCase = true)
            } else {
                true
            }

            searchOk && methodOk && ignoredOk && apiOk
        }

        _visibleLogs.value = result
    }
}