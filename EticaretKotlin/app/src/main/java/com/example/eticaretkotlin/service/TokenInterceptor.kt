package com.example.eticaretkotlin.service

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val t = tokenProvider()
        val req = if (!t.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $t")
                .build()
        } else original
        return chain.proceed(req)
    }
}