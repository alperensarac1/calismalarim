package com.alperensarac.projectmanagementkotlin.core.network.parser

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiErrorResponse
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * Retrofit, OkHttp ve Kotlin Serialization tarafından oluşturulan
 * exception nesnelerini uygulamanın NetworkError modeline dönüştürür.
 *
 * Bu sınıf sayesinde repository sınıflarında aynı try-catch kodlarını
 * tekrar tekrar yazmak zorunda kalmayacağız.
 */
@Singleton
class NetworkErrorMapper @Inject constructor(
    private val json: Json
) {

    /**
     * Gelen Throwable nesnesini uygulamanın anlayacağı NetworkError
     * modeline dönüştürür.
     */
    fun map(throwable: Throwable): NetworkError {
        return when (throwable) {
            is HttpException -> mapHttpException(throwable)

            is SocketTimeoutException -> {
                NetworkError.Timeout(
                    message = "Sunucu yanıt vermedi. Lütfen tekrar deneyin."
                )
            }

            is SerializationException -> {
                NetworkError.Serialization(
                    message = "Sunucudan gelen cevap okunamadı."
                )
            }

            is IOException -> {
                /*
                 * OkHttp bağlantı, DNS, ağ kopması ve stream hatalarının
                 * büyük bölümünü IOException olarak bildirir.
                 */
                NetworkError.NoConnection(
                    message = "Sunucuya bağlanılamadı. Ağ bağlantınızı kontrol edin."
                )
            }

            else -> {
                NetworkError.Unknown(
                    message = throwable.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Beklenmeyen bir hata oluştu.",
                    throwable = throwable
                )
            }
        }
    }

    /**
     * Retrofit HttpException nesnesini HTTP status koduna göre
     * uygulama hata modeline dönüştürür.
     */
    private fun mapHttpException(
        exception: HttpException
    ): NetworkError {
        val statusCode = exception.code()
        val apiError = parseErrorBody(exception)

        val backendMessage = apiError?.bestMessage()

        return when (statusCode) {
            400 -> {
                val fieldErrors = apiError?.errors.orEmpty()

                if (fieldErrors.isNotEmpty()) {
                    NetworkError.Validation(
                        message = backendMessage
                            ?: "Gönderilen bilgileri kontrol edin.",
                        fieldErrors = fieldErrors
                    )
                } else {
                    NetworkError.Unknown(
                        message = backendMessage
                            ?: "Gönderilen bilgiler geçersiz."
                    )
                }
            }

            401 -> {
                NetworkError.Unauthorized(
                    message = backendMessage
                        ?: "Oturum süreniz doldu. Lütfen tekrar giriş yapın."
                )
            }

            403 -> {
                NetworkError.Forbidden(
                    message = backendMessage
                        ?: "Bu işlemi gerçekleştirme yetkiniz bulunmuyor."
                )
            }

            404 -> {
                NetworkError.NotFound(
                    message = backendMessage
                        ?: "İstenen kayıt bulunamadı."
                )
            }

            409 -> {
                NetworkError.Conflict(
                    message = backendMessage
                        ?: "İşlem mevcut bir kayıtla çakışıyor."
                )
            }

            413 -> {
                NetworkError.PayloadTooLarge(
                    message = backendMessage
                        ?: "Gönderilen dosya veya veri izin verilen boyuttan büyük."
                )
            }

            415 -> {
                NetworkError.UnsupportedMediaType(
                    message = backendMessage
                        ?: "Gönderilen dosya veya veri türü desteklenmiyor."
                )
            }

            429 -> {
                NetworkError.TooManyRequests(
                    message = backendMessage
                        ?: "Çok fazla istek gönderdiniz. Lütfen daha sonra tekrar deneyin."
                )
            }

            in 500..599 -> {
                NetworkError.Server(
                    message = backendMessage
                        ?: "Sunucuda beklenmeyen bir hata oluştu.",
                    statusCode = statusCode
                )
            }

            else -> {
                NetworkError.Unknown(
                    message = backendMessage
                        ?: "İstek tamamlanamadı. Hata kodu: $statusCode",
                    throwable = exception
                )
            }
        }
    }

    /**
     * Backend'in hata response body içeriğini ApiErrorResponse modeline
     * dönüştürmeye çalışır.
     *
     * Parse işlemi başarısız olursa null döndürür. Hata mapper'ın kendisi
     * yeni bir exception oluşturmamalıdır.
     */
    private fun parseErrorBody(
        exception: HttpException
    ): ApiErrorResponse? {
        val errorBody = exception.response()
            ?.errorBody()
            ?.string()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return runCatching {
            json.decodeFromString<ApiErrorResponse>(errorBody)
        }.getOrNull()
    }
}