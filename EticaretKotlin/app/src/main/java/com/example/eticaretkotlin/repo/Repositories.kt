package com.example.eticaretkotlin.repo

import com.example.eticaretkotlin.data.TokenStore
import com.example.eticaretkotlin.model.AddToCartResponse
import com.example.eticaretkotlin.model.CartDto
import com.example.eticaretkotlin.model.CategoryDto
import com.example.eticaretkotlin.model.CheckoutRequest
import com.example.eticaretkotlin.model.CheckoutResponse
import com.example.eticaretkotlin.model.LoginRequest
import com.example.eticaretkotlin.model.OrderDetailDto
import com.example.eticaretkotlin.model.OrderSummaryDto
import com.example.eticaretkotlin.model.ProductDto
import com.example.eticaretkotlin.model.ProductListPage
import com.example.eticaretkotlin.model.RegisterRequest
import com.example.eticaretkotlin.model.UserDto
import com.example.eticaretkotlin.service.AuthApi

// data/repo/AuthRepository.kt
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun register(name: String, email: String, password: String): Result<Unit>
    suspend fun me(): Result<UserDto?>
}

// data/repo/ProductRepository.kt
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

// data/repo/CartRepository.kt
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

