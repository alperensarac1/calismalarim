package com.example.surusuygulamakotlin.model

data class ApiResp<T>(
    val ok: Boolean,
    val message: String,
    val data: T?
)

data class UploadData(
    val report_id: Long,
    val video_url: String,
    val sent_at: String,
    val ip: String,
    val plates_saved: List<String>
)
