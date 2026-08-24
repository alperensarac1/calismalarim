package com.alperensarac.projectmanagementkotlin.domain.usecase.auth

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Oturum açmış kullanıcının güncel bilgilerini backend'den getirir.
 *
 * Bu use case:
 *
 * GET /api/Auth/me
 *
 * endpoint'ine karşılık gelir.
 *
 * ViewModel doğrudan repository implementasyonuna veya Retrofit servisine
 * bağımlı değildir.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): AppResult<AuthUser> {
        return authRepository.getCurrentUser()
    }
}