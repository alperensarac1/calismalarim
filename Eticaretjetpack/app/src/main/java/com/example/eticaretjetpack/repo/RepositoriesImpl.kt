package com.example.eticaretjetpack.repo

import com.example.eticaretjetpack.data.TokenStore
import com.example.eticaretjetpack.model.AddToCartRequest
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
import com.example.eticaretjetpack.model.UpdateCartItemRequest
import com.example.eticaretjetpack.model.UserDto
import com.example.eticaretjetpack.service.AuthApi
import com.example.eticaretjetpack.service.CartApi
import com.example.eticaretjetpack.service.OrderApi
import com.example.eticaretjetpack.service.ProductApi
import com.example.eticaretjetpack.util.ApiException
import com.example.eticaretjetpack.util.apiCall
import com.example.eticaretjetpack.util.toResult


class AuthRepositoryImpl(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> =
        runCatching {
            val res = api.login(LoginRequest(email, password))
            val data = res.toResult().getOrThrow()
            tokenStore.token = data.token
        }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> =
        runCatching {
            val res = api.register(RegisterRequest(name, email, password))
            val data = res.toResult().getOrThrow()
            tokenStore.token = data.token
        }

    override suspend fun me(): Result<UserDto?> =
        runCatching {
            val t = tokenStore.token ?: throw ApiException("Token yok")
            api.me("Bearer $t").toResult().getOrThrow()
        }
}


class ProductRepositoryImpl(
    private val api: ProductApi
) : ProductRepository {

    override suspend fun getCategories(): Result<List<CategoryDto>> =
        apiCall { api.getCategories() }

    override suspend fun getProducts(
        cat: Int?, q: String?, min: Double?, max: Double?, discount: Int?,
        sort: String?, page: Int?, per: Int?
    ): Result<ProductListPage> =
        apiCall {
            api.getProducts(
                cat = cat, q = q, min = min, max = max,
                discount = discount, sort = sort, page = page, per = per
            )
        }

    override suspend fun getProduct(id: Int): Result<ProductDto> =
        apiCall { api.getProduct(id) }
}
class CartRepositoryImpl(
    private val api: CartApi,
    private val tokenStore: TokenStore
) : CartRepository {

    private fun bearer(): String =
        "Bearer " + (tokenStore.token ?: throw ApiException("Token yok"))

    override suspend fun getCart(): Result<CartDto> =
        runCatching {
            api.getCart(bearer()).toResult().getOrThrow()
        }

    override suspend fun addToCart(productId: Int, quantity: Int): Result<AddToCartResponse> =
        runCatching {
            api.addToCart(bearer(), AddToCartRequest(productId, quantity))
                .toResult()
                .getOrThrow()
        }

    override suspend fun updateItem(itemId: Int, quantity: Int): Result<Unit> =
        runCatching {
            api.updateItem(bearer(), itemId, UpdateCartItemRequest(quantity))
                .toResult()
                .getOrThrow()
            Unit
        }

    override suspend fun deleteItem(itemId: Int): Result<Unit> =
        runCatching {
            api.deleteItem(bearer(), itemId)
                .toResult()
                .getOrThrow()
            Unit
        }
}


class OrderRepositoryImpl(
    private val api: OrderApi,
    private val tokenStore: TokenStore
) : OrderRepository {

    private fun bearer(): String =
        "Bearer " + (tokenStore.token ?: throw ApiException("Token yok"))

    override suspend fun getOrders(): Result<List<OrderSummaryDto>> = runCatching {
        api.getOrders(bearer()).toResult().getOrThrow()
    }

    override suspend fun getOrderDetail(id: Int): Result<OrderDetailDto> = runCatching {
        api.getOrderDetail(bearer(), id).toResult().getOrThrow()
    }

    override suspend fun checkout(body: CheckoutRequest): Result<CheckoutResponse> = runCatching {
        api.checkout(bearer(), body).toResult().getOrThrow()
    }
}
