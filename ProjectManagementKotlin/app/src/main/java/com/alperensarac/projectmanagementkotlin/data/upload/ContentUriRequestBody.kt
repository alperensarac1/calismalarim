package com.alperensarac.projectmanagementkotlin.data.upload

import android.content.ContentResolver
import android.net.Uri
import java.io.IOException
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

/**
 * content:// URI içeriğini doğrudan OkHttp RequestBody'ye
 * stream eder.
 *
 * Dosyanın tamamı RAM'e alınmaz.
 */
class ContentUriRequestBody(
    private val contentResolver: ContentResolver,
    private val uri: Uri,
    private val contentType: MediaType,
    private val contentLength: Long,
    private val onBytesWritten: (Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType =
        contentType

    /**
     * ASP.NET Core IFormFile.Length için dosyanın boyutunu
     * önceden bildiriyoruz.
     */
    override fun contentLength(): Long =
        contentLength

    @Throws(IOException::class)
    override fun writeTo(
        sink: BufferedSink
    ) {

        val inputStream =
            contentResolver
                .openInputStream(
                    uri
                )
                ?: throw IOException(
                    "Dosya içeriği açılamadı."
                )

        inputStream.use { input ->

            val buffer =
                ByteArray(
                    BUFFER_SIZE
                )

            while (true) {

                val readCount =
                    input.read(
                        buffer
                    )

                if (
                    readCount ==
                    -1
                ) {
                    break
                }

                sink.write(
                    buffer,
                    0,
                    readCount
                )

                /*
                 * Her parçada kaç byte yazıldığını upload progress
                 * hesaplayıcısına bildiriyoruz.
                 */
                onBytesWritten(
                    readCount.toLong()
                )
            }
        }
    }

    private companion object {

        const val BUFFER_SIZE =
            64 * 1024
    }
}