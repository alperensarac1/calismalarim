package com.example.yardimuygulamajetpack.model

data class LoginBody(val telefon: String, val sifre: String)

data class RegisterBody(
    val role: String,
    val ad: String,
    val soyad: String,
    val yas: Int?,
    val telefon: String,
    val il: String,
    val ilce: String,
    val sifre: String
)

data class HelpCreateBody(
    val patient_id: Long,
    val servis_adi: String,
    val oda_no: String,
    val lat: Double,
    val lng: Double
)
data class HelpConfirmBody(val request_id: Long, val patient_id: Long)
data class HelpCancelBody(val request_id: Long, val patient_id: Long)
data class HelpAcceptBody(val request_id: Long, val helper_id: Long)