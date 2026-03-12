package com.example.yardimuygulamakotlin.model

// bodies.kt
data class RegisterBody(
    val role: String,
    val ad: String,
    val soyad: String,
    val yas: Int? = null,
    val telefon: String,
    val il: String? = null,
    val ilce: String,
    val email: String? = null,
    val sifre: String
)

data class LoginBody(
    val telefon: String,
    val sifre: String
)

data class HelpCreateBody(
    val patient_id: Long,
    val hastane_adi: String? = null,
    val servis_adi: String,
    val oda_no: String,
    val lat: Double,
    val lng: Double
)

data class HelpAcceptBody(
    val request_id: Long,
    val helper_id: Long
)

data class HelpConfirmBody(
    val request_id: Long,
    val patient_id: Long
)
data class ConfirmedHelpItem(
    val id: Long,
    val patient_id: Long,
    val patient_name: String,
    val patient_age: Int?,
    val patient_phone: String,

    val hastane_adi: String?,
    val servis_adi: String,
    val oda_no: String,

    val lat: Double,
    val lng: Double,
    val ilce: String,

    val accepted_at: String?,
    val confirmed_at: String?,
    val created_at: String
)