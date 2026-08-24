package com.alperensarac.projectmanagementkotlin.feature.auth.login

/**
 * Login ekranındaki tek seferlik olayları temsil eder.
 *
 * Kalıcı ekran durumu StateFlow ile, tek seferlik navigation gibi olaylar
 * Channel üzerinden yönetilir.
 */
sealed interface LoginUiEvent {

    /**
     * Login başarıyla tamamlandığında ana ekrana geçilmesini ister.
     */
    data object NavigateToHome : LoginUiEvent

    /**
     * Kullanıcıya kısa süreli mesaj gösterilmesini ister.
     */
    data class ShowMessage(
        val message: String
    ) : LoginUiEvent
}