package com.example.yardimuygulamajetpack.vm



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yardimuygulamajetpack.model.HelpActive
import com.example.yardimuygulamajetpack.repo.PatientRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PatientViewModel(
    private val repo: PatientRepo = PatientRepo()
) : ViewModel() {

    private val _active = MutableStateFlow<UiState<HelpActive?>>(UiState.Idle)
    val active: StateFlow<UiState<HelpActive?>> = _active

    private var pollJob: Job? = null

    fun startPolling(patientId: Long) {
        if (pollJob != null) return
        pollJob = viewModelScope.launch {
            while (true) {
                fetchActive(patientId, silent = true)
                delay(2500)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun fetchActive(patientId: Long, silent: Boolean = false) {
        if (!silent) _active.value = UiState.Loading
        viewModelScope.launch {
            val res = runCatching { repo.myActive(patientId) }.getOrNull()
            if (res?.ok == true) {
                _active.value = UiState.Data(res.active)
            } else {
                // aktif yoksa error değil, "null" kabul ediyoruz
                _active.value = UiState.Data(null)
            }
        }
    }

    fun createHelp(patientId: Long, servis: String, oda: String, lat: Double, lng: Double, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val res = runCatching { repo.createHelp(patientId, servis, oda, lat, lng) }.getOrNull()
            onDone(if (res?.ok == true) "Durum: OPEN (yardımcı bekleniyor)" else (res?.error ?: "İstek gönderilemedi"))
            fetchActive(patientId, silent = true)
        }
    }

    fun confirm(reqId: Long, patientId: Long, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val res = runCatching { repo.confirm(reqId, patientId) }.getOrNull()
            onDone(if (res?.ok == true) "Durum: CONFIRMED (tamamlandı)" else (res?.error ?: "Onaylanamadı"))
            fetchActive(patientId, silent = true)
        }
    }

    fun cancel(reqId: Long, patientId: Long, onDone: (String) -> Unit) {
        viewModelScope.launch {
            val res = runCatching { repo.cancel(reqId, patientId) }.getOrNull()
            onDone(if (res?.ok == true) "İstek iptal edildi" else (res?.error ?: "İptal edilemedi"))
            fetchActive(patientId, silent = true)
        }
    }
}