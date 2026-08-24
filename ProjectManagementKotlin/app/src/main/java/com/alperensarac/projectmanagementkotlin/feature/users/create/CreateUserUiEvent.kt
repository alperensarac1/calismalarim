package com.alperensarac.projectmanagementkotlin.feature.users.create

sealed interface CreateUserUiEvent {

    data class UserCreated(
        val userId: Int,
        val message: String
    ) : CreateUserUiEvent
}