package com.alperensarac.projectmanagementkotlin.feature.projects.edit

/**
 * Tek seferlik UI olayları.
 */
sealed interface EditProjectUiEvent {

    data class ProjectUpdated(
        val projectId: Int,
        val message: String
    ) : EditProjectUiEvent
}