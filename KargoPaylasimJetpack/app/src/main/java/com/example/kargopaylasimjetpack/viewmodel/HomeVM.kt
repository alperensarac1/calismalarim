package com.example.kargopaylasimjetpack.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kargopaylasimjetpack.model.Address
import com.example.kargopaylasimjetpack.model.Shipment
import com.example.kargopaylasimjetpack.repository.Repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUi(
    val loading: Boolean = false,
    val error: String? = null,
    val shipments: List<Shipment> = emptyList(),
    val addresses: List<Address> = emptyList()
)

class HomeVM(private val repo: Repo) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUi())
    val ui: StateFlow<HomeUi> = _ui

    fun refresh() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val s = repo.shipmentListAll()
                val a = repo.addressList()
                if (!s.ok) throw Exception(s.error ?: "shipment_list failed")
                if (!a.ok) throw Exception(a.error ?: "address_list failed")

                _ui.value = _ui.value.copy(
                    loading = false,
                    shipments = s.data?.items ?: emptyList(),
                    addresses = a.data?.items ?: emptyList()
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Error")
            }
        }
    }

    fun setDefaultAddress(id: Int) {
        viewModelScope.launch {
            try {
                val r = repo.addressSetDefault(id)
                if (!r.ok) throw Exception(r.error ?: "Set default failed")
                refresh()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message)
            }
        }
    }

    fun deleteAddress(id: Int) {
        viewModelScope.launch {
            try {
                val r = repo.addressDelete(id)
                if (!r.ok) throw Exception(r.error ?: "Delete failed")
                refresh()
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message)
            }
        }
    }
}
