package com.alperensarac.projectmanagementkotlin.feature.tasks.detail.actions

import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember

data class TaskActionsUiState(

    val isProcessing: Boolean = false,

    val isDeleting: Boolean = false,

    val isMembersLoading: Boolean = false,

    val members: List<ProjectMember> =
        emptyList(),

    val membersErrorMessage: String? =
        null
) {

    /**
     * Bir mutation devam ederken ikinci mutation başlatmayız.
     */
    val isAnyOperationRunning: Boolean
        get() =
            isProcessing ||
                    isDeleting
}