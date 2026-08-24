package com.alperensarac.projectmanagementkotlin.domain.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthSession
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser

/**
 * Authentication işlemlerinin domain sözleşmesidir.
 *
 * ViewModel veya UseCase katmanı AuthApi'yi doğrudan kullanmaz.
 */
interface AuthRepository {

    /**
     * Kullanıcı girişini gerçekleştirir ve başarılı tokenları kaydeder.
     */
    suspend fun login(
        email: String,
        password: String
    ): AppResult<AuthSession>

    /**
     * Saklanan refresh token ile access token yeniler.
     */
    suspend fun refreshSession(): AppResult<AuthSession>

    /**
     * Mevcut kullanıcının bilgilerini backend'den getirir.
     */
    suspend fun getCurrentUser(): AppResult<AuthUser>

    /**
     * Backend logout işlemini çağırır ve yerel tokenları temizler.
     */
    suspend fun logout(): AppResult<Unit>

    /**
     * Backend çağrısı yapmadan yerel tokenları temizler.
     *
     * Refresh token geçersizse veya ağ bağlantısı yoksa kullanıcıyı
     * cihaz üzerinde oturumdan çıkarmak için kullanılabilir.
     */
    suspend fun clearLocalSession()
}