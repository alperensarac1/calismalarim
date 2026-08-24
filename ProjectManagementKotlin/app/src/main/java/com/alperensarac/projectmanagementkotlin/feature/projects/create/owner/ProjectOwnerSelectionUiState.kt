package com.alperensarac.projectmanagementkotlin.feature.projects.create.owner

import com.alperensarac.projectmanagementkotlin.domain.model.users.User

/**
 * Proje sahibi seçim ekranının state'idir.
 */
data class ProjectOwnerSelectionUiState(

    /**
     * Kullanıcı arama alanı.
     */
    val search: String = "",

    /**
     * Listeden seçilmiş kullanıcı.
     */
    val selectedUser: User? = null
) {

    /**
     * Kullanıcı seçilmeden dialog onaylanamaz.
     */
    val canSelect: Boolean
        get() =
            selectedUser != null
}