package com.example.eticaretkotlin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eticaretkotlin.model.ProductDto
import com.example.eticaretkotlin.repo.CartRepository
import com.example.eticaretkotlin.repo.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val productRepo: ProductRepository,
    private val cartRepo: CartRepository
) : ViewModel() {

    data class DetailState(
        val loading: Boolean = false,
        val error: String? = null,
        val product: ProductDto? = null,
        val addSuccess: Boolean = false
    )

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    fun load(id: Int) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null, addSuccess = false) }

        productRepo.getProduct(id)
            .onSuccess { prod ->
                _state.update { it.copy(loading = false, product = prod) }
            }
            .onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Detay hatası") }
            }
    }

    fun addToCart(productId: Int, qty: Int) = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null, addSuccess = false) }

        cartRepo.addToCart(productId, qty)
            .onSuccess {
                _state.update { it.copy(loading = false, addSuccess = true) }
            }
            .onFailure { e ->
                _state.update { it.copy(loading = false, error = e.message ?: "Sepete ekleme hatası") }
            }
    }

    fun clearFlags() = _state.update { it.copy(addSuccess = false, error = null) }
}
