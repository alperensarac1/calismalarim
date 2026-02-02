package com.example.kargopaylasimkotlin.viewmodel


import androidx.lifecycle.*
import com.example.kargopaylasimkotlin.dto.ReceiverLookupResp
import com.example.kargopaylasimkotlin.dto.ShipmentCreateResp
import com.example.kargopaylasimkotlin.dto.ShipmentDeleteResp
import com.example.kargopaylasimkotlin.dto.ShipmentDetailDto
import com.example.kargopaylasimkotlin.dto.ShipmentDto
import com.example.kargopaylasimkotlin.dto.ShipmentItemDto
import com.example.kargopaylasimkotlin.dto.ShipmentRegenerateResp
import com.example.kargopaylasimkotlin.model.UiState
import com.example.kargopaylasimkotlin.repo.CargoRepository

import kotlinx.coroutines.launch

class ShipmentViewModel(private val repo: CargoRepository) : ViewModel() {

    val listState = MutableLiveData<UiState<List<ShipmentDto>>>(UiState.Idle)
    val createState = MutableLiveData<UiState<ShipmentCreateResp>>(UiState.Idle)
    val detailState = MutableLiveData<UiState<ShipmentDetailDto>>(UiState.Idle)
    val lookupState = MutableLiveData<UiState<ReceiverLookupResp>>(UiState.Idle)
    val regenerateState = MutableLiveData<UiState<ShipmentRegenerateResp>>(UiState.Idle)
    val deleteState = MutableLiveData<UiState<ShipmentDeleteResp>>(UiState.Idle)

    fun deleteShipment(shipmentId: Int) {
        deleteState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.shipmentDelete(shipmentId)
                if (resp.ok && resp.data != null) deleteState.value = UiState.Success(resp.data)
                else deleteState.value = UiState.Error(resp.error ?: "Delete failed")
            } catch (e: Exception) {
                deleteState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadShipments() {
        listState.value = UiState.Loading
        viewModelScope.launch {
            try {

                val resp = repo.shipmentList(type = "all")
                android.util.Log.d("SHIP", "count=" + (resp.data?.shipments?.size ?: -1))
                if (resp.ok && resp.data != null) {
                    listState.value = UiState.Success(resp.data.shipments) // ✅ burası
                } else {
                    listState.value = UiState.Error(resp.error ?: "Load failed")
                }
            } catch (e: Exception) {
                listState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }


    fun lookupReceiver(phone: String) {
        lookupState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.receiverLookup(phone)
                if (resp.ok && resp.data != null) lookupState.value = UiState.Success(resp.data)
                else lookupState.value = UiState.Error(resp.error ?: "Lookup failed")
            } catch (e: Exception) {
                lookupState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun createShipment(receiverPhone: String, senderAddressId: Int?) {
        createState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.shipmentCreate(receiverPhone, senderAddressId)
                if (resp.ok && resp.data != null) createState.value = UiState.Success(resp.data)
                else createState.value = UiState.Error(resp.error ?: "Create failed")
            } catch (e: Exception) {
                createState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadDetail(id: Int) {
        detailState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.shipmentDetail(id)
                if (resp.ok && resp.data != null) detailState.value = UiState.Success(resp.data.shipment)
                else detailState.value = UiState.Error(resp.error ?: "Detail failed")
            } catch (e: Exception) {
                detailState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun regenerateCode(shipmentId: Int) {
        regenerateState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val resp = repo.shipmentRegenerate(shipmentId)
                if (resp.ok && resp.data != null) regenerateState.value = UiState.Success(resp.data)
                else regenerateState.value = UiState.Error(resp.error ?: "Regenerate failed")
            } catch (e: Exception) {
                regenerateState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }
}
