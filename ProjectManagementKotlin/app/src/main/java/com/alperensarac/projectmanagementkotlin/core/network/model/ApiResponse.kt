package com.alperensarac.projectmanagementkotlin.core.network.model

import kotlinx.serialization.Serializable


@Serializable
data class ApiResponse<T>(
    /**
     * Backend işleminin başarılı olup olmadığını belirtir.
     *
     * HTTP status kodu 200 olsa bile backend success=false döndürebilir.
     * Bu nedenle repository katmanında hem HTTP status kodu hem de bu alan
     * kontrol edilmelidir.
     */
    val success: Boolean,

    /**
     * Kullanıcıya veya geliştiriciye gösterilebilecek genel mesajdır.
     */
    val message: String,

    /**
     * Response içerisindeki asıl veridir.
     *
     * Başarısız işlemlerde veya veri dönmeyen endpointlerde null olabilir.
     */
    val data: T? = null,

    /**
     * Backend doğrulama hatalarını alan bazlı döndürür.
     *
     * Örnek:
     *
     * {
     *   "email": [
     *     "E-posta alanı zorunludur.",
     *     "Geçerli bir e-posta adresi giriniz."
     *   ]
     * }
     */
    val errors: Map<String, List<String>>? = null
)