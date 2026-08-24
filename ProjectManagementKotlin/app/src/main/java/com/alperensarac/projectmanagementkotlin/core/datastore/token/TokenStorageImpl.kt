package com.alperensarac.projectmanagementkotlin.core.datastore.token

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alperensarac.projectmanagementkotlin.core.security.crypto.EncryptedValue
import com.alperensarac.projectmanagementkotlin.core.security.crypto.TokenCipher
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * TokenStorage interface'inin DataStore ve Android Keystore kullanan
 * gerçek implementasyonudur.
 */
@Singleton
class TokenStorageImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenCipher: TokenCipher,
    private val accessTokenMemoryStore: AccessTokenMemoryStore
) : TokenStorage {

    /**
     * DataStore içerisindeki refresh token değişikliklerini izler.
     *
     * Şifre çözme başarısız olursa güvenlik nedeniyle refresh token null
     * kabul edilir.
     */
    override val tokenSnapshotFlow: Flow<TokenSnapshot> =
        context.tokenDataStore.data
            .catch { throwable ->
                /*
                 * DataStore okuma sırasında IOException oluşursa boş
                 * Preferences yayını yapılır.
                 *
                 * Diğer exception türleri tekrar fırlatılır.
                 */
                if (throwable is IOException) {
                    emit(
                        androidx.datastore.preferences.core.emptyPreferences()
                    )
                } else {
                    throw throwable
                }
            }
            .map { preferences ->
                createTokenSnapshot(preferences)
            }

    /**
     * Mevcut token durumunu bir kez okur.
     */
    override suspend fun getTokenSnapshot(): TokenSnapshot {
        return tokenSnapshotFlow.first()
    }

    /**
     * Login veya token refresh işlemi sonrasında tüm tokenları kaydeder.
     */
    override suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        accessTokenExpiresAtUtc: String?
    ) {
        require(accessToken.isNotBlank()) {
            "Access token boş olamaz."
        }

        require(refreshToken.isNotBlank()) {
            "Refresh token boş olamaz."
        }

        /*
         * Access token yalnızca belleğe yazılır.
         */
        accessTokenMemoryStore.setAccessToken(accessToken)

        /*
         * Refresh token DataStore'a yazılmadan önce Keystore anahtarıyla
         * şifrelenir.
         */
        val encryptedRefreshToken = tokenCipher.encrypt(
            refreshToken.trim()
        )

        context.tokenDataStore.edit { preferences ->
            preferences[KEY_REFRESH_TOKEN_CIPHER_TEXT] =
                encryptedRefreshToken.cipherText

            preferences[KEY_REFRESH_TOKEN_IV] =
                encryptedRefreshToken.initializationVector

            if (accessTokenExpiresAtUtc.isNullOrBlank()) {
                preferences.remove(KEY_ACCESS_TOKEN_EXPIRES_AT_UTC)
            } else {
                preferences[KEY_ACCESS_TOKEN_EXPIRES_AT_UTC] =
                    accessTokenExpiresAtUtc.trim()
            }
        }
    }

    /**
     * Yalnızca bellekteki access token ve sona erme bilgisini günceller.
     */
    override suspend fun updateAccessToken(
        accessToken: String,
        accessTokenExpiresAtUtc: String?
    ) {
        require(accessToken.isNotBlank()) {
            "Access token boş olamaz."
        }

        accessTokenMemoryStore.setAccessToken(accessToken)

        context.tokenDataStore.edit { preferences ->
            if (accessTokenExpiresAtUtc.isNullOrBlank()) {
                preferences.remove(KEY_ACCESS_TOKEN_EXPIRES_AT_UTC)
            } else {
                preferences[KEY_ACCESS_TOKEN_EXPIRES_AT_UTC] =
                    accessTokenExpiresAtUtc.trim()
            }
        }
    }

    /**
     * Kullanıcı çıkış yaptığında bütün token bilgilerini temizler.
     */
    override suspend fun clearTokens() {
        /*
         * Önce bellekteki access token temizlenir.
         */
        accessTokenMemoryStore.clear()

        /*
         * Ardından kalıcı ve şifrelenmiş refresh token bilgileri silinir.
         */
        context.tokenDataStore.edit { preferences ->
            preferences.remove(KEY_REFRESH_TOKEN_CIPHER_TEXT)
            preferences.remove(KEY_REFRESH_TOKEN_IV)
            preferences.remove(KEY_ACCESS_TOKEN_EXPIRES_AT_UTC)
        }
    }

    /**
     * OkHttp Interceptor tarafından kullanılacak senkron access token erişimi.
     */
    override fun getAccessToken(): String? {
        return accessTokenMemoryStore.getAccessToken()
    }

    /**
     * DataStore Preferences nesnesini uygulamanın TokenSnapshot modeline
     * dönüştürür.
     */
    private fun createTokenSnapshot(
        preferences: Preferences
    ): TokenSnapshot {
        val encryptedCipherText =
            preferences[KEY_REFRESH_TOKEN_CIPHER_TEXT]

        val initializationVector =
            preferences[KEY_REFRESH_TOKEN_IV]

        val refreshToken = decryptRefreshTokenOrNull(
            cipherText = encryptedCipherText,
            initializationVector = initializationVector
        )

        return TokenSnapshot(
            accessToken = accessTokenMemoryStore.getAccessToken(),
            refreshToken = refreshToken,
            accessTokenExpiresAtUtc =
            preferences[KEY_ACCESS_TOKEN_EXPIRES_AT_UTC]
        )
    }

    /**
     * Şifrelenmiş refresh token değerini güvenli biçimde çözmeye çalışır.
     *
     * Şifreli metin veya IV eksikse null döndürür.
     *
     * Keystore anahtarı silinmiş, değiştirilmiş veya veri bozulmuşsa
     * exception UI katmanına taşınmaz; oturum geçersiz kabul edilir.
     */
    private fun decryptRefreshTokenOrNull(
        cipherText: String?,
        initializationVector: String?
    ): String? {
        if (
            cipherText.isNullOrBlank() ||
            initializationVector.isNullOrBlank()
        ) {
            return null
        }

        return runCatching {
            tokenCipher.decrypt(
                EncryptedValue(
                    cipherText = cipherText,
                    initializationVector = initializationVector
                )
            )
        }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    private companion object {

        val KEY_REFRESH_TOKEN_CIPHER_TEXT =
            stringPreferencesKey(
                "refresh_token_cipher_text"
            )

        val KEY_REFRESH_TOKEN_IV =
            stringPreferencesKey(
                "refresh_token_initialization_vector"
            )

        val KEY_ACCESS_TOKEN_EXPIRES_AT_UTC =
            stringPreferencesKey(
                "access_token_expires_at_utc"
            )
    }
}