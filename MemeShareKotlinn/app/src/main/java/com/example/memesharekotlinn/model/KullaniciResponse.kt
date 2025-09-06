package com.example.memesharekotlinn.model

import com.google.gson.annotations.SerializedName

data class KullaniciResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("user_id")
    val userId: Int
)
