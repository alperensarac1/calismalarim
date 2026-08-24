package com.alperensarac.projectmanagementkotlin.feature.tasks.form

import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task

sealed interface TaskFormUiEvent {

    data class TaskSaved(
        val task: Task,
        val message: String
    ) : TaskFormUiEvent

    data class ShowMessage(
        val message: String
    ) : TaskFormUiEvent
}