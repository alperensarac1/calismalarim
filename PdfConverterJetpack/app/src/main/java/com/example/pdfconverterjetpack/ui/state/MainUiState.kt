package com.example.pdfconverterjetpack.ui.state

data class MainUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val currentJobId: Int? = null,
    val currentJobStatus: String? = null,
    val resultFileUrl: String? = null,
    val errorText: String? = null
)