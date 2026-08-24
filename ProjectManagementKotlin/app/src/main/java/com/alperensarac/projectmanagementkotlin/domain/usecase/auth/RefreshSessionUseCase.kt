package com.alperensarac.projectmanagementkotlin.domain.usecase.auth

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthSession
import com.alperensarac.projectmanagementkotlin.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Kayıtlı refresh token üzerinden kullanıcı oturumunu yeniler.
 *
 * Splash ekranı uygulama açılırken bu use case'i çalıştıracaktır.
 *
 * Authentication ayrıntıları ViewModel içerisinde tutulmaz.
 * ViewModel yalnızca use case sonucuna göre yönlendirme yapar.
 */
class RefreshSessionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): AppResult<AuthSession> {
        return authRepository.refreshSession()
    }
}