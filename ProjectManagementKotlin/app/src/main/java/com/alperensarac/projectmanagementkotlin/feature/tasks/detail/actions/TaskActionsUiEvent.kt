package com.alperensarac.projectmanagementkotlin.feature.tasks.detail.actions

import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task

sealed interface TaskActionsUiEvent {

    data class StatusUpdated(
        val task: Task,
        val message: String
    ) : TaskActionsUiEvent

    data class AssignmentUpdated(
        val task: Task,
        val message: String
    ) : TaskActionsUiEvent

    data class ProjectMembersLoaded(
        val members: List<ProjectMember>
    ) : TaskActionsUiEvent

    /**
     * Görev silindikten sonra detay ekranının artık gösterilecek
     * bir modeli kalmadığı için geri dönmesi gerekir.
     */
    data class TaskDeleted(
        val message: String
    ) : TaskActionsUiEvent

    data class ShowMessage(
        val message: String
    ) : TaskActionsUiEvent
}