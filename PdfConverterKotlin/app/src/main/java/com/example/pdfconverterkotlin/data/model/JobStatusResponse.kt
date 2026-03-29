package com.example.pdfconverterkotlin.data.model

data class JobStatusResponse(
    val success: Boolean,
    val job_id: Int?,
    val job_type: String?,
    val status: String?,
    val error_message: String?,
    val created_at: String?,
    val updated_at: String?,
    val source_file_url: String?,
    val result_file_url: String?
)