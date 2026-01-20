package com.example.eticaretkotlin.model


import com.google.gson.annotations.SerializedName

data class CheckoutRequest(
    @SerializedName("idempotency_key")
    val idempotencyKey: String? = null,

    @SerializedName("address_name")
    val addressName: String? = null,

    @SerializedName("address_line1")
    val addressLine1: String,

    @SerializedName("address_line2")
    val addressLine2: String? = null,

    @SerializedName("city")
    val city: String,

    @SerializedName("district")
    val district: String? = null,

    @SerializedName("postal_code")
    val postalCode: String? = null
)


data class CheckoutResponse(
    @SerializedName("order_id")
    val orderId: Int,

    @SerializedName("total")
    val total: Double,

    @SerializedName("currency")
    val currency: String
)
