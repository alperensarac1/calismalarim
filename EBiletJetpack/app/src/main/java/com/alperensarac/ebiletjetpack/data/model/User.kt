package com.alperensarac.ebiletjetpack.data.model

import com.google.gson.annotations.SerializedName

/*
    Kullanıcı modeli.
*/
data class User(
    val id: Int,

    @SerializedName("full_name")
    val fullName: String,

    val email: String,

    val phone: String? = null,

    val role: String,

    @SerializedName("api_token")
    val apiToken: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)