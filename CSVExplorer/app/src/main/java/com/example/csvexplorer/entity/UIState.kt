package com.example.csvexplorer.entity



import com.example.csvexplorer.entity.RowEntity

data class UiState(
    val isLoading: Boolean = false,
    val records: List<RowEntity> = emptyList(),
    val headers: List<String> = emptyList(),
    val selectedColumn: String = "ALL_COLUMNS",
    val query: String = "",
    val infoText: String = "0 records",
    val downloadUrl: String? = null,
    val errorMessage: String? = null,
    val canUpload: Boolean = false
)
