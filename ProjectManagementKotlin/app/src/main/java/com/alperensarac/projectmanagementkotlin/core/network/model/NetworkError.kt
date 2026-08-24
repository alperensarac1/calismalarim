package com.alperensarac.projectmanagementkotlin.core.network.model

/**
 * Uygulamanın ağ ve API hata modelidir.
 *
 * Retrofit veya OkHttp sınıflarını UI katmanına taşımamak için
 * kendi domain'e yakın hata tipimizi oluşturuyoruz.
 *
 * ViewModel ve Fragment katmanları HttpException gibi Retrofit
 * sınıflarını bilmek zorunda kalmayacaktır.
 */
sealed interface NetworkError {

    /**
     * Backend tarafından döndürülen alan bazlı doğrulama hatası.
     *
     * Genellikle HTTP 400 durumunda oluşur.
     */
    data class Validation(
        val message: String,
        val fieldErrors: Map<String, List<String>>
    ) : NetworkError

    /**
     * Kullanıcının oturumu geçersiz veya access token süresi dolmuş.
     *
     * HTTP 401.
     */
    data class Unauthorized(
        val message: String
    ) : NetworkError

    /**
     * Kullanıcı oturum açmış ancak ilgili işlemi yapmaya yetkili değil.
     *
     * HTTP 403.
     */
    data class Forbidden(
        val message: String
    ) : NetworkError

    /**
     * İstenen kayıt bulunamadı.
     *
     * HTTP 404.
     */
    data class NotFound(
        val message: String
    ) : NetworkError

    /**
     * Mevcut kayıt veya işlem ile çakışma oluştu.
     *
     * HTTP 409.
     */
    data class Conflict(
        val message: String
    ) : NetworkError

    /**
     * Gönderilen dosya veya request body izin verilen boyuttan büyük.
     *
     * HTTP 413.
     */
    data class PayloadTooLarge(
        val message: String
    ) : NetworkError

    /**
     * Gönderilen Content-Type backend tarafından desteklenmiyor.
     *
     * HTTP 415.
     */
    data class UnsupportedMediaType(
        val message: String
    ) : NetworkError

    /**
     * Kısa süre içerisinde çok fazla istek gönderildi.
     *
     * HTTP 429.
     */
    data class TooManyRequests(
        val message: String
    ) : NetworkError

    /**
     * Backend tarafında beklenmeyen sunucu hatası oluştu.
     *
     * HTTP 500 ve üzeri.
     */
    data class Server(
        val message: String,
        val statusCode: Int
    ) : NetworkError

    /**
     * İnternet veya yerel ağ bağlantısı bulunmuyor.
     */
    data class NoConnection(
        val message: String
    ) : NetworkError

    /**
     * İstek belirtilen süre içerisinde tamamlanamadı.
     */
    data class Timeout(
        val message: String
    ) : NetworkError

    /**
     * JSON parse hatası veya beklenmeyen response formatı.
     */
    data class Serialization(
        val message: String
    ) : NetworkError

    /**
     * Yukarıdaki kategorilerden hiçbirine girmeyen beklenmeyen hata.
     */
    data class Unknown(
        val message: String,
        val throwable: Throwable? = null
    ) : NetworkError
}