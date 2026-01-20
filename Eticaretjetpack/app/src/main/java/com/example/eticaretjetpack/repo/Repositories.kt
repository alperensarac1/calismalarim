package com.example.eticaretjetpack.repo

import com.example.eticaretjetpack.data.TokenStore
import com.example.eticaretjetpack.model.AddToCartResponse
import com.example.eticaretjetpack.model.CartDto
import com.example.eticaretjetpack.model.CategoryDto
import com.example.eticaretjetpack.model.CheckoutRequest
import com.example.eticaretjetpack.model.CheckoutResponse
import com.example.eticaretjetpack.model.LoginRequest
import com.example.eticaretjetpack.model.OrderDetailDto
import com.example.eticaretjetpack.model.OrderSummaryDto
import com.example.eticaretjetpack.model.ProductDto
import com.example.eticaretjetpack.model.ProductListPage
import com.example.eticaretjetpack.model.RegisterRequest
import com.example.eticaretjetpack.model.UserDto
import com.example.eticaretjetpack.service.AuthApi


interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun me(): Result<UserDto?>
}


interface ProductRepository {
    suspend fun getCategories(): Result<List<CategoryDto>>
    suspend fun getProducts(
        cat: Int? = null,
        q: String? = null,
        min: Double? = null,
        max: Double? = null,
        discount: Int? = null,
        sort: String? = null,
        page: Int? = 1,
        per: Int? = 12
    ): Result<ProductListPage>

    suspend fun getProduct(id: Int): Result<ProductDto>
}


interface CartRepository {
    suspend fun getCart(): Result<CartDto>
    suspend fun addToCart(productId: Int, quantity: Int): Result<AddToCartResponse>
    suspend fun updateItem(itemId: Int, quantity: Int): Result<Unit>
    suspend fun deleteItem(itemId: Int): Result<Unit>
}

// data/repo/OrderRepository.kt
interface OrderRepository {
    suspend fun checkout(body: CheckoutRequest): Result<CheckoutResponse>
    suspend fun getOrders(): Result<List<OrderSummaryDto>>
    suspend fun getOrderDetail(id: Int): Result<OrderDetailDto>
}
// data/repo/impl/AuthRepositoryImpl.kt

