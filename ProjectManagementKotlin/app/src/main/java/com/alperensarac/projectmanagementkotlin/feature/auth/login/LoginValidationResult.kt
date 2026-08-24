package com.alperensarac.projectmanagementkotlin.feature.auth.login

/**
 * Login form doğrulamasının sonucunu temsil eder.
 *
 * Fragment doğrulama kurallarını bilmez. Form doğrulaması ViewModel
 * tarafından bu model üzerinden yönetilir.
 */
data class LoginValidationResult(
    val emailError: String? = null,
    val passwordError: String? = null
) {

    /**
     * Herhangi bir doğrulama hatası yoksa form geçerlidir.
     */
    val isValid: Boolean
        get() = emailError == null && passwordError == null
}