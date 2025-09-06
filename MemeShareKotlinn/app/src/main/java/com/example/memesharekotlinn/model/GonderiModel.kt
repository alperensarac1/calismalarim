package com.example.memesharekotlinn.model

import com.google.gson.annotations.SerializedName

data class GonderiModel(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("room_id")
    val roomId: Int,
    @SerializedName("media_type")
    val mediaType: String,
    @SerializedName("media_url")
    val mediaUrl: String,
    @SerializedName("caption")
    val caption: String,
    @SerializedName("uploaded_at")
    val uploadedAt: String
)