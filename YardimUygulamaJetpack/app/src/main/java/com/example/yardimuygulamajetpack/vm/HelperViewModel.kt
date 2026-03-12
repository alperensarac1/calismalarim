package com.example.yardimuygulamajetpack.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yardimuygulamajetpack.model.AcceptedHelpItem
import com.example.yardimuygulamajetpack.model.ConfirmedHelpItem
import com.example.yardimuygulamajetpack.model.OpenHelpItem
import com.example.yardimuygulamajetpack.repo.HelperRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HelperViewModel(
    private val repo: HelperRepo = HelperRepo()
) : ViewModel() {

    private val _open = MutableStateFlow<UiState<List<OpenHelpItem>>>(UiState.Idle)
    val open: StateFlow<UiState<List<OpenHelpItem>>> = _open

    private val _accepted = MutableStateFlow<UiState<AcceptedHelpItem?>>(UiState.Idle)
    val accepted: StateFlow<UiState<AcceptedHelpItem?>> = _accepted

    private val _history = MutableStateFlow<UiState<List<ConfirmedHelpItem>>>(UiState.Idle)
    val history: StateFlow<UiState<List<ConfirmedHelpItem>>> = _history

    private var pollOpenJob: Job? = null
    private var pollAcceptedJob: Job? = null

    fun startOpenPolling(helperId: Long) {
        if (pollOpenJob != null) return
        pollOpenJob = viewModelScope.launch {
            while (true) {
                fetchOpen(helperId, silent = true)
                delay(4000)
            }
        }
    }

    fun stopOpenPolling() { pollOpenJob?.cancel(); pollOpenJob = null }

    fun startAcceptedPolling(helperId: Long) {
        if (pollAcceptedJob != null) return
        pollAcceptedJob = viewModelScope.launch {
            while (true) {
                fetchAccepted(helperId, silent = true)
                delay(2000)
            }
        }
    }

    fun stopAcceptedPolling() { pollAcceptedJob?.cancel(); pollAcceptedJob = null }

    fun fetchOpen(helperId: Long, silent: Boolean = false) {
        if (!silent) _open.value = UiState.Loading
        viewModelScope.launch {
            val res = runCatching { repo.listOpen(helperId) }.getOrNull()
            if (res?.ok == true) _open.value = UiState.Data(res.items ?: emptyList())
            else _open.value = UiState.Error(res?.error ?: "Liste alınamadı")
        }
    }

    fun accept(reqId: Long, helperId: Long, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val res = runCatching { repo.accept(reqId, helperId) }.getOrNull()
            onDone(if (res?.ok == true) "Kabul edildi" else (res?.error ?: "Kabul edilemedi"))
        }
    }

    fun fetchAccepted(helperId: Long, silent: Boolean = false) {
        if (!silent) _accepted.value = UiState.Loading
        viewModelScope.launch {
            val res = runCatching { repo.myAccepted(helperId) }.getOrNull()
            if (res?.ok == true) _accepted.value = UiState.Data(res.items?.firstOrNull())
            else _accepted.value = UiState.Data(null)
        }
    }

    fun fetchHistory(helperId: Long) {
        _history.value = UiState.Loading
        viewModelScope.launch {
            val res = runCatching { repo.myConfirmed(helperId) }.getOrNull()
            if (res?.ok == true) _history.value = UiState.Data(res.items ?: emptyList())
            else _history.value = UiState.Error(res?.error ?: "Geçmiş alınamadı")
        }
    }
}