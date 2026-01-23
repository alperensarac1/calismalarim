package com.example.csvexplorerjetpack.viewmodel



import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.csvexplorerjetpack.data.AppDb
import com.example.csvexplorerjetpack.data.CsvRepository

import com.example.csvexplorerjetpack.domain.UriFileUtil
import com.example.csvexplorerjetpack.service.UploadClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = CsvRepository(AppDb.get(app).rowDao())

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state
    private var lastPickedCsvUri: Uri? = null

    fun init() {
        refreshAll()

    }

    fun onCsvPicked(uri: Uri, cr: ContentResolver) {
        lastPickedCsvUri = uri

        _state.value = _state.value.copy(isLoading = true, errorMessage = null, infoText = "Importing...",canUpload = true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = repo.importCsv(cr, uri)
                val list = repo.getAll()
                _state.value = _state.value.copy(
                    isLoading = false,
                    headers = res.headers,
                    records = list,
                    infoText = "Imported: ${res.rows.size} rows",
                    canUpload = false,
                    downloadUrl = null

                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Import error: ${e.message}",
                    infoText = "Import failed"
                )
            }
        }
    }

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun setSelectedColumn(col: String) {
        _state.value = _state.value.copy(selectedColumn = col)
    }

    fun applyFilter() {
        val cur = _state.value
        _state.value = cur.copy(isLoading = true, errorMessage = null, infoText = "Filtering...")
        viewModelScope.launch(Dispatchers.IO) {
            val list = repo.filter(cur.selectedColumn, cur.query)

            val sorted = if (cur.selectedColumn != "ALL_COLUMNS") {
                list.sortedBy { jsonValue(it.dataJson, cur.selectedColumn).lowercase(Locale.getDefault()) }
            } else list

            _state.value = _state.value.copy(
                isLoading = false,
                records = sorted,
                infoText = "${sorted.size} records (filter: ${cur.selectedColumn})"
            )
        }
    }

    fun clearFilter() {
        _state.value = _state.value.copy(query = "", selectedColumn = "ALL_COLUMNS")
        refreshAll()
    }

    fun clearDb() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null, infoText = "Clearing database...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repo.clear()
                _state.value = _state.value.copy(
                    isLoading = false,
                    records = emptyList(),
                    headers = emptyList(),
                    query = "",
                    selectedColumn = "ALL_COLUMNS",
                    infoText = "Database cleared."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Clear error: ${e.message}")
            }
        }
    }

    private fun refreshAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repo.getAll()
            _state.value = _state.value.copy(
                isLoading = false,
                records = list,
                infoText = "${list.size} records"
            )
        }
    }

    private fun jsonValue(json: String, key: String): String =
        try { JSONObject(json).optString(key, "") } catch (_: Exception) { "" }
    fun uploadCsv(endpoint: String, cr: ContentResolver) {
        val uri = lastPickedCsvUri
        if (uri == null) {
            _state.value = _state.value.copy(errorMessage = "Önce CSV seçmelisin.")
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null, infoText = "Uploading...")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = UriFileUtil.copyToCacheFile(
                    cr = cr,
                    uri = uri,
                    cacheDir = getApplication<Application>().cacheDir
                )

                val url = UploadClient.uploadCsv(endpoint, file)

                _state.value = _state.value.copy(
                    isLoading = false,
                    downloadUrl = url,
                    infoText = "Upload done. Download ready."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Upload error: ${e.message}",
                    infoText = "Upload failed"
                )
            }
        }
    }

    fun consumeDownloadUrl() {
        _state.value = _state.value.copy(downloadUrl = null)
    }

}
