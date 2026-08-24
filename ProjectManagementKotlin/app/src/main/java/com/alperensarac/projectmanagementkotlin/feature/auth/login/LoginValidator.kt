package com.alperensarac.projectmanagementkotlin.feature.auth.login

import android.util.Patterns
import javax.inject.Inject

/**
 * Login form alanlarını doğrulayan sınıftır.
 *
 * Bu sınıf Android'in e-posta pattern kontrolünü kullandığı için feature
 * katmanında bulunmaktadır.
 */
class LoginValidator @Inject constructor() {

    /**
     * E-posta ve şifre alanlarını doğrular.
     */
    fun validate(
        email: String,
        password: String
    ): LoginValidationResult {
        val normalizedEmail = email.trim()

        val emailError = when {
            normalizedEmail.isBlank() -> {
                "E-posta adresi zorunludur."
            }

            !Patterns.EMAIL_ADDRESS.matcher(normalizedEmail).matches() -> {
                "Geçerli bir e-posta adresi giriniz."
            }

            else -> null
        }

        val passwordError = when {
            password.isBlank() -> {
                "Şifre zorunludur."
            }

            password.length < MINIMUM_PASSWORD_LENGTH -> {
                "Şifre en az $MINIMUM_PASSWORD_LENGTH karakter olmalıdır."
            }

            else -> null
        }

        return LoginValidationResult(
            emailError = emailError,
            passwordError = passwordError
        )
    }

    private companion object {
        const val MINIMUM_PASSWORD_LENGTH = 8
    }
}