package com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks

import kotlinx.serialization.Serializable

/**
 * Backend:
 *
 * ProjectManagement.Application.DTOs.Tasks.TaskResponseDto
 *
 * modelinin Android karşılığıdır.
 */
@Serializable
data class TaskResponseDto(
    val id: Int,

    val title: String,

    val description: String? = null,

    val projectId: Int,

    val projectName: String,

    /**
     * Görev bir kullanıcıya atanmamış olabilir.
     */
    val assignedToUserId: Int? = null,

    val assignedToUserFullName: String? = null,

    val createdByUserId: Int,

    val createdByUserFullName: String,

    /**
     * Backend enum değerini string döndürüyor.
     *
     * Todo
     * InProgress
     * InReview
     * Done
     */
    val status: String,

    /**
     * Low
     * Medium
     * High
     * Critical
     */
    val priority: String,

    val dueDate: String? = null,

    /**
     * Backend decimal? -> Kotlin Double?
     */
    val estimatedHours: Double? = null,

    /**
     * Backend decimal -> Kotlin Double
     */
    val actualHours: Double,

    val completedAt: String? = null,

    val isOverdue: Boolean,

    val commentCount: Int,

    val createdAt: String,

    val updatedAt: String? = null
)