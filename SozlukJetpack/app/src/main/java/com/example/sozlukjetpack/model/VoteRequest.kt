package com.example.sozlukjetpack.model

data class VoteRequest(
    val comment_id: Int,
    val user_id: Int,
    val is_like: Int
)
