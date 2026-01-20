package com.example.eticaretkotlin.model

import com.google.gson.annotations.SerializedName

// Login isteği
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

// Kullanıcı DTO
data class UserDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String?
)

data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class RegisterResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user_id")
    val userId: Int
)

// Sunucunun login cevabı: { ok:true, data:{ token:"...", user_id:123 } }
data class LoginResponse(
    @SerializedName("token")
    val token: String,

    @SerializedName("user_id")
    val userId: Int
)
