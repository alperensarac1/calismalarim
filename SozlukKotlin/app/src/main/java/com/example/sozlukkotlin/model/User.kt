package com.example.sozlukkotlin.model

data class User(
    val id: Int,
    val username: String,
    val email: String? = null
)
