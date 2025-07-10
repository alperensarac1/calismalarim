package com.example.chatkotlin.service.response

import com.example.chatkotlin.model.KonusulanKisi

data class KonusulanKisiListResponse(
    val success: Boolean,
    val kisiler: List<KonusulanKisi>
)
