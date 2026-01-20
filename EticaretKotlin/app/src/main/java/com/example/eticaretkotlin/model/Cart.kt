package com.example.eticaretkotlin.model

data class CartDto(
    val cart_id: Int?,
    val items: List<CartItemDto>,
    val total: Double,
    val total_items: Int
)

data class CartItemDto(
    val item_id: Int,
    val quantity: Int,
    val product_id: Int,
    val name: String,
    val sku: String?,
    val image_url: String?,
    val stock_qty: Int,
    val price: Double,
    val discount_percent: Double?,
    val sale_price: Double
)

data class AddToCartRequest(
    val product_id: Int,
    val quantity: Int
)

data class AddToCartResponse(
    val cart_id: Int,
    val item_id: Int,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)
