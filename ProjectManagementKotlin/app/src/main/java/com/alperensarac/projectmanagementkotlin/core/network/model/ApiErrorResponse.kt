package com.alperensarac.projectmanagementkotlin.core.network.model

import kotlinx.serialization.Serializable

/**
 * Backend'in başarısız HTTP response body modeli.
 *
 * ApiResponse ile alanları benzer olsa da hata body parse işlemini
 * daha güvenli ve bağımsız yapmak için ayrı model kullanıyoruz.
 *
 * Backend bazı hata durumlarında data alanını göndermeyebilir.
 * Bu nedenle burada data alanına ihtiyacımız yoktur.
 */
@Serializable
data class ApiErrorResponse(
    /**
     * İşlemin başarılı olup olmadığını belirtir.
     *
     * Hata response'larında genellikle false olacaktır.
     */
    val success: Boolean = false,

    /**
     * Backend tarafından döndürülen ana hata mesajıdır.
     */
    val message: String? = null,

    /**
     * Alan bazlı doğrulama hatalarıdır.
     */
    val errors: Map<String, List<String>>? = null
) {

    /**
     * Alan bazlı hataları tek bir okunabilir metne dönüştürür.
     *
     * Örnek:
     *
     * Email: Geçerli bir e-posta adresi giriniz.
     * Password: Şifre en az 8 karakter olmalıdır.
     */
    fun validationMessage(): String? {
        if (errors.isNullOrEmpty()) {
            return null
        }

        return errors.entries
            .flatMap { (field, messages) ->
                messages.map { message ->
                    "$field: $message"
                }
            }
            .joinToString(separator = "\n")
            .takeIf { it.isNotBlank() }
    }

    /**
     * Kullanıcıya gösterilecek en uygun hata mesajını döndürür.
     *
     * Öncelik:
     *
     * 1. Alan bazlı doğrulama hataları
     * 2. Backend ana mesajı
     * 3. null
     */
    fun bestMessage(): String? {
        return validationMessage()
            ?: message?.takeIf { it.isNotBlank() }
    }
}