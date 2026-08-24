package com.alperensarac.projectmanagementkotlin.domain.usecase.auth



import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthSession
import com.alperensarac.projectmanagementkotlin.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Kullanıcı giriş işlemini gerçekleştiren use case sınıfıdır.
 *
 * ViewModel repository implementasyonunu doğrudan kullanmaz.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ): AppResult<AuthSession> {
        return authRepository.login(
            email = email,
            password = password
        )
    }
}