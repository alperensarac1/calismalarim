package com.example.chatkotlin.service.response

import com.example.chatkotlin.model.Mesaj

data class MesajListResponse(
    val success: Boolean,
    val mesajlar: List<Mesaj>
)
