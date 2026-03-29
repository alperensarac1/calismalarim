package com.example.pdfconverterkotlin.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    /**
     * Uri'den görünen dosya adını almaya çalışır.
     * Eğer alamazsa zaman bazlı bir isim üretir.
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
     * Uri içeriğini cache klasörüne kopyalar ve File döner.
     * Retrofit'e verebilmek için en pratik yöntemlerden biridir.
     */
    fun copyUriToFile(context: Context, uri: Uri): File? {
        return try {
            val fileName = getFileName(context, uri)
            val tempFile = File(context.cacheDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Birden fazla Uri gelirse hepsini File listesine çevirir.
     */
    fun copyUrisToFiles(context: Context, uris: List<Uri>): List<File> {
        val files = mutableListOf<File>()

        uris.forEach { uri ->
            val file = copyUriToFile(context, uri)
            if (file != null) {
                files.add(file)
            }
        }

        return files
    }
}