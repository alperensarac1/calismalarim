package com.alperensarac.projectmanagementkotlin.feature.profile

/**
 * Profil ekranındaki tek seferlik UI olaylarıdır.
 */
sealed interface ProfileUiEvent {

    /**
     * Snackbar gibi kısa süreli mesajların gösterilmesini sağlar.
     */
    data class ShowMessage(
        val message: String
    ) : ProfileUiEvent
}