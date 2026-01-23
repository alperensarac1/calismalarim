package com.example.csvexplorer.usecases

import android.content.ContentResolver
import android.net.Uri
import com.example.csvexplorer.entity.RowEntity
import com.example.csvexplorer.model.CsvImportResult
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


object DynamicCSVImporter {

    suspend fun import(contentResolver: ContentResolver, uri: Uri): CsvImportResult {
        contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "CSV açılamadı" }

            BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { br ->
                val headerLine = br.readLine() ?: return CsvImportResult(emptyList(), emptyList())
                val headers = splitCsvLine(headerLine).map { it.trim() }.filter { it.isNotBlank() }

                val out = ArrayList<RowEntity>()
                var line: String?

                while (true) {
                    line = br.readLine() ?: break
                    val raw = line!!.trim()
                    if (raw.isBlank()) continue

                    val values = splitCsvLine(raw)
                    val obj = JSONObject()

                    for (i in headers.indices) {
                        val key = headers[i]
                        val value = values.getOrNull(i)?.trim()

                        // boşları yazmak zorunda değilsin (isteğe bağlı)
                        if (!value.isNullOrEmpty()) obj.put(key, value)
                    }

                    val externalId = guessExternalId(headers, obj)

                    out.add(
                        RowEntity(
                            externalId = externalId,
                            dataJson = obj.toString()
                        )
                    )
                }

                return CsvImportResult(headers, out)
            }
        }
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
        val result = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    val next = if (i + 1 < line.length) line[i + 1] else null
                    if (inQuotes && next == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.setLength(0)
                }
                else -> sb.append(c)
            }
            i++
        }

        result.add(sb.toString())
        return result
    }
}
