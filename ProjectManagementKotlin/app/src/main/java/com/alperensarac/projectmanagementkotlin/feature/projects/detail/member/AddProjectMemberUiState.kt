package com.alperensarac.projectmanagementkotlin.feature.projects.detail.member

import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.model.users.User

/**
 * Üye ekleme dialog'unun state modelidir.
 */
data class AddProjectMemberUiState(

    val search: String = "",

    /**
     * Kullanıcı listesinden seçilen kullanıcı.
     */
    val selectedUser: User? = null,

    /**
     * Yeni üyenin proje rolü.
     *
     * Backend varsayılanı da Member olduğu için Android'de de Member
     * ile başlıyoruz.
     */
    val selectedRole: ProjectMemberRole =
        ProjectMemberRole.MEMBER,

    val isSaving: Boolean = false,

    val errorMessage: String? = null
) {

    /**
     * Kullanıcı seçilmeden POST isteği gönderilemez.
     */
    val canSave: Boolean
        get() =
            selectedUser != null &&
                    !isSaving
}