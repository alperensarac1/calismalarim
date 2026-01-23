package com.example.csvexplorerjetpack.viewmodel

import com.example.csvexplorerjetpack.data.RowEntity

data class UiState(
    val isLoading: Boolean = false,
    val headers: List<String> = emptyList(),
    val records: List<RowEntity> = emptyList(),
    val selectedColumn: String = "ALL_COLUMNS",
    val query: String = "",
    val infoText: String = "0 records",
    val errorMessage: String? = null,
    val canUpload: Boolean = false,
    val downloadUrl: String? = null
)
