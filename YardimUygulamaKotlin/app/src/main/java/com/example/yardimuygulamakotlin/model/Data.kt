package com.example.yardimuygulamakotlin.model

data class ApiOk<T>(
    val ok: Boolean,
    val error: String? = null,
    val count: Int? = null,
    val items: List<T>? = null,
    val user: User? = null,
    val request: HelpRequestShort? = null,
    val accepted: Boolean? = null,
    val confirmed: Boolean? = null,
    val active: HelpRequestActive? = null,
    val helper_ilce: String? = null,
    val expires_in_minutes: Int? = null
)

data class User(
    val id: Long,
    val role: String,
    val ad: String,
    val soyad: String,
    val yas: Int?,
    val telefon: String,
    val il: String?,
    val ilce: String,
    val email: String?
)

// help_list_open.php response item (servis/oda yok!)
data class OpenHelpItem(
    val id: Long,
    val patient_id: Long,
    val patient_name: String,
    val patient_age: Int?,
    val lat: Double,
    val lng: Double,
    val ilce: String,
    val created_at: String
)

// help_create.php response "request"
data class HelpRequestShort(
    val id: Long,
    val status: String,
    val ilce: String
)

data class AcceptedHelpItem(
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
    val expires_at: String?,
    val created_at: String,

    val remaining_seconds: Int // ✅ eklendi
)
data class HelpCancelBody(
    val request_id: Long,
    val patient_id: Long
)
data class HelpRequestActive(
    val id: Long,
    val status: String,
    val hastane_adi: String?,
    val servis_adi: String,
    val oda_no: String,
    val lat: Double,
    val lng: Double,
    val ilce: String,
    val accepted_helper_id: Long?,
    val accepted_at: String?,
    val expires_at: String?,
    val created_at: String,

    val remaining_seconds: Int? // ✅ eklendi (ACCEPTED iken dolu)
)
