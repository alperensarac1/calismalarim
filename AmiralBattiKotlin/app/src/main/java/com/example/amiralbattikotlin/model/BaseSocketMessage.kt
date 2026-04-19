package com.example.amiralbattikotlin.model

import com.google.gson.JsonObject

data class BaseSocketMessage(
    val type: String,
    val data: JsonObject?
)