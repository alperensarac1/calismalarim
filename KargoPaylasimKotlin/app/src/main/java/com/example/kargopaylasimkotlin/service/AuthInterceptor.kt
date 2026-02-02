package com.example.kargopaylasimkotlin.service

import okhttp3.Interceptor

class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {

        val req = chain.request()
        val token = tokenStore.getToken()

        val newReq = if (!token.isNullOrBlank()) {
            req.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else req

        return chain.proceed(newReq)
    }
}
