package com.example.eticaretkotlin.util

import com.example.eticaretkotlin.model.ApiResponse

class ApiException(message: String) : Exception(message)

inline fun <T> ApiResponse<T>.toResult(): Result<T> {
    return if (this.ok && this.data != null) {
        Result.success(this.data)
    } else {
        Result.failure(ApiException(this.data.toString() ?: "API hatası"))
    }
}
