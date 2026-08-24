package com.alperensarac.projectmanagementkotlin.core.datastore.token

import kotlinx.coroutines.flow.Flow

/**
 * Uygulamanın token saklama sözleşmesidir.
 *
 * Network ve authentication katmanı bu interface üzerinden çalışır.
 * Böylece DataStore implementasyonu daha sonra değiştirilebilir ve
 * unit testlerde fake bir TokenStorage kullanılabilir.
 */
interface TokenStorage {

    /**
     * Token bilgilerindeki değişiklikleri Flow olarak yayınlar.
     */
    val tokenSnapshotFlow: Flow<TokenSnapshot>

    /**
     * Mevcut token bilgilerini tek seferlik okur.
     */
    suspend fun getTokenSnapshot(): TokenSnapshot

    /**
     * Login veya refresh işlemi başarılı olduğunda token bilgilerini kaydeder.
     *
     * Access token bellekte tutulur.
     * Refresh token şifrelenerek DataStore'a yazılır.
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        accessTokenExpiresAtUtc: String?
    )

    /**
     * Yalnızca access token değerini günceller.
     *
     * Bazı backend yapılarında refresh işlemi yeni refresh token dönmeyebilir.
     * Bu ihtimal için ayrı metot tutulmaktadır.
     */
    suspend fun updateAccessToken(
        accessToken: String,
        accessTokenExpiresAtUtc: String?
    )

    /**
     * Access ve refresh token bilgilerini tamamen temizler.
     */
    suspend fun clearTokens()

    /**
     * Bellekteki access token değerini senkron olarak döndürür.
     *
     * OkHttp Interceptor suspend fonksiyon çağıramadığı için bu metot
     * gereklidir.
     */
    fun getAccessToken(): String?
}