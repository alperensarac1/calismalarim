package com.example.csvexplorerjetpack.data


import android.content.ContentResolver
import android.net.Uri
import com.example.csvexplorerjetpack.domain.CsvImportResult
import com.example.csvexplorerjetpack.domain.DynamicCsvImporter


class CsvRepository(private val dao: RowDao) {

    suspend fun importCsv(cr: ContentResolver, uri: Uri): CsvImportResult {
        val result = DynamicCsvImporter.importCsv(cr, uri)
        dao.insertAll(result.rows)
        return result
    }

    suspend fun getAll(): List<RowEntity> = dao.getAll()

    suspend fun clear() = dao.clear()

    suspend fun filter(selected: String, q: String): List<RowEntity> {
        if (q.isBlank()) return dao.getAll()
        return if (selected == "ALL_COLUMNS") dao.searchAllColumns(q) else dao.searchInColumn(selected, q)
    }
}
