package com.example.csvexplorer.model

import com.example.csvexplorer.entity.RowEntity

data class CsvImportResult(
    val headers: List<String>,
    val rows: List<RowEntity>
)