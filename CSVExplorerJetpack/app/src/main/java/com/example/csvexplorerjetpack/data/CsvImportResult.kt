package com.example.csvexplorerjetpack.data

data class CsvImportResult(
    val headers: List<String>,
    val rows: List<RowEntity>
)
