package com.example.eticaretjetpack.model

import com.google.gson.annotations.SerializedName

data class OrderSummaryDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("total_amount")
    val totalAmount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("created_at")
    val createdAt: String
)

data class OrderDetailDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("total_amount")
    val totalAmount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("address_name")
    val addressName: String?,

    @SerializedName("address_line1")
    val addressLine1: String?,

    @SerializedName("address_line2")
    val addressLine2: String?,

    @SerializedName("city")
    val city: String?,

    @SerializedName("district")
    val district: String?,

    @SerializedName("postal_code")
    val postalCode: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("items")
    val items: List<OrderItemDto>,

    @SerializedName("payment")
    val payment: PaymentDto?
)

data class OrderItemDto(
    @SerializedName("product_id")
    val productId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("sku")
    val sku: String?,

    @SerializedName("unit_price")
    val unitPrice: Double,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("line_total")
    val lineTotal: Double
)
