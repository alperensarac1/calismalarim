package com.example.eticaretjetpack.viewmodel

// ui/orders/OrdersViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eticaretjetpack.model.CheckoutRequest
import com.example.eticaretjetpack.model.CheckoutResponse
import com.example.eticaretjetpack.model.OrderDetailDto
import com.example.eticaretjetpack.model.OrderSummaryDto
import com.example.eticaretjetpack.repo.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class OrdersViewModel(
    private val orderRepo: OrderRepository
) : ViewModel() {

    data class OrdersState(
        val loading: Boolean = false,
        val error: String? = null,
        val orders: List<OrderSummaryDto> = emptyList(),
        val lastOrder: CheckoutResponse? = null,
        val orderDetail: OrderDetailDto? = null
    )

    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state

    fun checkout(addr: CheckoutRequest) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null, lastOrder = null)

        val body = addr.copy(
            idempotencyKey = addr.idempotencyKey ?: UUID.randomUUID().toString()
        )

        runCatching { orderRepo.checkout(body) }
            .onSuccess { result ->
                val data = result.getOrNull()
                _state.value = _state.value.copy(
                    loading = false,
                    lastOrder = data,
                    error = if (data == null) "Checkout sonucu boş döndü" else null
                )
            }
            .onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Checkout hatası"
                )
            }
    }

    fun loadOrders() = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null)

        runCatching { orderRepo.getOrders() }
            .onSuccess { result ->
                val list = result.getOrNull()
                _state.value = _state.value.copy(
                    loading = false,
                    orders = list ?: emptyList(),
                    error = if (list == null) "Siparişler alınamadı (boş yanıt / bağlantı sorunu)" else null
                )
            }
            .onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    orders = emptyList(),
                    error = e.message ?: "Sipariş liste hatası"
                )
            }
    }

    fun loadOrderDetail(id: Int) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, error = null, orderDetail = null)

        runCatching { orderRepo.getOrderDetail(id) }
            .onSuccess { result ->
                val detail = result.getOrNull()
                _state.value = _state.value.copy(
                    loading = false,
                    orderDetail = detail,
                    error = if (detail == null) "Sipariş detayı boş döndü" else null
                )
            }
            .onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Sipariş detay hatası"
                )
            }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
