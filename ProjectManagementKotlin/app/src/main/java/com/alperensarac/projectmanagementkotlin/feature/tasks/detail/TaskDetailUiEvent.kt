package com.alperensarac.projectmanagementkotlin.feature.tasks.detail

/**
 * Görev detay ekranındaki tek seferlik olaylar.
 */
sealed interface TaskDetailUiEvent {

    data class ShowMessage(
        val message: String
    ) : TaskDetailUiEvent
}