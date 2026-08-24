package com.alperensarac.projectmanagementkotlin.feature.projects.detail.member

/**
 * Üye ekleme ekranının tek seferlik event'leri.
 */
sealed interface AddProjectMemberUiEvent {

    data class ShowMessage(
        val message: String
    ) : AddProjectMemberUiEvent

    /**
     * Üye başarılı şekilde eklendiğinde dialog kapanır.
     */
    data class MemberAdded(
        val message: String
    ) : AddProjectMemberUiEvent
}