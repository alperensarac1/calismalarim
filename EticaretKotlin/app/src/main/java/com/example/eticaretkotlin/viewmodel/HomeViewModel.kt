package com.example.eticaretkotlin.viewmodel

// ui/home/HomeViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eticaretkotlin.model.CategoryDto
import com.example.eticaretkotlin.model.ProductListDto
import com.example.eticaretkotlin.repo.ProductRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProductFilters(
    val cat: Int? = null,
    val q: String? = null,
    val min: Double? = null,
    val max: Double? = null,
    val discount: Boolean = false,
    val sort: String = "newest"
)

class HomeViewModel(
    private val productRepo: ProductRepository
) : ViewModel() {

    data class HomeState(
        val loading: Boolean = false,
        val error: String? = null,
        val categories: List<CategoryDto> = emptyList(),
        val items: List<ProductListDto> = emptyList(),
        val page: Int = 1,
        val total: Int = 0,
        val per: Int = 12,
        val filters: ProductFilters = ProductFilters()
    )

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    private var pagingJob: Job? = null

    fun loadCategories() = viewModelScope.launch {
        // İstersen loading’i burada da gösterebilirsin
        // _state.value = _state.value.copy(loading = true, error = null)
        productRepo.getCategories()
            .onSuccess { list ->
                _state.value = _state.value.copy(categories = list /*, loading = false*/)
            }
            .onFailure { e ->
                _state.value = _state.value.copy(error = e.message ?: "Kategori hatası" /*, loading = false*/)
            }
    }

    fun loadProducts(page: Int = 1) {
        pagingJob?.cancel()
        pagingJob = viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, page = page)

            productRepo.getProducts(
                cat = _state.value.filters.cat,
                q = _state.value.filters.q,
                min = _state.value.filters.min,
                max = _state.value.filters.max,
                discount = if (_state.value.filters.discount) 1 else null,
                sort = _state.value.filters.sort,
                page = page,
                per = _state.value.per
            ).onSuccess { resp ->
                _state.value = _state.value.copy(
                    loading = false,
                    items = resp.items,
                    total = resp.total,
                    page = resp.page,
                    per = resp.per
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Ürün hatası"
                )
            }
        }
    }

    fun setFilters(newFilters: ProductFilters) {
        _state.value = _state.value.copy(filters = newFilters)
        loadProducts(page = 1)
    }
}
