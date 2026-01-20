package com.example.eticaretjetpack.model

import com.google.gson.annotations.SerializedName

// Ürün listesi için sade DTO
data class ProductListDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("discount_percent")
    val discountPercent: Double?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("stock_qty")
    val stockQty: Int,

    @SerializedName("is_active")
    val isActive: Int
)

// Ürün detay DTO
data class ProductDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("sku")
    val sku: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("discount_percent")
    val discountPercent: Double?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("stock_qty")
    val stockQty: Int,

    @SerializedName("is_active")
    val isActive: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?
)
