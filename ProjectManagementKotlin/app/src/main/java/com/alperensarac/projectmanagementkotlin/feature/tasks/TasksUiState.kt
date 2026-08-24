package com.alperensarac.projectmanagementkotlin.feature.tasks

import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus

/**
 * Görev listesindeki filtrelerin UI state'i.
 */
data class TasksUiState(
    val search: String = "",

    val selectedStatus: TaskStatus? = null,

    val selectedPriority: TaskPriority? = null,

    val overdueFilter: OverdueFilter =
        OverdueFilter.ALL
)

/**
 * Nullable Boolean yerine okunabilir bir filtre modeli kullanıyoruz.
 */
enum class OverdueFilter {

    ALL,

    OVERDUE_ONLY,

    NOT_OVERDUE
}