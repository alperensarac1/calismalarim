package com.example.pdfconverterjetpack.data.model

data class CreateJobResponse(
    val success: Boolean,
    val job_id: Int?,
    val message: String?
)