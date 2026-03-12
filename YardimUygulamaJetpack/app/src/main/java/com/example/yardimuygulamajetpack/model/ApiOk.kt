package com.example.yardimuygulamajetpack.model


data class ApiOk<T>(
    val ok: Boolean? = null,
    val error: String? = null,
    val user: User? = null,
    val items: List<T>? = null,
    val active: T? = null
)