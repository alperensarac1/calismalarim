package com.example.kargopaylasimjetpack.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kargopaylasimjetpack.model.AddressCreateReq
import com.example.kargopaylasimjetpack.repository.Repo
import com.example.kargopaylasimjetpack.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddressCreateVM(private val repo: Repo) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val state: StateFlow<UiState<Boolean>> = _state

    fun save(req: AddressCreateReq) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val r = repo.addressCreate(req)
                if (!r.ok) throw Exception(r.error ?: "Adres eklenemedi")
                _state.value = UiState.Success(true)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Error")
            }
        }
    }
}
