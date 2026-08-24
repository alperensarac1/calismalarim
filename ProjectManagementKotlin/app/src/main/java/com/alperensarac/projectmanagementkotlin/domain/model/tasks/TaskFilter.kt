package com.alperensarac.projectmanagementkotlin.domain.model.tasks

/**
 * Görev liste ekranındaki filtreleri temsil eder.
 *
 * Page ve PageSize Paging 3 tarafından yönetilir.
 */
data class TaskFilter(
    val search: String = "",

    val projectId: Int? = null,

    val assignedToUserId: Int? = null,

    val status: TaskStatus? = null,

    val priority: TaskPriority? = null,

    val isOverdue: Boolean? = null
)