package com.example.surusuygulamakotlin.helper

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

object UploadHelper {

    fun copyUriToTempFile(context: Context, uri: Uri): File {
        val dir = File(context.cacheDir, "upload_cache")
        if (!dir.exists()) dir.mkdirs()

        val outFile = File(dir, "upload_${System.currentTimeMillis()}.mp4")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { out ->
                val buf = ByteArray(1024 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n <= 0) break
                    out.write(buf, 0, n)
                }
                out.flush()
            }
        } ?: throw IllegalStateException("Uri okunamadı: $uri")

        return outFile
    }

    fun buildVideoPart(file: File): MultipartBody.Part {
        val mediaType = "video/mp4".toMediaType()
        val body = file.asRequestBody(mediaType)
        return MultipartBody.Part.createFormData("video", file.name, body)
    }

    fun buildPlatesParts(plates: List<String>): List<MultipartBody.Part> {
        val textType = "text/plain".toMediaType()

        val safe = if (plates.isEmpty()) listOf("") else plates

        return safe.map { plate ->
            MultipartBody.Part.createFormData(
                /* name = */ "plates_json[]",      // ✅ kritik
                /* filename = */ null,
                /* body = */ plate.toRequestBody(textType)
            )
        }
    }


}
