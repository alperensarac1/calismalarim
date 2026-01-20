package com.example.eticaretkotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eticaretkotlin.model.CartDto
import com.example.eticaretkotlin.repo.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CartViewModel(
    private val repo: CartRepository
) : ViewModel() {

    data class CartState(
        val loading: Boolean = false,
        val error: String? = null,
        val cart: CartDto? = null,
        val busyItemId: Int? = null,   // + / - / sil tıklanınca o satırı disable etmek için
        val lastAction: String? = null // istersen toast/snackbar için: "updated", "deleted" vb.
    )

    private val _state = MutableStateFlow(CartState())
    val state: StateFlow<CartState> = _state

    fun loadCart() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null, lastAction = null) }
        repo.getCart()
            .onSuccess { cart ->
                _state.update { it.copy(loading = false, cart = cart) }
            }
            .onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Sepet yüklenemedi") }
            }
    }

    fun add(productId: Int, quantity: Int) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null, lastAction = null) }
        repo.addToCart(productId, quantity)
            .onSuccess {
                _state.update { it.copy(loading = false, lastAction = "added") }
                loadCart()
            }
            .onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Sepete eklenemedi") }
            }
    }

    fun inc(itemId: Int, currentQty: Int) = setQty(itemId, currentQty + 1)
    fun dec(itemId: Int, currentQty: Int) = setQty(itemId, (currentQty - 1).coerceAtLeast(1))

    fun setQty(itemId: Int, quantity: Int) = viewModelScope.launch {
        _state.update { it.copy(busyItemId = itemId, error = null, lastAction = null) }
        repo.updateItem(itemId, quantity)
            .onSuccess {
                _state.update { it.copy(busyItemId = null, lastAction = "updated") }
                loadCart()
            }
            .onFailure { e ->
                _state.update { it.copy(busyItemId = null, error = e.message ?: "Sepet güncellenemedi") }
            }
    }

    fun delete(itemId: Int) = viewModelScope.launch {
        _state.update { it.copy(busyItemId = itemId, error = null, lastAction = null) }
        repo.deleteItem(itemId)
            .onSuccess {
                _state.update { it.copy(busyItemId = null, lastAction = "deleted") }
                loadCart()
            }
            .onFailure { e ->
                _state.update { it.copy(busyItemId = null, error = e.message ?: "Ürün silinemedi") }
            }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
