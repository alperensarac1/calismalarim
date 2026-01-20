package com.example.eticaretjetpack.util

import com.example.eticaretjetpack.model.ApiResponse

// data/ApiCall.kt
suspend inline fun <T> apiCall(crossinline block: suspend () -> ApiResponse<T>): Result<T> {
    return try {
        val res = block()
        if (res.ok && res.data != null) {
            Result.success(res.data)
        } else {
            Result.failure(IllegalStateException(res.error ?: "API_ERROR"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
