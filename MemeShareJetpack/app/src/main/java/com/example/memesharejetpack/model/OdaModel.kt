package com.example.memesharejetpack.model

import com.google.gson.annotations.SerializedName

data class OdaModel(
    @SerializedName("room_id")
    val odaId: Int,
    @SerializedName("room_code")
    val roomCode: String,
    @SerializedName("created_by")
    val createdBy: Int
)