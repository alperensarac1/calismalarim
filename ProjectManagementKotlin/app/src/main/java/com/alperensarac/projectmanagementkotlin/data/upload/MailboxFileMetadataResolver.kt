package com.alperensarac.projectmanagementkotlin.data.upload

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SAF tarafından verilen content:// URI'den dosya metadata'sı okur.
 *
 * Dosyanın içeriğini belleğe almaz.
 */
@Singleton
class MailboxFileMetadataResolver @Inject constructor() {

    fun resolve(
        contentResolver: ContentResolver,
        uri: Uri
    ): Result<MailboxSelectedFile> {

        return runCatching {

            val contentType =
                contentResolver.getType(uri)
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: throw IllegalArgumentException(
                        "Dosyanın içerik türü belirlenemedi."
                    )

            var fileName: String? =
                null

            var sizeBytes: Long? =
                null

            contentResolver
                .query(
                    uri,
                    arrayOf(
                        OpenableColumns.DISPLAY_NAME,
                        OpenableColumns.SIZE
                    ),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->

                    if (cursor.moveToFirst()) {

                        val nameColumn =
                            cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                            )

                        if (
                            nameColumn >= 0 &&
                            !cursor.isNull(nameColumn)
                        ) {

                            fileName =
                                cursor
                                    .getString(nameColumn)
                                    ?.trim()
                        }

                        val sizeColumn =
                            cursor.getColumnIndex(
                                OpenableColumns.SIZE
                            )

                        if (
                            sizeColumn >= 0 &&
                            !cursor.isNull(sizeColumn)
                        ) {

                            sizeBytes =
                                cursor.getLong(
                                    sizeColumn
                                )
                        }
                    }
                }

            /*
             * Bazı DocumentProvider'lar OpenableColumns.SIZE vermeyebilir.
             *
             * Bu durumda AssetFileDescriptor üzerinden ikinci kez
             * dosya boyutunu öğrenmeye çalışıyoruz.
             */
            if (
                sizeBytes == null ||
                sizeBytes!! < 0L
            ) {

                contentResolver
                    .openAssetFileDescriptor(
                        uri,
                        "r"
                    )
                    ?.use { descriptor ->

                        if (
                            descriptor.length >= 0
                        ) {

                            sizeBytes =
                                descriptor.length
                        }
                    }
            }

            val resolvedFileName =
                fileName
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: throw IllegalArgumentException(
                        "Dosya adı belirlenemedi."
                    )

            val resolvedSize =
                sizeBytes
                    ?: throw IllegalArgumentException(
                        "Dosya boyutu belirlenemedi."
                    )

            MailboxSelectedFile(
                uri = uri,
                fileName = resolvedFileName,
                contentType = contentType,
                sizeBytes = resolvedSize
            )
        }
    }
}