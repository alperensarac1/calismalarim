package com.alperensarac.projectmanagementkotlin.feature.users.detail

/**
 * Tek sefer tüketilecek olaylar.
 *
 * Snackbar veya navigation gibi olayları StateFlow içerisinde tutmuyoruz.
 */
sealed interface UserDetailUiEvent {

    data class ShowMessage(
        val message: String
    ) : UserDetailUiEvent

    data class UserDeleted(
        val message: String
    ) : UserDetailUiEvent
}