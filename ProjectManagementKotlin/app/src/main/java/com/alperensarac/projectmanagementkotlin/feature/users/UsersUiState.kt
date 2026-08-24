package com.alperensarac.projectmanagementkotlin.feature.users

/**
 * Kullanıcı listesi filtre state'i.
 */
data class UsersUiState(

    val search: String = "",

    val selectedRole: String? = null,

    val activeFilter:
    UserActiveFilter =
        UserActiveFilter.ALL
)