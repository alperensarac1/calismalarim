package com.alperensarac.projectmanagementauthenticator.data.remote.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName


/*
 * =========================================================
 * ORTAK API RESPONSE MODELİ
 * =========================================================
 */


/**
 * Python Authenticator servisinin kullandığı ortak API
 * cevap yapısını temsil eder.
 *
 * Başarılı cevap örneği:
 *
 * {
 *   "success": true,
 *   "message": "İşlem başarıyla tamamlandı.",
 *   "data": {
 *     ...
 *   },
 *   "errors": {}
 * }
 *
 * Hatalı cevap örneği:
 *
 * {
 *   "success": false,
 *   "message": "İşlem tamamlanamadı.",
 *   "data": null,
 *   "errors": {
 *     "field": [
 *       "Alan geçersiz."
 *     ]
 *   }
 * }
 *
 * T generic tipi endpointin döndürdüğü gerçek data
 * modelini temsil eder.
 *
 * Örnek:
 *
 * ApiResponse<ChallengeVerificationData>
 * ApiResponse<DeviceRegistrationData>
 */
data class ApiResponse<T>(

    /**
     * Sunucudaki işlemin başarılı olup olmadığını
     * belirtir.
     */
    @SerializedName("success")
    val success: Boolean = false,

    /**
     * Sunucunun kullanıcıya göstermek üzere gönderdiği
     * genel mesajdır.
     */
    @SerializedName("message")
    val message: String? = null,

    /**
     * Endpointin döndürdüğü asıl veridir.
     *
     * Hata cevaplarında null olabilir.
     */
    @SerializedName("data")
    val data: T? = null,

    /**
     * Alan bazlı veya genel doğrulama hatalarıdır.
     *
     * Python servisinden gelen errors alanının yapısı
     * endpointlere göre değişebileceği için JsonElement
     * kullanılmıştır.
     *
     * Şu biçimlerde gelebilir:
     *
     * {}
     *
     * {
     *   "email": [
     *     "E-posta geçersiz."
     *   ]
     * }
     *
     * [
     *   "İstek doğrulanamadı."
     * ]
     *
     * "Beklenmeyen hata oluştu."
     */
    @SerializedName("errors")
    val errors: JsonElement? = null,
) {

    /*
     * =====================================================
     * RESPONSE DURUM YARDIMCILARI
     * =====================================================
     */


    /**
     * Cevap başarılı ve data alanı doluysa true döndürür.
     */
    fun hasData(): Boolean {
        return (
                success &&
                        data != null
                )
    }


    /**
     * Sunucu cevabının başarısız olduğunu belirtir.
     */
    fun isFailure(): Boolean {
        return !success
    }


    /**
     * Message alanını temizleyerek döndürür.
     *
     * Message null veya boşsa null döner.
     */
    fun normalizedMessage(): String? {
        return message
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
    }


    /*
     * =====================================================
     * HATA MESAJI OLUŞTURMA
     * =====================================================
     */


    /**
     * API cevabındaki en anlamlı hata mesajını döndürür.
     *
     * Öncelik sırası:
     *
     * 1. message alanı
     * 2. errors alanı
     * 3. defaultMessage
     *
     * ChallengeRepository içerisindeki kullanım:
     *
     * responseBody.getErrorMessage(
     *     defaultMessage = "Challenge kararı işlenemedi.",
     * )
     */
    fun getErrorMessage(
        defaultMessage: String,
    ): String {
        val normalizedApiMessage =
            normalizedMessage()


        if (normalizedApiMessage != null) {
            return normalizedApiMessage
        }


        val errorMessage =
            extractErrorsAsText(
                errorsElement =
                errors,
            )


        if (!errorMessage.isNullOrBlank()) {
            return errorMessage
        }


        return defaultMessage
            .trim()
            .takeIf {
                it.isNotBlank()
            }
            ?: "İşlem tamamlanamadı."
    }


    /**
     * errors alanını kullanıcıya gösterilebilecek sade
     * bir metne dönüştürür.
     */
    private fun extractErrorsAsText(
        errorsElement: JsonElement?,
    ): String? {
        if (
            errorsElement == null ||
            errorsElement.isJsonNull
        ) {
            return null
        }


        return try {
            when {
                errorsElement.isJsonPrimitive -> {
                    errorsElement
                        .asString
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                errorsElement.isJsonArray -> {
                    errorsElement
                        .asJsonArray
                        .mapNotNull { item ->

                            extractJsonElementText(
                                element =
                                item,
                            )
                        }
                        .distinct()
                        .joinToString(
                            separator = "\n",
                        )
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                errorsElement.isJsonObject -> {
                    errorsElement
                        .asJsonObject
                        .entrySet()
                        .flatMap { entry ->

                            extractObjectEntryMessages(
                                fieldName =
                                entry.key,

                                value =
                                entry.value,
                            )
                        }
                        .distinct()
                        .joinToString(
                            separator = "\n",
                        )
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                else -> {
                    errorsElement
                        .toString()
                        .trim()
                        .takeIf {
                            it.isNotBlank() &&
                                    it != "{}" &&
                                    it != "[]"
                        }
                }
            }
        } catch (_: Exception) {
            null
        }
    }


    /**
     * errors nesnesindeki bir alanı okunabilir mesajlara
     * dönüştürür.
     *
     * Örnek:
     *
     * {
     *   "email": [
     *     "Geçersiz e-posta."
     *   ]
     * }
     *
     * şu metne dönüşür:
     *
     * email: Geçersiz e-posta.
     */
    private fun extractObjectEntryMessages(
        fieldName: String,
        value: JsonElement,
    ): List<String> {
        if (value.isJsonNull) {
            return emptyList()
        }


        val normalizedFieldName =
            fieldName
                .trim()
                .takeIf {
                    it.isNotBlank()
                }


        return when {
            value.isJsonPrimitive -> {
                val messageText =
                    extractJsonElementText(
                        element =
                        value,
                    )
                        ?: return emptyList()


                listOf(
                    joinFieldAndMessage(
                        fieldName =
                        normalizedFieldName,

                        message =
                        messageText,
                    ),
                )
            }


            value.isJsonArray -> {
                value
                    .asJsonArray
                    .mapNotNull { item ->

                        val messageText =
                            extractJsonElementText(
                                element =
                                item,
                            )
                                ?: return@mapNotNull null


                        joinFieldAndMessage(
                            fieldName =
                            normalizedFieldName,

                            message =
                            messageText,
                        )
                    }
            }


            value.isJsonObject -> {
                value
                    .asJsonObject
                    .entrySet()
                    .flatMap { nestedEntry ->

                        val nestedFieldName =
                            buildNestedFieldName(
                                parentField =
                                normalizedFieldName,

                                childField =
                                nestedEntry.key,
                            )


                        extractObjectEntryMessages(
                            fieldName =
                            nestedFieldName,

                            value =
                            nestedEntry.value,
                        )
                    }
            }


            else -> {
                val messageText =
                    value
                        .toString()
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?: return emptyList()


                listOf(
                    joinFieldAndMessage(
                        fieldName =
                        normalizedFieldName,

                        message =
                        messageText,
                    ),
                )
            }
        }
    }


    /**
     * Tek bir JsonElement değerini sade metne
     * dönüştürür.
     */
    private fun extractJsonElementText(
        element: JsonElement,
    ): String? {
        if (element.isJsonNull) {
            return null
        }


        return try {
            when {
                element.isJsonPrimitive -> {
                    element
                        .asString
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                element.isJsonObject -> {
                    element
                        .asJsonObject
                        .entrySet()
                        .flatMap { entry ->

                            extractObjectEntryMessages(
                                fieldName =
                                entry.key,

                                value =
                                entry.value,
                            )
                        }
                        .joinToString(
                            separator = "\n",
                        )
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                element.isJsonArray -> {
                    element
                        .asJsonArray
                        .mapNotNull { item ->

                            extractJsonElementText(
                                element =
                                item,
                            )
                        }
                        .joinToString(
                            separator = "\n",
                        )
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                else -> {
                    element
                        .toString()
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                }
            }
        } catch (_: Exception) {
            null
        }
    }


    /**
     * Alan adı ve hata mesajını birleştirir.
     */
    private fun joinFieldAndMessage(
        fieldName: String?,
        message: String,
    ): String {
        val normalizedMessage =
            message.trim()


        if (fieldName.isNullOrBlank()) {
            return normalizedMessage
        }


        return "$fieldName: $normalizedMessage"
    }


    /**
     * İç içe hata alanları için birleşik alan adı
     * oluşturur.
     *
     * Örnek:
     *
     * device.public_key
     */
    private fun buildNestedFieldName(
        parentField: String?,
        childField: String,
    ): String {
        val normalizedChildField =
            childField.trim()


        return if (parentField.isNullOrBlank()) {
            normalizedChildField
        } else {
            "$parentField.$normalizedChildField"
        }
    }
}


/*
 * =========================================================
 * FASTAPI STANDART HATA RESPONSE MODELİ
 * =========================================================
 */


/**
 * FastAPI tarafından HTTPException veya validation
 * hatalarında döndürülebilecek standart hata yapısını
 * temsil eder.
 *
 * Basit hata örneği:
 *
 * {
 *   "detail": "Challenge bulunamadı."
 * }
 *
 * Validation hata örneği:
 *
 * {
 *   "detail": [
 *     {
 *       "loc": [
 *         "body",
 *         "signature"
 *       ],
 *       "msg": "Field required",
 *       "type": "missing"
 *     }
 *   ]
 * }
 *
 * ChallengeRepository hata gövdesini doğrudan JSON
 * olarak okuduğu için bu model şu an zorunlu değildir.
 * Ancak diğer endpointlerde Retrofit dönüş modeli olarak
 * kullanılabilir.
 */
data class FastApiErrorResponse(

    /**
     * FastAPI detail alanı String, Array veya Object
     * olabileceği için JsonElement kullanılır.
     */
    @SerializedName("detail")
    val detail: JsonElement? = null,

    /**
     * Bazı özel hata cevaplarında message alanı da
     * bulunabilir.
     */
    @SerializedName("message")
    val message: String? = null,

    /**
     * Uygulamaya özel hata kodu.
     */
    @SerializedName("code")
    val code: String? = null,
) {

    /**
     * FastAPI hata cevabından kullanıcıya gösterilecek
     * mesajı üretir.
     */
    fun getErrorMessage(
        defaultMessage: String,
    ): String {
        val normalizedMessage =
            message
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }


        if (normalizedMessage != null) {
            return normalizedMessage
        }


        val detailMessage =
            extractDetailMessage()


        if (!detailMessage.isNullOrBlank()) {
            return detailMessage
        }


        return defaultMessage
            .trim()
            .takeIf {
                it.isNotBlank()
            }
            ?: "Sunucu isteği işleyemedi."
    }


    /**
     * FastAPI detail alanını metne dönüştürür.
     */
    private fun extractDetailMessage(): String? {
        val detailElement =
            detail
                ?: return null


        if (detailElement.isJsonNull) {
            return null
        }


        return try {
            when {
                detailElement.isJsonPrimitive -> {
                    detailElement
                        .asString
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                detailElement.isJsonArray -> {
                    detailElement
                        .asJsonArray
                        .mapNotNull { item ->

                            if (!item.isJsonObject) {
                                return@mapNotNull try {
                                    item
                                        .asString
                                        .trim()
                                        .takeIf {
                                            it.isNotBlank()
                                        }
                                } catch (_: Exception) {
                                    null
                                }
                            }


                            val errorObject =
                                item.asJsonObject


                            val errorMessage =
                                errorObject
                                    .get("msg")
                                    ?.takeUnless {
                                        it.isJsonNull
                                    }
                                    ?.asString
                                    ?.trim()
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }


                            val location =
                                errorObject
                                    .get("loc")
                                    ?.takeIf {
                                        it.isJsonArray
                                    }
                                    ?.asJsonArray
                                    ?.mapNotNull { locationItem ->

                                        try {
                                            locationItem
                                                .asString
                                                .trim()
                                                .takeIf {
                                                    it.isNotBlank()
                                                }
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }
                                    ?.joinToString(
                                        separator = ".",
                                    )
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }


                            when {
                                errorMessage == null -> {
                                    null
                                }

                                location.isNullOrBlank() -> {
                                    errorMessage
                                }

                                else -> {
                                    "$location: $errorMessage"
                                }
                            }
                        }
                        .joinToString(
                            separator = "\n",
                        )
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                detailElement.isJsonObject -> {
                    detailElement
                        .toString()
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        }
                }


                else -> {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}