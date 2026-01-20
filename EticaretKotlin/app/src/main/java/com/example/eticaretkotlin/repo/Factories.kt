package com.example.eticaretkotlin.repo

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.eticaretkotlin.data.TokenStore

import com.example.eticaretkotlin.viewmodel.AuthViewModel
import com.example.eticaretkotlin.viewmodel.CartViewModel
import com.example.eticaretkotlin.viewmodel.HomeViewModel
import com.example.eticaretkotlin.viewmodel.OrdersViewModel
import com.example.eticaretkotlin.viewmodel.ProductDetailViewModel


class AuthVMFactory(ctx: Context) : ViewModelProvider.Factory {
    private val tokenStore = TokenStore(ctx.applicationContext)

    // ✅ interface tipinde tut
    private val repo: AuthRepositoryImpl =
        AuthRepositoryImpl(RetrofitProvider.authApi, tokenStore)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(repo) as T
    }
}


class HomeVMFactory : ViewModelProvider.Factory {
    private val repo = ProductRepositoryImpl(RetrofitProvider.productApi) // kendi impl’in
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") return HomeViewModel(repo) as T
    }
}

class ProductDetailVMFactory(ctx: Context) : ViewModelProvider.Factory {
    private val prod = ProductRepositoryImpl(RetrofitProvider.productApi)
    private val cart = CartRepositoryImpl(RetrofitProvider.cartApi, tokenStore = TokenStore(ctx.applicationContext))
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") return ProductDetailViewModel(prod, cart) as T
    }
}


class CartVMFactory(ctx: Context) : ViewModelProvider.Factory {
    private val repo: CartRepository =
        CartRepositoryImpl(RetrofitProvider.cartApi, TokenStore(ctx.applicationContext))

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CartViewModel(repo) as T
    }
}


class OrdersVMFactory(ctx: Context) : ViewModelProvider.Factory {
    private val repo = OrderRepositoryImpl(RetrofitProvider.orderApi, TokenStore(ctx.applicationContext))
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") return OrdersViewModel(repo) as T
    }
}


