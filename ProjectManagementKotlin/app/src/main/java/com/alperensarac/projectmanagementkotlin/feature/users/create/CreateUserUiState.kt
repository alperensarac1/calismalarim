package com.alperensarac.projectmanagementkotlin.feature.users.create

/**
 * Yeni kullanıcı form state'i.
 */
data class CreateUserUiState(

    val isSubmitting: Boolean = false,

    val firstNameError: String? = null,

    val lastNameError: String? = null,

    val emailError: String? = null,

    val passwordError: String? = null,

    val departmentError: String? = null,

    val generalError: String? = null
)