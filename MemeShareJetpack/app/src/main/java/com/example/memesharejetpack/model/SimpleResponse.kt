package com.example.memesharejetpack.model

import com.google.gson.annotations.SerializedName

data class SimpleResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("room_code")
    val roomCode: String?,
    @SerializedName("room_id")
    val roomId: Int?
)