package com.alperensarac.projectmanagementkotlin.data.download

import android.content.ContentResolver
import android.net.Uri
import com.alperensarac.projectmanagementkotlin.data.remote.api.MailboxApi
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mailbox attachment'larını Android SAF tarafından verilen URI'ye
 * stream ederek kaydeder.
 *
 * Neden ByteArray kullanmıyoruz?
 *
 * Çünkü attachment büyük olabilir.
 *
 * Örneğin:
 *
 * 150 MB dosya
 *
 * responseBody.bytes()
 *
 * ile okunursa dosyanın tamamı RAM'e alınabilir.
 *
 * Bunun yerine:
 *
 * Network InputStream
 *        ↓
 *      Buffer
 *        ↓
 * ContentResolver OutputStream
 *
 * şeklinde dosyayı parça parça kopyalıyoruz.
 */
@Singleton
class MailboxAttachmentDownloader @Inject constructor(
    private val mailboxApi: MailboxApi
) {

    /**
     * Attachment'ı backend'den indirip kullanıcının SAF üzerinden
     * seçtiği hedef URI'ye kaydeder.
     *
     * @param messageId Mesaj ID.
     * @param attachmentId Attachment ID.
     * @param destinationUri Android tarafından verilen hedef URI.
     * @param contentResolver URI üzerinde yazma işlemi yapmak için kullanılır.
     * @param onProgress 0..100 arasındaki indirme yüzdesini bildirir.
     */
    suspend fun download(
        messageId: Int,
        attachmentId: Int,
        destinationUri: Uri,
        contentResolver: ContentResolver,
        onProgress: (Int) -> Unit
    ): MailboxDownloadResult {

        // ---------------------------------------------------------------------
        // BASIC VALIDATION
        // ---------------------------------------------------------------------

        if (
            messageId <= 0 ||
            attachmentId <= 0
        ) {

            return MailboxDownloadResult.Error(
                message =
                "Geçersiz mesaj veya dosya numarası."
            )
        }

        /*
         * Network ve disk işlemlerini Main Thread üzerinde yapmıyoruz.
         *
         * Generic tipi açıkça MailboxDownloadResult veriyoruz.
         *
         * Böylece withContext içerisindeki HER branch'in
         * MailboxDownloadResult döndürmesi gerektiği compiler tarafından
         * kesin olarak bilinir.
         */
        return withContext<MailboxDownloadResult>(
            Dispatchers.IO
        ) {

            try {

                // =============================================================
                // 1. NETWORK REQUEST
                // =============================================================

                val response =
                    mailboxApi.downloadAttachment(
                        messageId =
                        messageId,

                        attachmentId =
                        attachmentId
                    )

                /*
                 * Retrofit Response<ResponseBody> kullanıyoruz çünkü
                 * endpoint JSON ApiResponse<T> değil doğrudan binary
                 * FileStreamResult döndürüyor.
                 */
                if (!response.isSuccessful) {

                    return@withContext MailboxDownloadResult.Error(
                        message =
                        "Dosya indirilemedi. HTTP ${response.code()}"
                    )
                }

                // =============================================================
                // 2. RESPONSE BODY
                // =============================================================

                /*
                 * BURASI ÖNCEKİ HATANIN OLDUĞU YERLERDEN BİRİYDİ.
                 *
                 * Yanlış:
                 *
                 * ?: return@withContext
                 * MailboxDownloadResult.Error(...)
                 *
                 * Doğru:
                 *
                 * ?: return@withContext MailboxDownloadResult.Error(...)
                 */
                val body =
                    response.body()
                        ?: return@withContext MailboxDownloadResult.Error(
                            message =
                            "Sunucudan dosya içeriği alınamadı."
                        )

                /*
                 * Sunucu Content-Length gönderiyorsa toplam dosya
                 * boyutunu buradan öğrenebiliriz.
                 *
                 * Bilinmiyorsa OkHttp -1 döndürebilir.
                 */
                val totalBytes =
                    body.contentLength()

                // =============================================================
                // 3. DESTINATION OUTPUT STREAM
                // =============================================================

                /*
                 * Kullanıcının CreateDocument ile seçtiği URI üzerinde
                 * OutputStream açıyoruz.
                 */
                val outputStream =
                    contentResolver
                        .openOutputStream(
                            destinationUri,
                            "w"
                        )
                        ?: return@withContext MailboxDownloadResult.Error(
                            message =
                            "Dosya kayıt konumu açılamadı."
                        )

                // =============================================================
                // 4. STREAM COPY
                // =============================================================

                /*
                 * responseBody.bytes() YOK.
                 *
                 * readBytes() YOK.
                 *
                 * Çünkü bunlar dosyanın tamamını belleğe alabilir.
                 */
                body.byteStream()
                    .use { input ->

                        outputStream
                            .use { output ->

                                /*
                                 * Yalnızca 64 KB RAM buffer kullanıyoruz.
                                 */
                                val buffer =
                                    ByteArray(
                                        BUFFER_SIZE
                                    )

                                var downloadedBytes =
                                    0L

                                /*
                                 * Aynı yüzdeyi sürekli UI'ya göndermemek için
                                 * son bildirilen progress değerini tutuyoruz.
                                 */
                                var lastProgress =
                                    -1

                                while (true) {

                                    // -------------------------------------------------
                                    // READ
                                    // -------------------------------------------------

                                    val readCount =
                                        input.read(
                                            buffer
                                        )

                                    /*
                                     * -1:
                                     *
                                     * InputStream sonuna gelindi.
                                     */
                                    if (
                                        readCount ==
                                        -1
                                    ) {
                                        break
                                    }

                                    // -------------------------------------------------
                                    // WRITE
                                    // -------------------------------------------------

                                    output.write(
                                        buffer,
                                        0,
                                        readCount
                                    )

                                    downloadedBytes +=
                                        readCount.toLong()

                                    // -------------------------------------------------
                                    // PROGRESS
                                    // -------------------------------------------------

                                    if (
                                        totalBytes >
                                        0L
                                    ) {

                                        /*
                                         * Önce Long olarak hesaplıyoruz.
                                         *
                                         * Böylece büyük dosyalarda Int
                                         * hesaplamasına göre daha güvenli olur.
                                         */
                                        val progress =
                                            (
                                                    downloadedBytes *
                                                            100L /
                                                            totalBytes
                                                    )
                                                .toInt()
                                                .coerceIn(
                                                    0,
                                                    100
                                                )

                                        /*
                                         * Progress değiştiğinde UI'ya bildir.
                                         */
                                        if (
                                            progress !=
                                            lastProgress
                                        ) {

                                            lastProgress =
                                                progress

                                            onProgress(
                                                progress
                                            )
                                        }
                                    }
                                }

                                /*
                                 * Buffered writer olmasa bile output'ta
                                 * bekleyen verilerin hedefe aktarılmasını
                                 * garanti altına alıyoruz.
                                 */
                                output.flush()
                            }
                    }

                // =============================================================
                // 5. COMPLETE
                // =============================================================

                /*
                 * Content-Length bilinmese dahi dosyanın tamamını okuyup
                 * yazdıysak son olarak %100 gönderiyoruz.
                 */
                onProgress(
                    100
                )

                /*
                 * try bloğunun son expression'ı.
                 *
                 * withContext lambda'sının sonucu olarak döner.
                 */
                MailboxDownloadResult.Success

            } catch (
                exception: IOException
            ) {

                /*
                 * Network stream veya dosya yazma kaynaklı IO hataları.
                 */
                MailboxDownloadResult.Error(
                    message =
                    exception.message
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "Dosya indirilirken veya kaydedilirken bir hata oluştu."
                )

            } catch (
                throwable: Throwable
            ) {

                /*
                 * Beklenmeyen diğer hatalar.
                 */
                MailboxDownloadResult.Error(
                    message =
                    throwable.message
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "Dosya indirilemedi."
                )
            }
        }
    }

    private companion object {

        /**
         * 64 KB buffer.
         *
         * Dosya:
         *
         * 5 MB
         * 50 MB
         * 200 MB
         *
         * olsa bile tüm dosya RAM'e yüklenmez.
         */
        const val BUFFER_SIZE =
            64 * 1024
    }
}