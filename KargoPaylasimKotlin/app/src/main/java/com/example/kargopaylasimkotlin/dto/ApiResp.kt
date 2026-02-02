package com.example.kargopaylasimkotlin.dto

data class ApiResp<T>(
    val ok: Boolean,
    val data: T? = null,
    val error: String? = null
)

