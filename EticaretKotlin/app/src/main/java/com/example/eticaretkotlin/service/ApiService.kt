package com.example.eticaretkotlin.service

import com.example.eticaretkotlin.model.AddToCartRequest
import com.example.eticaretkotlin.model.AddToCartResponse
import com.example.eticaretkotlin.model.ApiResponse
import com.example.eticaretkotlin.model.BasicOk
import com.example.eticaretkotlin.model.CartDto
import com.example.eticaretkotlin.model.CategoryDto
import com.example.eticaretkotlin.model.CheckoutRequest
import com.example.eticaretkotlin.model.CheckoutResponse
import com.example.eticaretkotlin.model.LoginRequest
import com.example.eticaretkotlin.model.LoginResponse
import com.example.eticaretkotlin.model.OrderDetailDto
import com.example.eticaretkotlin.model.OrderSummaryDto
import com.example.eticaretkotlin.model.ProductDto
import com.example.eticaretkotlin.model.ProductListDto
import com.example.eticaretkotlin.model.ProductListPage
import com.example.eticaretkotlin.model.RegisterRequest
import com.example.eticaretkotlin.model.RegisterResponse
import com.example.eticaretkotlin.model.UpdateCartItemRequest
import com.example.eticaretkotlin.model.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// network/ApiService.kt
import retrofit2.http.*

interface ProductApi {
    @GET("categories")
    suspend fun getCategories(): ApiResponse<List<CategoryDto>>

    @GET("products")
    suspend fun getProducts(
        @Query("cat") cat: Int? = null,
        @Query("q") q: String? = null,
        @Query("min") min: Double? = null,
        @Query("max") max: Double? = null,
        @Query("discount") discount: Int? = null,
        @Query("sort") sort: String? = null,
        @Query("page") page: Int? = null,
        @Query("per") per: Int? = null
    ): ApiResponse<ProductListPage>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ApiResponse<ProductDto>
}


interface CartApi {
    @GET("cart.php")
    suspend fun getCart(@Header("Authorization") bearer: String): ApiResponse<CartDto>

    @POST("cart_add.php")
    suspend fun addToCart(
        @Header("Authorization") bearer: String,
        @Body body: AddToCartRequest
    ): ApiResponse<AddToCartResponse>

    @POST("cart_item.php")
    suspend fun updateItem(
        @Header("Authorization") bearer: String,
        @Query("id") itemId: Int,
        @Body body: UpdateCartItemRequest
    ): ApiResponse<BasicOk>

    @DELETE("cart_item.php")
    suspend fun deleteItem(
        @Header("Authorization") bearer: String,
        @Query("id") itemId: Int
    ): ApiResponse<BasicOk>
}


interface OrderApi {
    @POST("checkout.php")
    suspend fun checkout(
        @Header("Authorization") bearer: String,
        @Body body: CheckoutRequest
    ): ApiResponse<CheckoutResponse>

    @GET("orders.php")
    suspend fun getOrders(
        @Header("Authorization") bearer: String
    ): ApiResponse<List<OrderSummaryDto>>

    @GET("order.php")
    suspend fun getOrderDetail(
        @Header("Authorization") bearer: String,
        @Query("id") orderId: Int
    ): ApiResponse<OrderDetailDto>
}










