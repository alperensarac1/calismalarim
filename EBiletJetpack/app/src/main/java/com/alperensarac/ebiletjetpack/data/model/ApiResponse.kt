package com.alperensarac.ebiletjetpack.data.model


data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val extra: Any? = null
)