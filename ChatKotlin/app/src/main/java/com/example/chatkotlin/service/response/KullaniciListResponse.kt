package com.example.chatkotlin.service.response

import com.example.chatkotlin.model.Kullanici

data class KullaniciListResponse(
    val success: Boolean,
    val kullanicilar: List<Kullanici>
)
