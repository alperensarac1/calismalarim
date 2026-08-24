package com.alperensarac.projectmanagementkotlin.domain.model.tasks

/**
 * Uygulama içerisinde kullanılan görev domain modelidir.
 */
data class Task(
    val id: Int,

    val title: String,

    val description: String?,

    val projectId: Int,

    val projectName: String,

    val assignedToUserId: Int?,

    val assignedToUserFullName: String?,

    val createdByUserId: Int,

    val createdByUserFullName: String,

    val status: String,

    val priority: String,

    val dueDateUtc: String?,

    val estimatedHours: Double?,

    val actualHours: Double,

    val completedAtUtc: String?,

    val isOverdue: Boolean,

    val commentCount: Int,

    val createdAtUtc: String,

    val updatedAtUtc: String?
)