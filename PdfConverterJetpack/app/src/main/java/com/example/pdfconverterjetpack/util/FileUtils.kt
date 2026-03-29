package com.example.pdfconverterjetpack.util

import com.example.pdfconverterjetpack.data.model.CreateJobResponse
import com.example.pdfconverterjetpack.data.model.JobStatusResponse
import com.example.pdfconverterjetpack.data.model.ListJobsResponse
import com.example.pdfconverterjetpack.data.remote.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.FileOutputStream

object FileUtils {

    /**
     * Uri üzerinden görünen dosya adını almaya çalışır.
     * Alamazsa zaman bazlı geçici bir isim üretir.
     */
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "file_${System.currentTimeMillis()}"

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                val value = it.getString(nameIndex)
                if (!value.isNullOrBlank()) {
                    fileName = value
                }
            }
        }

        return fileName
    }

    /**
     * Uri içeriğini app cache klasörüne kopyalar.
     * Retrofit upload tarafında File kullanmak için çok uygundur.
     */
    fun copyUriToFile(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri)
            val tempFile = File(context.cacheDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Çoklu seçimde gelen Uri listesini File listesine çevirir.
     */
    fun copyUrisToFiles(context: Context, uris: List<Uri>): List<File> {
        return uris.mapNotNull { copyUriToFile(context, it) }
    }
}