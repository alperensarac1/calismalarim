package com.example.pdfconverterkotlin.data.model

data class ListJobsResponse(
    val success: Boolean,
    val jobs: List<JobItem>?
)

data class JobItem(
    val job_id: Int?,
    val job_type: String?,
    val status: String?,
    val source_file_url: String?,
    val result_file_url: String?,
    val error_message: String?,
    val created_at: String?
)