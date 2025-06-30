package com.example.qrkodolusturucu.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CRUDCevap(
    @SerializedName("success") @Expose var success:Int,
    @SerializedName("message") @Expose var message:String,
    @SerializedName("icilen_kahve") @Expose var kahveSayisi:Int,
    @SerializedName("hediye_kahve") @Expose var hediyeKahve:Int
)