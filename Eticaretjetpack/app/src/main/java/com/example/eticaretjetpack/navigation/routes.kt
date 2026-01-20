package com.example.eticaretjetpack.navigation


object Routes {
    // auth
    const val LOGIN = "login"
    const val REGISTER = "register"

    // main tabs
    const val HOME = "home"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val SETTINGS = "settings"

    // details
    const val PRODUCT_DETAIL = "productDetail"
    const val ORDER_DETAIL = "orderDetail"

    fun productDetail(id: Int) = "$PRODUCT_DETAIL/$id"
    fun orderDetail(id: Int) = "$ORDER_DETAIL/$id"
}
