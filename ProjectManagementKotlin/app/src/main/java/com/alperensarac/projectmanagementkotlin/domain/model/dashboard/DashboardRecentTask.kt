package com.alperensarac.projectmanagementkotlin.domain.model.dashboard

/**
 * Dashboard üzerinde gösterilen kısa görev modelidir.
 */
data class DashboardRecentTask(
    val id: Int,
    val title: String,
    val projectId: Int,
    val projectName: String,
    val status: String,
    val priority: String,
    val assignedToUserId: Int?,
    val assignedToUserFullName: String?,
    val dueDateUtc: String?,
    val isOverdue: Boolean,
    val createdAtUtc: String,
    val updatedAtUtc: String?
)