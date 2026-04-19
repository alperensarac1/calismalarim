package com.example.amiralbattijetpack.model

import com.google.gson.JsonObject

data class BaseSocketMessage(
    val type: String,
    val data: JsonObject?
)
