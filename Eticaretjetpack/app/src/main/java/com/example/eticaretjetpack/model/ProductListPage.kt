package com.example.eticaretjetpack.model

import com.google.gson.annotations.SerializedName

data class ProductListPage(
    @SerializedName("page")
    val page: Int,

    @SerializedName("per")
    val per: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("items")
    val items: List<ProductListDto>
)
