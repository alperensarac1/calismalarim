package com.alperensarac.projectmanagementauthenticator.security

import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeDecision
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeSigningInput
import com.alperensarac.projectmanagementauthenticator.data.remote.model.ChallengeSigningInputValidationResult

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


/*
 * =========================================================
 * CHALLENGE SIGNING PAYLOAD BUILDER
 * =========================================================
 */


/**
 * Python Authenticator servisindeki
 * build_challenge_signing_payload fonksiyonuyla birebir
 * uyumlu imzalama metni oluşturur.
 *
 * Python tarafındaki alan sırası:
 *
 * challenge_id={challenge_public_id}
 * nonce={nonce}
 * user_id={external_user_id}
 * installation_id={installation_id}
 * decision={approve|reject}
 * expires_at={UTC ISO-8601}
 *
 * Bu alanların:
 *
 * - sırası,
 * - alan adları,
 * - büyük/küçük harfleri,
 * - satır sonları,
 * - boşlukları
 *
 * değiştirilmemelidir.
 *
 * Payload içindeki tek karakterlik fark bile ECDSA
 * imzasının Python tarafında doğrulanamamasına neden
 * olur.
 *
 * Bu sürüm java.time kullanmaz ve minimum SDK 24 ile
 * çalışır.
 */
class ChallengeSigningPayloadBuilder {

    /*
     * =====================================================
     * PUBLIC METOTLAR
     * =====================================================
     */


    /**
     * ChallengeSigningInput modelinden imzalanacak UTF-8
     * byte dizisini oluşturur.
     *
     * Oluşturulan byte dizisi DeviceKeyManager içindeki
     * SHA256withECDSA imzalama metoduna gönderilir.
     */
    fun buildPayloadBytes(
        input: ChallengeSigningInput,
    ): ByteArray {
        val payloadText =
            buildPayloadText(
                input = input,
            )


        return payloadText.toByteArray(
            StandardCharsets.UTF_8,
        )
    }


    /**
     * İmzalanacak challenge payloadını String olarak
     * oluşturur.
     *
     * Python tarafında kullanılan yapı:
     *
     * "\n".join([...])
     *
     * Kotlin tarafında joinToString(separator = "\n")
     * kullanılarak aynı metin üretilir.
     */
    fun buildPayloadText(
        input: ChallengeSigningInput,
    ): String {
        when (
            val validationResult =
                input.validate()
        ) {
            ChallengeSigningInputValidationResult.Valid -> {
                // Payload oluşturulabilir.
            }

            is ChallengeSigningInputValidationResult.Invalid -> {
                throw ChallengeSigningPayloadException(
                    message =
                    validationResult.message,
                )
            }
        }


        val challengePublicId =
            input.challengePublicId.trim()

        val nonce =
            input.nonce.trim()

        val externalUserId =
            input.externalUserId.trim()

        val installationId =
            input.installationId.trim()

        val decision =
            normalizeDecision(
                decision =
                input.decision,
            )

        val expiresAt =
            normalizeExpiresAtForPython(
                value =
                input.expiresAt,
            )


        /*
         * Python security.py içindeki gerçek payload
         * sırası aşağıdaki gibidir.
         *
         * Sona fazladan satır sonu eklenmez.
         */
        return listOf(
            "challenge_id=$challengePublicId",
            "nonce=$nonce",
            "user_id=$externalUserId",
            "installation_id=$installationId",
            "decision=$decision",
            "expires_at=$expiresAt",
        ).joinToString(
            separator = "\n",
        )
    }


    /**
     * WebSocket challenge alanlarını doğrudan alarak
     * imzalanacak byte dizisini oluşturur.
     *
     * ChallengeRepository bu metodu kullanır.
     */
    fun buildPayloadBytes(
        challengePublicId: String,
        nonce: String,
        externalUserId: String,
        installationId: String,
        decision: ChallengeDecision,
        expiresAt: String,
    ): ByteArray {
        return buildPayloadBytes(
            input =
            ChallengeSigningInput(
                challengePublicId =
                challengePublicId,

                nonce =
                nonce,

                externalUserId =
                externalUserId,

                installationId =
                installationId,

                decision =
                decision,

                expiresAt =
                expiresAt,
            ),
        )
    }


    /*
     * =====================================================
     * KARAR NORMALLEŞTİRME
     * =====================================================
     */


    /**
     * ChallengeDecision enum değerini Python servisinin
     * beklediği metne dönüştürür.
     *
     * Dönen değer yalnızca:
     *
     * approve
     * reject
     *
     * olabilir.
     */
    private fun normalizeDecision(
        decision: ChallengeDecision,
    ): String {
        return when (decision) {
            ChallengeDecision.APPROVE -> {
                "approve"
            }

            ChallengeDecision.REJECT -> {
                "reject"
            }
        }
    }


    /*
     * =====================================================
     * EXPIRES_AT NORMALLEŞTİRME
     * =====================================================
     */


    /**
     * WebSocket üzerinden gelen expires_at değerini
     * Python datetime.isoformat() çıktısıyla uyumlu UTC
     * biçimine dönüştürür.
     *
     * Python payloadında UTC zaman çoğunlukla:
     *
     * 2026-08-03T10:15:00+00:00
     *
     * veya:
     *
     * 2026-08-03T10:15:00.123456+00:00
     *
     * biçimindedir.
     *
     * Gelen değer Z, +00:00 veya farklı bir saat dilimi
     * içerebilir. Önce Unix zamanına çevrilir, ardından
     * UTC +00:00 biçiminde yeniden yazılır.
     */
    private fun normalizeExpiresAtForPython(
        value: String,
    ): String {
        val normalizedValue =
            value.trim()


        if (normalizedValue.isBlank()) {
            throw ChallengeSigningPayloadException(
                message = (
                        "Challenge son geçerlilik zamanı "
                                + "boş olamaz."
                        ),
            )
        }


        val parsedTime =
            parseIsoDateTime(
                value =
                normalizedValue,
            )
                ?: throw ChallengeSigningPayloadException(
                    message = (
                            "Challenge expires_at değeri "
                                    + "geçerli ISO-8601 biçiminde "
                                    + "değil: "
                                    + normalizedValue
                            ),
                )


        return formatLikePythonIsoFormat(
            timeMillis =
            parsedTime.timeMillis,

            microseconds =
            parsedTime.microseconds,
        )
    }


    /**
     * ISO-8601 tarih değerini Unix milisaniye ve
     * mikrosaniye bilgisine dönüştürür.
     *
     * Desteklenen örnekler:
     *
     * 2026-08-03T10:15:00Z
     * 2026-08-03T10:15:00.123Z
     * 2026-08-03T10:15:00.123456Z
     * 2026-08-03T10:15:00+00:00
     * 2026-08-03T13:15:00+03:00
     * 2026-08-03T10:15:00.123456+00:00
     */
    private fun parseIsoDateTime(
        value: String,
    ): ParsedIsoDateTime? {
        val normalizedInput =
            normalizeIsoInput(
                value =
                value,
            )
                ?: return null


        val formatPatterns =
            listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
            )


        formatPatterns.forEach {
                pattern ->

            try {
                val formatter =
                    SimpleDateFormat(
                        pattern,
                        Locale.US,
                    ).apply {
                        isLenient =
                            false
                    }


                val parsedDate =
                    formatter.parse(
                        normalizedInput.valueForParsing,
                    )


                if (parsedDate != null) {
                    return ParsedIsoDateTime(
                        timeMillis =
                        parsedDate.time,

                        microseconds =
                        normalizedInput.microseconds,
                    )
                }
            } catch (_: Exception) {
                /*
                 * Sonraki format denenir.
                 */
            }
        }


        return null
    }


    /**
     * ISO tarih metnindeki kesirli saniye bölümünü
     * ayırır.
     *
     * SimpleDateFormat en fazla milisaniye çözünürlüğü
     * kullandığı için parse işlemine üç basamaklı değer
     * verilir.
     *
     * Ancak Python payloadında mikrosaniye altı basamakla
     * yazılabildiği için orijinal mikrosaniye bilgisi
     * ayrıca korunur.
     */
    private fun normalizeIsoInput(
        value: String,
    ): NormalizedIsoInput? {
        var normalizedValue =
            value.trim()


        if (normalizedValue.isBlank()) {
            return null
        }


        /*
         * Küçük z ile gelen UTC işaretini büyük Z
         * biçimine çeviriyoruz.
         */
        if (
            normalizedValue.endsWith(
                suffix = "z",
                ignoreCase = false,
            )
        ) {
            normalizedValue =
                normalizedValue.dropLast(
                    1,
                ) + "Z"
        }


        /*
         * Kesirli saniye bölümünü yakalar.
         *
         * Örnek:
         *
         * .123456
         */
        val fractionRegex =
            Regex(
                pattern =
                """\.(\d+)(?=Z|[+-]\d{2}:\d{2}$)""",
            )


        val fractionMatch =
            fractionRegex.find(
                input =
                normalizedValue,
            )


        val fractionalDigits =
            fractionMatch
                ?.groupValues
                ?.getOrNull(
                    1,
                )
                .orEmpty()


        /*
         * Python datetime en fazla altı basamaklı
         * mikrosaniye hassasiyetindedir.
         *
         * Daha kısa değerler sağdan sıfırla tamamlanır,
         * daha uzun değerler altı basamağa kesilir.
         */
        val microseconds =
            if (fractionalDigits.isBlank()) {
                0
            } else {
                fractionalDigits
                    .take(
                        6,
                    )
                    .padEnd(
                        length = 6,
                        padChar = '0',
                    )
                    .toIntOrNull()
                    ?: 0
            }


        /*
         * SimpleDateFormat için en fazla üç basamaklı
         * milisaniye bölümü hazırlanır.
         */
        val valueForParsing =
            if (fractionMatch == null) {
                normalizedValue
            } else {
                val milliseconds =
                    fractionalDigits
                        .take(
                            3,
                        )
                        .padEnd(
                            length = 3,
                            padChar = '0',
                        )


                normalizedValue.replaceRange(
                    range =
                    fractionMatch.range,

                    replacement =
                    ".$milliseconds",
                )
            }


        return NormalizedIsoInput(
            valueForParsing =
            valueForParsing,

            microseconds =
            microseconds,
        )
    }


    /**
     * Unix zamanını Python datetime.isoformat() biçimine
     * yakın bir UTC metnine dönüştürür.
     *
     * Python davranışı:
     *
     * - Mikrosaniye 0 ise kesirli bölüm yazılmaz.
     * - Mikrosaniye varsa altı basamak yazılır.
     * - UTC için Z yerine +00:00 kullanılır.
     */
    private fun formatLikePythonIsoFormat(
        timeMillis: Long,
        microseconds: Int,
    ): String {
        val formatter =
            SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss",
                Locale.US,
            ).apply {
                timeZone =
                    TimeZone.getTimeZone(
                        "UTC",
                    )
            }


        val dateTimeText =
            formatter.format(
                timeMillis,
            )


        val fractionalPart =
            if (microseconds == 0) {
                ""
            } else {
                ".${microseconds.toString().padStart(6, '0')}"
            }


        return buildString {
            append(
                dateTimeText,
            )

            append(
                fractionalPart,
            )

            append(
                "+00:00",
            )
        }
    }
}


/*
 * =========================================================
 * DAHİLİ TARİH MODELLERİ
 * =========================================================
 */


/**
 * ISO zaman metninin parse edilmeden önce
 * normalleştirilmiş hâlidir.
 */
private data class NormalizedIsoInput(
    /**
     * SimpleDateFormat ile parse edilecek değer.
     */
    val valueForParsing: String,

    /**
     * Orijinal metinden alınan mikrosaniye değeri.
     */
    val microseconds: Int,
)


/**
 * Parse edilmiş ISO tarih bilgisidir.
 */
private data class ParsedIsoDateTime(
    /**
     * UTC Unix zamanı, milisaniye cinsinden.
     */
    val timeMillis: Long,

    /**
     * Python payloadına yazılacak mikrosaniye değeri.
     */
    val microseconds: Int,
)


/*
 * =========================================================
 * PAYLOAD EXCEPTION
 * =========================================================
 */


/**
 * Challenge imzalama payloadı oluşturulamadığında
 * kullanılan özel hata sınıfıdır.
 */
class ChallengeSigningPayloadException(
    override val message: String,
    override val cause: Throwable? = null,
) : IllegalArgumentException(
    message,
    cause,
)