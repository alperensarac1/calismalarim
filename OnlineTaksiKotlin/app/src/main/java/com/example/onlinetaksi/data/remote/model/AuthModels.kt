package com.example.onlinetaksi.data.remote.model


data class RegisterRequest(
    val full_name: String,
    val phone: String,
    val email: String?,
    val password: String,
    val role: String
)

data class LoginRequest(
    val phone: String,
    val password: String
)

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val user_id: Int,
    val full_name: String,
    val role: String
)