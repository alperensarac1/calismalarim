package com.example.yardimuygulamajetpack.model

data class OpenHelpItem(
    val id: Long,
    val patient_name: String?,
    val patient_age: Int?,
    val created_at: String?
)

data class AcceptedHelpItem(
    val id: Long,
    val patient_id: Long,
    val patient_name: String?,
    val patient_age: Int?,
    val patient_phone: String?,
    val servis_adi: String?,
    val oda_no: String?,
    val lat: Double,
    val lng: Double,
    val remaining_seconds: Int?
)

data class ConfirmedHelpItem(
    val id: Long,
    val patient_name: String?,
    val patient_phone: String?,
    val servis_adi: String?,
    val oda_no: String?,
    val confirmed_at: String?
)

data class HelpActive(
    val id: Long,
    val status: String,
    val remaining_seconds: Int? = null
)