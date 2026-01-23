package com.example.csvexplorerjetpack.service


import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object UploadClient {

    private val client = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Sunucu cevap formatı değişebilir.
     * Biz şu anahtarları sırayla deneriz:
     * - download_url
     * - url
     * - file_url
     * - link
     * JSON değilse: body içinden ilk http(s) linkini çekmeye çalışır.
     */
    fun uploadCsv(endpoint: String, file: File): String {
        val csvType = "text/csv".toMediaType()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            // PHP tarafında genelde "file" veya "csv" beklenir. En yaygını "file".
            .addFormDataPart("file", file.name, file.asRequestBody(csvType))
            .build()

        val req = Request.Builder()
            .url(endpoint)
            .post(body)
            .build()

        client.newCall(req).execute().use { res ->
            val text = res.body?.string().orEmpty()
            if (!res.isSuccessful) error("Upload failed (${res.code}): $text")

            // JSON parse dene
            val urlFromJson = runCatching {
                val o = JSONObject(text)
                o.optString("download_url")
                    .ifBlank { o.optString("url") }
                    .ifBlank { o.optString("file_url") }
                    .ifBlank { o.optString("link") }
                    .ifBlank { "" }
            }.getOrDefault("")

            if (urlFromJson.isNotBlank()) return urlFromJson

            // JSON değilse veya url alanı yoksa: metinden link ayıkla
            val regex = Regex("""https?://\S+""")
            val m = regex.find(text)?.value
            if (!m.isNullOrBlank()) return m

            // Hiçbir şey bulamazsak ham cevabı gösterelim
            error("Server response parsed, but no download URL found. Response: $text")
        }
    }
}
