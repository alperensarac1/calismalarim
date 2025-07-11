package com.example.qrkodolusturucujetpack.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class KodUretCevap(
    @SerializedName("success") @Expose var success: Int,
    @SerializedName("message") @Expose var message: String,
)