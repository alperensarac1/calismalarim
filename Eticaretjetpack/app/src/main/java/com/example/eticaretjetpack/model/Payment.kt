package com.example.eticaretjetpack.model

import com.google.gson.annotations.SerializedName

data class PaymentDto(
    @SerializedName("provider")
    val provider: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("created_at")
    val createdAt: String
)
