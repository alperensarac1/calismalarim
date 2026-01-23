package com.example.csvexplorer.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.csvexplorer.entity.HeadersStore
import com.example.csvexplorer.entity.UiState
import com.example.csvexplorer.repo.CsvRepository
import com.example.csvexplorer.service.AppDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = CsvRepository(AppDB.get(app))

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private var lastPickedUri: Uri? = null

    fun init() {
        val headers = HeadersStore.load(getApplication())
        _state.update {
            it.copy(
                headers = headers,
                infoText = "${it.records.size} records",
                canUpload = lastPickedUri != null
            )
        }
        refreshAll()
    }

    fun onCsvPicked(uri: Uri, contentResolver: ContentResolver) {
        lastPickedUri = uri
        _state.update { it.copy(canUpload = true) }
        importCsv(uri, contentResolver)
    }

    fun refreshAll() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repo.getAll() }
            _state.update {
                it.copy(
                    records = list,
                    isLoading = false,
                    errorMessage = null,
                    downloadUrl = null,
                    infoText = "${list.size} records"
                )
            }
        }
    }

    fun setQuery(q: String) {
        _state.update { it.copy(query = q) }
    }

    fun setSelectedColumn(col: String) {
        _state.update { it.copy(selectedColumn = col) }
    }

    private fun jsonValue(rowJson: String, key: String): String {
        return try {
            org.json.JSONObject(rowJson).optString(key, "")
        } catch (_: Exception) {
            ""
        }
    }

    fun applyFilter() {
        viewModelScope.launch {
            val s = _state.value
            val list = withContext(Dispatchers.IO) {
                repo.filter(s.selectedColumn, s.query)
            }

            // ✅ seçili kolona göre sıralama
            val sorted = if (s.selectedColumn != "ALL_COLUMNS") {
                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { jsonValue(it.dataJson, s.selectedColumn) })
            } else {
                list
            }

            _state.update {
                it.copy(
                    records = sorted,
                    infoText = "${sorted.size} records (filter: ${s.selectedColumn})",
                    errorMessage = null
                )
            }
        }
    }

    private fun importCsv(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, downloadUrl = null, infoText = "Importing...") }
            try {
                val (headers, count) = withContext(Dispatchers.IO) { repo.importCsv(contentResolver, uri) }

                HeadersStore.save(getApplication(), headers)

                _state.update {
                    it.copy(
                        headers = headers,
                        isLoading = false,
                        infoText = "Imported: $count ${if (count == 1) "row" else "rows"}"
                    )
                }

                refreshAll()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Import error: ${e.message}",
                        infoText = "Import failed"
                    )
                }
            }
        }
    }
    fun clearDb() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, infoText = "Clearing database...") }
            try {
                withContext(Dispatchers.IO) { repo.clear() } // repo tarafı
                _state.update { it.copy(isLoading = false, query = "", selectedColumn = "ALL_COLUMNS", infoText = "Database cleared.") }
                refreshAll()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Clear DB error: ${e.message}", infoText = "Clear failed") }
            }
        }
    }

    fun upload(endpoint: String, contentResolver: ContentResolver) {
        val uri = lastPickedUri ?: run {
            _state.update { it.copy(errorMessage = "Please select a CSV file first.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, infoText = "Uploading...") }

            val res = withContext(Dispatchers.IO) { repo.uploadCsv(contentResolver, uri, endpoint) }

            if (!res.ok) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Upload failed: ${res.error}",
                        infoText = "Upload failed"
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    downloadUrl = res.downloadUrl,
                    infoText = "Download as .xls: ${res.downloadUrl ?: "N/A"}"
                )
            }
        }
    }

    fun consumeDownloadUrl() {
        _state.update { it.copy(downloadUrl = null) }
    }

    fun clearFilter() {
        _state.update { it.copy(query = "", selectedColumn = "ALL_COLUMNS") }
        refreshAll()
    }
}
