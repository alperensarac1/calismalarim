package com.alperensarac.projectmanagementkotlin.core.security.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Refresh token gibi hassas değerleri Android Keystore tabanlı
 * AES/GCM şifreleme ile korur.
 *
 * Anahtar uygulama kaynak kodunda veya DataStore içerisinde tutulmaz.
 * AndroidKeyStore sağlayıcısı tarafından yönetilir.
 */
@Singleton
class TokenCipher @Inject constructor() {

    /**
     * Düz metni şifreler.
     *
     * Sonuç olarak Base64 biçiminde:
     *
     * - cipherText
     * - initializationVector
     *
     * döndürülür.
     */
    fun encrypt(plainText: String): EncryptedValue {
        require(plainText.isNotBlank()) {
            "Şifrelenecek değer boş olamaz."
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)

        /*
         * Anahtar henüz oluşturulmamışsa oluşturulur.
         * Daha önce oluşturulmuşsa mevcut anahtar kullanılır.
         */
        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateSecretKey()
        )

        val encryptedBytes = cipher.doFinal(
            plainText.toByteArray(StandardCharsets.UTF_8)
        )

        val encodedCipherText = Base64.encodeToString(
            encryptedBytes,
            Base64.NO_WRAP
        )

        val encodedInitializationVector = Base64.encodeToString(
            cipher.iv,
            Base64.NO_WRAP
        )

        return EncryptedValue(
            cipherText = encodedCipherText,
            initializationVector = encodedInitializationVector
        )
    }

    /**
     * Daha önce encrypt fonksiyonuyla şifrelenmiş değeri çözer.
     */
    fun decrypt(encryptedValue: EncryptedValue): String {
        require(encryptedValue.cipherText.isNotBlank()) {
            "Şifreli değer boş olamaz."
        }

        require(encryptedValue.initializationVector.isNotBlank()) {
            "Şifreleme IV değeri boş olamaz."
        }

        val encryptedBytes = Base64.decode(
            encryptedValue.cipherText,
            Base64.NO_WRAP
        )

        val initializationVector = Base64.decode(
            encryptedValue.initializationVector,
            Base64.NO_WRAP
        )

        val cipher = Cipher.getInstance(TRANSFORMATION)

        /*
         * AES/GCM şifre çözme işleminde şifreleme sırasında üretilen
         * IV değeri tekrar kullanılmalıdır.
         */
        val parameterSpec = GCMParameterSpec(
            AUTHENTICATION_TAG_LENGTH_BITS,
            initializationVector
        )

        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            parameterSpec
        )

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(
            decryptedBytes,
            StandardCharsets.UTF_8
        )
    }

    /**
     * Android Keystore içerisindeki AES anahtarını döndürür.
     *
     * Anahtar bulunamazsa yeni bir anahtar oluşturulur.
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
            load(null)
        }

        val existingKey = keyStore.getKey(
            KEY_ALIAS,
            null
        ) as? SecretKey

        if (existingKey != null) {
            return existingKey
        }

        return createSecretKey()
    }

    /**
     * Android Keystore içerisinde yeni AES anahtarı oluşturur.
     */
    private fun createSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE_PROVIDER
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            /*
             * GCM blok modu kullanılır.
             */
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)

            /*
             * GCM modunda padding kullanılmaz.
             */
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )

            /*
             * Her şifreleme işleminde rastgele IV üretilir.
             */
            .setRandomizedEncryptionRequired(true)

            /*
             * 256 bit AES anahtarı kullanılır.
             */
            .setKeySize(AES_KEY_SIZE_BITS)
            .build()

        keyGenerator.init(keyGenParameterSpec)

        return keyGenerator.generateKey()
    }

    private companion object {

        const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"

        const val KEY_ALIAS =
            "project_management_refresh_token_key"

        const val TRANSFORMATION =
            "AES/GCM/NoPadding"

        const val AES_KEY_SIZE_BITS = 256

        const val AUTHENTICATION_TAG_LENGTH_BITS = 128
    }
}