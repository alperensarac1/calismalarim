package com.example.haberuygulamajetpack.servis

import com.example.haberuygulamajetpack.model.HaberModel

data class HaberResponse(
    val success: Boolean,
    val data: HaberModel?,
    val message: String? = null
)