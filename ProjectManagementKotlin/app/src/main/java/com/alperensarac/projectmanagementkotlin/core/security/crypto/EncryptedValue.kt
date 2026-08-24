package com.alperensarac.projectmanagementkotlin.core.security.crypto

/**
 * Android Keystore ile şifrelenen bir değeri temsil eder.
 *
 * AES/GCM her şifreleme işleminde benzersiz bir IV üretir.
 *
 * Bu nedenle DataStore içerisinde:
 *
 * - Şifreli metin
 * - IV
 *
 * birlikte saklanmalıdır.
 */
data class EncryptedValue(
    val cipherText: String,
    val initializationVector: String
)