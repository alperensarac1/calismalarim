package com.example.kargopaylasimjetpack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kargopaylasimjetpack.model.CreateShipmentData
import com.example.kargopaylasimjetpack.model.LookupReceiverData
import com.example.kargopaylasimjetpack.repository.Repo
import com.example.kargopaylasimjetpack.util.UiState

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateShipmentVM(private val repo: Repo) : ViewModel() {

    private val _lookup = MutableStateFlow<UiState<LookupReceiverData>>(UiState.Idle)
    val lookup: StateFlow<UiState<LookupReceiverData>> = _lookup

    private val _create = MutableStateFlow<UiState<CreateShipmentData>>(UiState.Idle)
    val create: StateFlow<UiState<CreateShipmentData>> = _create

    private var confirmedPhone: String? = null

    fun reset() {
        confirmedPhone = null
        _lookup.value = UiState.Idle
        _create.value = UiState.Idle
    }

    fun lookupReceiver(phone: String) {
        viewModelScope.launch {
            _lookup.value = UiState.Loading
            try {
                val r = repo.receiverLookup(phone)
                if (!r.ok || r.data == null) throw Exception(r.error ?: "User not found")
                confirmedPhone = phone
                _lookup.value = UiState.Success(r.data)
            } catch (e: Exception) {
                _lookup.value = UiState.Error(e.message ?: "Lookup error")
            }
        }
    }

    fun createShipment() {
        val p = confirmedPhone
        if (p.isNullOrBlank()) {
            _create.value = UiState.Error("Önce kişiyi bul.")
            return
        }
        viewModelScope.launch {
            _create.value = UiState.Loading
            try {
                val r = repo.shipmentCreate(p)
                if (!r.ok || r.data == null) {
                    val msg = r.error ?: "Create failed"
                    if (msg.contains("RECEIVER_ADDRESS_MISSING", true) || msg.contains("receiver address not found", true)) {
                        throw Exception("Bu kullanıcı henüz adresini kaydetmemiş.")
                    }
                    throw Exception(msg)
                }
                _create.value = UiState.Success(r.data)
            } catch (e: Exception) {
                _create.value = UiState.Error(e.message ?: "Create error")
            }
        }
    }
}
