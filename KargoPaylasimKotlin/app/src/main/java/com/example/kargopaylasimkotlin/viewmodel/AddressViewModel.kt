package com.example.kargopaylasimkotlin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kargopaylasimkotlin.dto.AddressCreateReq
import com.example.kargopaylasimkotlin.dto.AddressDto
import com.example.kargopaylasimkotlin.dto.AddressUpdateReq
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.repo.CargoRepository
import kotlinx.coroutines.launch

class AddressViewModel(private val repo: CargoRepository) : ViewModel() {

    val defaultState = MutableLiveData<UiState<AddressDto>>(UiState.Idle)
    val saveState = MutableLiveData<UiState<Unit>>(UiState.Idle)

    fun loadDefault() {
        defaultState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.addressList()
                if (!resp.ok || resp.data == null) {
                    defaultState.value = UiState.Error(resp.error ?: "Load failed")
                    return@launch
                }
                val list = resp.data.items ?: emptyList()
                val def = list.firstOrNull { it.is_default == 1 } ?: list.firstOrNull()
                if (def == null) defaultState.value = UiState.Error("Adres bulunamadı")
                else defaultState.value = UiState.Success(def)
            } catch (e: Exception) {
                defaultState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }


    fun saveOrCreate(
        addressId: Int,
        title: String,
        city: String,
        district: String,
        neighborhood: String?,
        line: String,
        postalCode: String?
    ) {
        saveState.value = UiState.Loading
        viewModelScope.launch {
            try {
                if (addressId > 0) {
                    val resp = repo.addressUpdate(
                        AddressUpdateReq(
                            id = addressId,
                            title = title,
                            city = city,
                            district = district,
                            neighborhood = neighborhood?.ifBlank { null },
                            address_line = line,
                            postal_code = postalCode?.ifBlank { null }
                        )
                    )
                    if (resp.ok) saveState.value = UiState.Success(Unit)
                    else saveState.value = UiState.Error(resp.error ?: "Save failed")
                } else {
                    val resp = repo.addressCreate(
                        AddressCreateReq(
                            title = title,
                            city = city,
                            district = district,
                            neighborhood = neighborhood?.ifBlank { null },
                            address_line = line,
                            postal_code = postalCode?.ifBlank { null }
                        )
                    )
                    if (resp.ok) saveState.value = UiState.Success(Unit)
                    else saveState.value = UiState.Error(resp.error ?: "Create failed")
                }
            } catch (e: Exception) {
                saveState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }


    fun loadById(id: Int) {
        defaultState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.addressDetail(id) // address_detail_post.php
                if (resp.ok && resp.data != null) defaultState.value = UiState.Success(resp.data)
                else defaultState.value = UiState.Error(resp.error ?: "Load failed")
            } catch (e: Exception) {
                defaultState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }


}
