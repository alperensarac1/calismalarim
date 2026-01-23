package com.example.csvexplorer.repo


import android.content.ContentResolver
import android.net.Uri
import com.example.csvexplorer.entity.HeadersStore
import com.example.csvexplorer.service.AppDB
import com.example.csvexplorer.service.UploadService
import com.example.csvexplorer.usecases.DynamicCSVImporter

class CsvRepository(
    private val db: AppDB
) {
    private val dao = db.rowDao()

    suspend fun importCsv(contentResolver: ContentResolver, uri: Uri): Pair<List<String>, Int> {
        val result = DynamicCSVImporter.import(contentResolver, uri)
        dao.insertAll(result.rows)
        return result.headers to result.rows.size
    }

    suspend fun getAll() = dao.getAll()
    suspend fun clear() = dao.clear()

    suspend fun filter(selected: String, q: String): List<com.example.csvexplorer.entity.RowEntity> {
        if (q.isBlank()) return dao.getAll()
        return if (selected == "ALL_COLUMNS") dao.searchAllColumns(q)
        else dao.searchInColumn(selected, q)
    }

    suspend fun uploadCsv(contentResolver: ContentResolver, uri: Uri, endpoint: String) =
        UploadService.uploadCsv(contentResolver, uri, endpoint)
}
