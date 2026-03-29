package com.example.pdfconverterkotlin.ui

data class MainUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val currentJobId: Int? = null,
    val currentJobStatus: String? = null,
    val resultFileUrl: String? = null,
    val errorText: String? = null
)