package com.alperensarac.projectmanagementauthenticator.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

import java.security.spec.ECGenParameterSpec


/*
 * =========================================================
 * DEVICE KEY MANAGER
 * =========================================================
 */


/**
 * Authenticator cihazına ait ECDSA P-256 anahtar çiftini
 * Android Keystore içerisinde yöneten sınıftır.
 *
 * Temel sorumlulukları:
 *
 * 1. Cihaza özel public/private key çifti oluşturmak.
 * 2. Private key'i Android Keystore içinde korumak.
 * 3. Public key'i PEM biçiminde Python servisine vermek.
 * 4. Challenge payloadını SHA256withECDSA ile imzalamak.
 * 5. Public key parmak izi oluşturmak.
 * 6. Geliştirme veya sıfırlama sırasında anahtarı silmek.
 *
 * Önemli:
 *
 * Private key hiçbir zaman uygulama dışına çıkarılmaz.
 * Android Keystore içindeki private key yalnızca imzalama
 * işlemlerinde kullanılır.
 */
class DeviceKeyManager {

    /*
     * =====================================================
     * KEYSTORE
     * =====================================================
     */


    /**
     * Android Keystore nesnesini yükler.
     */
    private fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(
            ANDROID_KEYSTORE_PROVIDER,
        ).apply {
            load(null)
        }
    }


    /*
     * =====================================================
     * ANAHTAR OLUŞTURMA
     * =====================================================
     */


    /**
     * Keystore içinde daha önce anahtar oluşturulmuşsa
     * mevcut anahtar çiftini döndürür.
     *
     * Anahtar bulunmuyorsa yeni ECDSA P-256 anahtar
     * çifti oluşturur.
     */
    fun getOrCreateKeyPair(): KeyPair {
        val existingKeyPair =
            getExistingKeyPair()

        if (existingKeyPair != null) {
            return existingKeyPair
        }

        return generateKeyPair()
    }


    /**
     * Android Keystore içinde kayıtlı anahtar çiftini
     * okur.
     *
     * Anahtar bulunamazsa null döndürür.
     */
    fun getExistingKeyPair(): KeyPair? {
        val keyStore =
            getKeyStore()

        if (
            !keyStore.containsAlias(
                KEY_ALIAS,
            )
        ) {
            return null
        }


        val privateKey =
            keyStore.getKey(
                KEY_ALIAS,
                null,
            ) as? PrivateKey
                ?: return null


        val certificate =
            keyStore.getCertificate(
                KEY_ALIAS,
            )
                ?: return null


        val publicKey =
            certificate.publicKey
                ?: return null


        return KeyPair(
            publicKey,
            privateKey,
        )
    }


    /**
     * Yeni ECDSA P-256 anahtar çifti oluşturur.
     *
     * Kullanılan algoritma:
     *
     * - Key algorithm: EC
     * - Curve: secp256r1 / P-256
     * - Signature digest: SHA-256
     * - Signature algorithm: SHA256withECDSA
     */
    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator =
            KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                ANDROID_KEYSTORE_PROVIDER,
            )


        val parameterSpec =
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or
                        KeyProperties.PURPOSE_VERIFY,
            )
                /*
                 * Python servisi ECDSA SHA-256 imzası
                 * doğruladığı için SHA-256 digest
                 * etkinleştirilir.
                 */
                .setDigests(
                    KeyProperties.DIGEST_SHA256,
                )

                /*
                 * NIST P-256 eğrisi.
                 *
                 * Python tarafında secp256r1 ve
                 * prime256v1 adları kabul edilmektedir.
                 */
                .setAlgorithmParameterSpec(
                    ECGenParameterSpec(
                        EC_CURVE_NAME,
                    ),
                )

                /*
                 * Kullanıcı kimlik doğrulaması zorunlu
                 * tutulmaz.
                 *
                 * Demo uygulamada PIN veya biyometri
                 * istemeden challenge imzalanabilir.
                 */
                .setUserAuthenticationRequired(
                    false,
                )

                /*
                 * Anahtar yalnızca Android Keystore
                 * içerisinde kullanılabilir.
                 */
                .build()


        keyPairGenerator.initialize(
            parameterSpec,
        )


        return keyPairGenerator.generateKeyPair()
    }


    /*
     * =====================================================
     * PUBLIC KEY
     * =====================================================
     */


    /**
     * Cihaz public key'ini döndürür.
     *
     * Anahtar yoksa otomatik olarak oluşturulur.
     */
    fun getOrCreatePublicKey(): PublicKey {
        return getOrCreateKeyPair().public
    }


    /**
     * Public key'i X.509 SubjectPublicKeyInfo biçimindeki
     * DER byte dizisi olarak döndürür.
     */
    fun getOrCreatePublicKeyDer(): ByteArray {
        return getOrCreatePublicKey()
            .encoded
            ?: throw DeviceKeyException(
                "Public key DER biçimine dönüştürülemedi.",
            )
    }


    /**
     * Public key'i Python servisinin kabul ettiği PEM
     * biçiminde döndürür.
     *
     * Örnek:
     *
     * -----BEGIN PUBLIC KEY-----
     * MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcD...
     * -----END PUBLIC KEY-----
     */
    fun getOrCreatePublicKeyPem(): String {
        val publicKeyDer =
            getOrCreatePublicKeyDer()


        val base64PublicKey =
            Base64.encodeToString(
                publicKeyDer,
                Base64.NO_WRAP,
            )


        /*
         * PEM standardında Base64 metni genellikle
         * 64 karakterlik satırlara bölünür.
         */
        val wrappedBase64 =
            base64PublicKey
                .chunked(
                    PEM_LINE_LENGTH,
                )
                .joinToString(
                    separator = "\n",
                )


        return buildString {
            append(
                PEM_PUBLIC_KEY_HEADER,
            )
            append("\n")

            append(
                wrappedBase64,
            )
            append("\n")

            append(
                PEM_PUBLIC_KEY_FOOTER,
            )
        }
    }


    /*
     * =====================================================
     * İMZALAMA
     * =====================================================
     */


    /**
     * Verilen payload byte dizisini Android Keystore
     * private key'iyle SHA256withECDSA kullanarak imzalar.
     *
     * Dönen değer standart Base64 biçimindedir.
     *
     * Python tarafında:
     *
     * base64.b64decode(signature_base64)
     *
     * kullanıldığı için URL-safe Base64 değil, standart
     * Base64 üretmeliyiz.
     */
    fun signPayload(
        payload: ByteArray,
    ): String {
        if (payload.isEmpty()) {
            throw DeviceKeyException(
                "İmzalanacak payload boş olamaz.",
            )
        }


        val privateKey =
            getOrCreateKeyPair().private
                ?: throw DeviceKeyException(
                    "Private key bulunamadı.",
                )


        return try {
            val signature =
                Signature.getInstance(
                    SIGNATURE_ALGORITHM,
                )


            signature.initSign(
                privateKey,
            )


            signature.update(
                payload,
            )


            val signatureBytes =
                signature.sign()


            Base64.encodeToString(
                signatureBytes,
                Base64.NO_WRAP,
            )
        } catch (exception: Exception) {
            throw DeviceKeyException(
                message = (
                        "Payload cihaz anahtarıyla "
                                + "imzalanamadı."
                        ),
                cause = exception,
            )
        }
    }


    /**
     * String payloadı UTF-8 byte dizisine çevirerek
     * imzalar.
     */
    fun signPayload(
        payload: String,
    ): String {
        if (payload.isBlank()) {
            throw DeviceKeyException(
                "İmzalanacak payload boş olamaz.",
            )
        }


        return signPayload(
            payload =
            payload.toByteArray(
                Charsets.UTF_8,
            ),
        )
    }


    /*
     * =====================================================
     * PUBLIC KEY PARMAK İZİ
     * =====================================================
     */


    /**
     * Public key için SHA-256 parmak izi üretir.
     *
     * Python servisindeki çıktı biçimiyle uyumludur.
     *
     * Örnek:
     *
     * SHA256:3A:8F:11:...
     */
    fun getOrCreatePublicKeyFingerprint(): String {
        val publicKeyDer =
            getOrCreatePublicKeyDer()


        val digest =
            MessageDigest
                .getInstance(
                    SHA_256_ALGORITHM,
                )
                .digest(
                    publicKeyDer,
                )


        val groupedDigest =
            digest.joinToString(
                separator = ":",
            ) {
                    byte ->

                "%02X".format(
                    byte.toInt() and 0xFF,
                )
            }


        return "SHA256:$groupedDigest"
    }


    /*
     * =====================================================
     * ANAHTAR DURUMU
     * =====================================================
     */


    /**
     * Android Keystore içinde cihaz anahtarının bulunup
     * bulunmadığını belirtir.
     */
    fun hasKeyPair(): Boolean {
        return try {
            getKeyStore().containsAlias(
                KEY_ALIAS,
            )
        } catch (_: Exception) {
            false
        }
    }


    /**
     * Kayıtlı anahtar algoritmasını döndürür.
     *
     * Beklenen değer:
     *
     * EC
     */
    fun getKeyAlgorithm(): String? {
        return getExistingKeyPair()
            ?.public
            ?.algorithm
    }


    /*
     * =====================================================
     * ANAHTAR SİLME
     * =====================================================
     */


    /**
     * Android Keystore içindeki cihaz anahtarını siler.
     *
     * Bu işlemden sonra eski public key ile kayıtlı cihaz
     * challenge imzalarını doğrulayamaz.
     *
     * Anahtar silindiyse cihazın Python servisine yeniden
     * kaydedilmesi gerekir.
     */
    fun deleteKeyPair() {
        try {
            val keyStore =
                getKeyStore()


            if (
                keyStore.containsAlias(
                    KEY_ALIAS,
                )
            ) {
                keyStore.deleteEntry(
                    KEY_ALIAS,
                )
            }
        } catch (exception: Exception) {
            throw DeviceKeyException(
                message = (
                        "Cihaz güvenlik anahtarı "
                                + "silinemedi."
                        ),
                cause = exception,
            )
        }
    }


    /*
     * =====================================================
     * SABİTLER
     * =====================================================
     */


    private companion object {

        /**
         * Android Keystore sağlayıcısının adı.
         */
        const val ANDROID_KEYSTORE_PROVIDER =
            "AndroidKeyStore"


        /**
         * Keystore içinde kullanılacak anahtar adı.
         *
         * Bu değer değişirse uygulama eski anahtarı
         * bulamaz ve yeni bir anahtar oluşturur.
         */
        const val KEY_ALIAS =
            "project_management_authenticator_device_key"


        /**
         * ECDSA P-256 eğrisinin Java adı.
         */
        const val EC_CURVE_NAME =
            "secp256r1"


        /**
         * Python tarafındaki:
         *
         * ECDSA(hashes.SHA256())
         *
         * doğrulamasıyla uyumlu Java imza algoritması.
         */
        const val SIGNATURE_ALGORITHM =
            "SHA256withECDSA"


        const val SHA_256_ALGORITHM =
            "SHA-256"


        const val PEM_PUBLIC_KEY_HEADER =
            "-----BEGIN PUBLIC KEY-----"


        const val PEM_PUBLIC_KEY_FOOTER =
            "-----END PUBLIC KEY-----"


        const val PEM_LINE_LENGTH =
            64
    }
}


/*
 * =========================================================
 * DEVICE KEY EXCEPTION
 * =========================================================
 */


/**
 * Android Keystore anahtar oluşturma, okuma, imzalama
 * veya silme işlemlerinde kullanılan özel hata sınıfıdır.
 */
class DeviceKeyException(
    override val message: String,
    override val cause: Throwable? = null,
) : IllegalStateException(
    message,
    cause,
)