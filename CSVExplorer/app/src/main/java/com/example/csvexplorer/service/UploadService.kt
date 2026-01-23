package com.example.csvexplorer.service


import android.content.ContentResolver
import android.net.Uri
import com.example.csvexplorer.model.UploadResult
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

object UploadService {

    suspend fun uploadCsv(
        contentResolver: ContentResolver,
        fileUri: Uri,
        endpointUrl: String
    ): UploadResult {
        val boundary = "----DynamicCsvBoundary${System.currentTimeMillis()}"
        val lineEnd = "\r\n"

        val url = URL(endpointUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doInput = true
            doOutput = true
            useCaches = false

            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            setRequestProperty("Connection", "Keep-Alive")
        }

        try {
            val output = DataOutputStream(conn.outputStream)

            // Part: file
            output.writeBytes("--$boundary$lineEnd")
            output.writeBytes("""Content-Disposition: form-data; name="file"; filename="data.csv"$lineEnd""")
            output.writeBytes("Content-Type: text/csv$lineEnd")
            output.writeBytes(lineEnd)

            // file bytes
            contentResolver.openInputStream(fileUri).use { input ->
                requireNotNull(input) { "Dosya açılamadı" }
                val buffer = ByteArray(8192)
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                }
            }

            output.writeBytes(lineEnd)
            output.writeBytes("--$boundary--$lineEnd")
            output.flush()
            output.close()

            val code = conn.responseCode
            val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader(Charsets.UTF_8)
                ?.readText()
                .orEmpty()

            if (code !in 200..299) {
                return UploadResult(false, null, "HTTP $code: $body")
            }

            val json = JSONObject(body)
            val ok = json.optBoolean("ok", false)
            if (!ok) return UploadResult(false, null, json.optString("error", "unknown error"))

            val downloadUrl = json.optString("download_url", null)
            return UploadResult(true, downloadUrl, null)

        } catch (e: Exception) {
            return UploadResult(false, null, e.message ?: "upload error")
        } finally {
            conn.disconnect()
        }
    }
}
