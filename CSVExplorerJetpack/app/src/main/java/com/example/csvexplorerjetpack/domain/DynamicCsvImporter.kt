package com.example.csvexplorerjetpack.domain


import android.content.ContentResolver
import android.net.Uri
import com.example.csvexplorerjetpack.data.RowEntity

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

data class CsvImportResult(
    val headers: List<String>,
    val rows: List<RowEntity>
)

object DynamicCsvImporter {

    suspend fun importCsv(contentResolver: ContentResolver, uri: Uri): CsvImportResult {
        val input = contentResolver.openInputStream(uri) ?: error("CSV açılamadı")
        val br = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))

        val headerLine = br.readLine() ?: return CsvImportResult(emptyList(), emptyList())
        val headers = splitCsvLine(headerLine).map { it.trim() }.filter { it.isNotEmpty() }

        val rows = mutableListOf<RowEntity>()
        var line: String?
        while (true) {
            line = br.readLine() ?: break
            val raw = line.trim()
            if (raw.isEmpty()) continue

            val values = splitCsvLine(raw)
            val obj = JSONObject()

            headers.forEachIndexed { i, key ->
                val v = values.getOrNull(i)?.trim()
                if (!v.isNullOrEmpty()) obj.put(key, v)
            }

            val externalId = guessExternalId(headers, obj)
            rows.add(RowEntity(externalId = externalId, dataJson = obj.toString()))
        }

        br.close()
        input.close()

        return CsvImportResult(headers, rows)
    }

    private fun guessExternalId(headers: List<String>, obj: JSONObject): String? {
        val candidates = listOf("id", "ID", "Id", "user_id", "uid", "pk")
        for (c in candidates) {
            if (headers.contains(c) && obj.has(c)) {
                val v = obj.optString(c, null)
                if (!v.isNullOrBlank()) return v
            }
        }
        return null
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                val next = line.getOrNull(i + 1)
                if (inQuotes && next == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString())
        return result
    }
}
