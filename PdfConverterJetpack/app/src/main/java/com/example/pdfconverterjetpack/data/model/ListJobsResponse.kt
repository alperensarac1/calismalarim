package com.example.pdfconverterjetpack.data.model

data class ListJobsResponse(
    val success: Boolean,
    val jobs: List<JobItem>?
)