package com.example.csvexplorerjetpack.domain

import android.content.ContentResolver
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object UriFileUtil {

    fun copyToCacheFile(cr: ContentResolver, uri: Uri, cacheDir: File): File {
        val inStream = cr.openInputStream(uri) ?: error("CSV stream açılamadı")
        val outFile = File(cacheDir, "upload_${System.currentTimeMillis()}.csv")

        FileOutputStream(outFile).use { out ->
            inStream.use { input ->
                input.copyTo(out)
            }
        }
        return outFile
    }
}
