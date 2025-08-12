package com.example.sozlukkotlin.model

data class Comment(
    val id: Int,
    val comment_text: String,
    val created_at: String,
    val username: String,
    val likes: Int,
    val dislikes: Int
)
