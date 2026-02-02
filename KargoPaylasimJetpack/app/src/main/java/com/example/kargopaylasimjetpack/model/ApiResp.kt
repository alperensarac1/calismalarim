package com.example.kargopaylasimjetpack.model

data class ApiResp<T>(
    val ok: Boolean,
    val data: T?,
    val error: String?
)
