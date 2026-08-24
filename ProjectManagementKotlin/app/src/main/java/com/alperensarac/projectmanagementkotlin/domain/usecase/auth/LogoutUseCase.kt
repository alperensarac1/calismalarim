package com.alperensarac.projectmanagementkotlin.domain.usecase.auth

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Kullanıcının mevcut cihazdaki oturumunu kapatır.
 *
 * Repository:
 *
 * 1. Saklanan refresh tokenı okur.
 * 2. POST /api/Auth/logout isteğini gönderir.
 * 3. İşlem sonucundan bağımsız olarak yerel tokenları temizler.
 */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): AppResult<Unit> {
        return authRepository.logout()
    }
}