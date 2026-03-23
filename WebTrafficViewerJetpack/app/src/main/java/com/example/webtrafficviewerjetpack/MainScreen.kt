package com.example.webtrafficviewerjetpack

import com.example.webtrafficviewerjetpack.model.NetworkLog
import com.example.webtrafficviewerjetpack.util.RequestUtils
import com.example.webtrafficviewerjetpack.viewmodel.MainViewModel


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onWebViewReady: (WebView) -> Unit,
    onLoadUrl: (String) -> Unit,
    onReplayRequest: (
        log: NetworkLog,
        baseUrl: String,
        params: String,
        openInWebView: Boolean,
        onResult: (String) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val visibleLogs by viewModel.visibleLogs.collectAsState()
    val filterOptions by viewModel.filterOptions.collectAsState()
    val urlText by viewModel.urlText.collectAsState()

    val urlState = remember(urlText) { mutableStateOf(urlText) }
    val selectedLog = remember { mutableStateOf<NetworkLog?>(null) }
    val replayLog = remember { mutableStateOf<NetworkLog?>(null) }
    val replayResult = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(urlText) {
        urlState.value = urlText
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = urlState.value,
            onValueChange = {
                urlState.value = it
                viewModel.updateUrlText(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL") }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onLoadUrl(urlState.value) }) {
                Text("Aç")
            }

            Button(onClick = {
                copyToClipboard(
                    context = context,
                    label = "tum_istekler",
                    text = viewModel.getAllLogsText()
                )
            }) {
                Text("Kopyala")
            }
        }

        FilterSection(
            enableFilter = filterOptions.enableFilter,
            onlyApi = filterOptions.onlyApiRequests,
            enableJsHook = filterOptions.enableJsHook,
            onlyGet = filterOptions.showOnlyGet,
            onlyPost = filterOptions.showOnlyPost,
            searchQuery = filterOptions.searchQuery,
            onEnableFilterChanged = viewModel::updateEnableFilter,
            onOnlyApiChanged = viewModel::updateOnlyApi,
            onEnableJsHookChanged = viewModel::updateEnableJsHook,
            onOnlyGetChanged = viewModel::updateOnlyGet,
            onOnlyPostChanged = viewModel::updateOnlyPost,
            onSearchChanged = viewModel::updateSearchQuery
        )

        AndroidView(
            factory = { ctx ->
                WebView(ctx).also(onWebViewReady)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        Text(
            text = "Yakalanan İstekler",
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(visibleLogs) { log ->
                NetworkLogItem(
                    log = log,
                    onClick = { selectedLog.value = log }
                )
                HorizontalDivider()
            }
        }
    }

    selectedLog.value?.let { log ->
        LogActionDialog(
            log = log,
            onDismiss = { selectedLog.value = null },
            onDetail = {
                selectedLog.value = null
                replayLog.value = null
                selectedLog.value = log
            },
            onReplay = {
                selectedLog.value = null
                replayLog.value = log
            },
            onCopy = {
                copyToClipboard(
                    context = context,
                    label = "istek_detayi",
                    text = viewModel.formatSingleLog(log)
                )
            }
        )
    }

    replayLog.value?.let { log ->
        ReplayDialog(
            log = log,
            onDismiss = { replayLog.value = null },
            onReplay = { baseUrl, params, openInWebView ->
                onReplayRequest(
                    log,
                    baseUrl,
                    params,
                    openInWebView
                ) { result ->
                    replayResult.value = result
                }
            }
        )
    }

    replayResult.value?.let { result ->
        AlertDialog(
            onDismissRequest = { replayResult.value = null },
            title = { Text("Replay Sonucu") },
            text = { Text(result) },
            confirmButton = {
                TextButton(onClick = { replayResult.value = null }) {
                    Text("Kapat")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        copyToClipboard(
                            context = context,
                            label = "replay_result",
                            text = result
                        )
                    }
                ) {
                    Text("Kopyala")
                }
            }
        )
    }

    // ayrı detay dialog
    val detailLog = remember { mutableStateOf<NetworkLog?>(null) }

    LaunchedEffect(selectedLog.value) {
        // boş
    }
}

@Composable
private fun FilterSection(
    enableFilter: Boolean,
    onlyApi: Boolean,
    enableJsHook: Boolean,
    onlyGet: Boolean,
    onlyPost: Boolean,
    searchQuery: String,
    onEnableFilterChanged: (Boolean) -> Unit,
    onOnlyApiChanged: (Boolean) -> Unit,
    onEnableJsHookChanged: (Boolean) -> Unit,
    onOnlyGetChanged: (Boolean) -> Unit,
    onOnlyPostChanged: (Boolean) -> Unit,
    onSearchChanged: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FilterRow("Filtre aktif", enableFilter, onEnableFilterChanged)
        FilterRow("Sadece API benzeri istekler", onlyApi, onOnlyApiChanged)
        FilterRow("JS Hook aktif", enableJsHook, onEnableJsHookChanged)
        FilterRow("Sadece GET", onlyGet, onOnlyGetChanged)
        FilterRow("Sadece POST", onlyPost, onOnlyPostChanged)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("URL içinde ara") }
        )
    }
}

@Composable
private fun FilterRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun NetworkLogItem(
    log: NetworkLog,
    onClick: () -> Unit
) {
    val preview = log.requestBody?.takeIf { it.isNotBlank() }?.take(150) ?: "yok"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text("Method: ${log.method} | Kaynak: ${log.source}")
        Text("Tip: ${log.resourceType}")
        Text("URL: ${log.url}")
        Text("Host: ${log.host}")
        Text("Zaman: ${log.time}")
        Text("Body: $preview")
    }
}

@Composable
private fun LogActionDialog(
    log: NetworkLog,
    onDismiss: () -> Unit,
    onDetail: () -> Unit,
    onReplay: () -> Unit,
    onCopy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("İstek İşlemleri") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Method: ${log.method}")
                Text("URL: ${log.url}")
            }
        },
        confirmButton = {
            TextButton(onClick = onReplay) {
                Text("Replay")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onCopy) {
                    Text("Kopyala")
                }
                TextButton(onClick = onDismiss) {
                    Text("Kapat")
                }
            }
        }
    )
}

@Composable
private fun ReplayDialog(
    log: NetworkLog,
    onDismiss: () -> Unit,
    onReplay: (baseUrl: String, params: String, openInWebView: Boolean) -> Unit
) {
    val (initialBaseUrl, initialParams) = if (log.method.equals("GET", ignoreCase = true)) {
        RequestUtils.splitUrlAndQuery(log.url)
    } else {
        log.url to (log.requestBody?.trim().orEmpty())
    }

    val baseUrlState = remember(log) { mutableStateOf(initialBaseUrl) }
    val paramsState = remember(log) { mutableStateOf(initialParams) }
    val openInWebViewState = remember(log) { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("İsteği İncele / Tekrar Dene") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Method: ${log.method}")

                OutlinedTextField(
                    value = baseUrlState.value,
                    onValueChange = { baseUrlState.value = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paramsState.value,
                    onValueChange = { paramsState.value = it },
                    label = { Text("Query / Body") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Replay sonrası WebView'de aç")
                    Checkbox(
                        checked = openInWebViewState.value,
                        onCheckedChange = { openInWebViewState.value = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onReplay(
                        baseUrlState.value.trim(),
                        paramsState.value,
                        openInWebViewState.value
                    )
                }
            ) {
                Text("Tekrar Dene")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

private fun copyToClipboard(
    context: Context,
    label: String,
    text: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}