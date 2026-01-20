package com.example.eticaretjetpack.model

import com.google.gson.annotations.SerializedName

data class BasicOk(
    @SerializedName("ok")
    val ok: Boolean
)

// Ortak response sarmalayıcı
data class ApiResponse<T>(
    @SerializedName("ok")
    val ok: Boolean,

    @SerializedName("data")
    val data: T?,

    @SerializedName("error")
    val error: String? = null
)

