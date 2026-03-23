package com.example.surusuygulamakotlin.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.surusuygulamakotlin.helper.UploadHelper
import com.example.surusuygulamakotlin.service.ApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class UploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val videoUriStr = inputData.getString(KEY_VIDEO_URI) ?: return Result.failure()
        val platesJson = inputData.getString(KEY_PLATES_JSON) ?: "[]"
        val clientSentAt = inputData.getString(KEY_CLIENT_SENT_AT) ?: nowIstanbul()

        // ✅ Konum ZORUNLU
        val lat = inputData.getDouble(KEY_DEVICE_LAT, Double.NaN)
        val lng = inputData.getDouble(KEY_DEVICE_LNG, Double.NaN)
        val acc = inputData.getFloat(KEY_DEVICE_ACC, Float.NaN)
        val locAt = inputData.getString(KEY_DEVICE_LOC_AT)

        if (lat.isNaN() || lng.isNaN() || acc.isNaN() || locAt.isNullOrBlank()) {
            // Konum yoksa backend zaten reddedecek -> boş yere retry etme
            return Result.failure()
        }

        val videoUri = Uri.parse(videoUriStr)

        return try {
            // Uri -> temp file
            val tempFile = UploadHelper.copyUriToTempFile(applicationContext, videoUri)
            val videoPart = UploadHelper.buildVideoPart(tempFile)

            // plates_json string -> List<String>
            val plates: List<String> = parsePlatesJson(platesJson)
            val safePlates = if (plates.isNotEmpty()) plates else listOf("UNKNOWN")
            val platesJsonStr = Gson().toJson(safePlates)

            val textType = "text/plain".toMediaType()

            val resp = ApiClient.api.uploadReport(
                video = videoPart,
                platesJson = platesJsonStr.toRequestBody(textType),
                clientSentAt = clientSentAt.toRequestBody(textType),

                // ✅ Yeni alanlar
                deviceLat = lat.toString().toRequestBody(textType),
                deviceLng = lng.toString().toRequestBody(textType),
                deviceAcc = acc.toString().toRequestBody(textType),
                deviceLocAt = locAt.toRequestBody(textType)
            )

            try { tempFile.delete() } catch (_: Throwable) {}

            if (resp.ok) {
                Result.success()
            } else {
                // Kalıcı hatalarda retry etme
                val msg = (resp.message ?: "").lowercase(Locale.US)
                if (
                    msg.contains("format") ||
                    msg.contains("gerekli") ||
                    msg.contains("mime") ||
                    msg.contains("array olmalı") ||
                    msg.contains("geçerli plaka") ||
                    msg.contains("konum") ||
                    msg.contains("location")
                ) Result.failure()
                else Result.retry()
            }
        } catch (_: Throwable) {
            Result.retry()
        }
    }

    private fun parsePlatesJson(json: String): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            val list: List<String> = Gson().fromJson(json, type) ?: emptyList()
            list.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun nowIstanbul(): String {
        val tz = TimeZone.getTimeZone("Europe/Istanbul")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = tz
        return sdf.format(Date())
    }

    companion object {
        const val KEY_VIDEO_URI = "video_uri"
        const val KEY_PLATES_JSON = "plates_json"
        const val KEY_CLIENT_SENT_AT = "client_sent_at"

        // ✅ Konum inputları
        const val KEY_DEVICE_LAT = "device_lat"
        const val KEY_DEVICE_LNG = "device_lng"
        const val KEY_DEVICE_ACC = "device_acc"
        const val KEY_DEVICE_LOC_AT = "device_loc_at"
    }
}
