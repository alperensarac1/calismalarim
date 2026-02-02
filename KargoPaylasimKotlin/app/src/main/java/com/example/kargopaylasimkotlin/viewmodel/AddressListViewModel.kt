package com.example.kargopaylasimkotlin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kargopaylasimkotlin.dto.AddressDto
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.repo.CargoRepository
import kotlinx.coroutines.launch


class AddressListViewModel(private val repo: CargoRepository) : ViewModel() {

    val listState = MutableLiveData<UiState<List<AddressDto>>>(UiState.Idle)
    val deleteState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val setDefaultState = MutableLiveData<UiState<Unit>>(UiState.Idle)

    fun load() {
        listState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.addressList()
                if (resp.ok && resp.data != null) {
                    listState.value = UiState.Success(resp.data.items ?: emptyList())
                } else listState.value = UiState.Error(resp.error ?: "Load failed")
            } catch (e: Exception) {
                listState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun setDefault(id: Int) {
        setDefaultState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.addressSetDefault(id)
                if (resp.ok) setDefaultState.value = UiState.Success(Unit)
                else setDefaultState.value = UiState.Error(resp.error ?: "Default failed")
            } catch (e: Exception) {
                setDefaultState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun delete(id: Int) {
        deleteState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.addressDelete(id)
                if (resp.ok) deleteState.value = UiState.Success(Unit)
                else deleteState.value = UiState.Error(resp.error ?: "Delete failed")
            } catch (e: Exception) {
                deleteState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }
}
