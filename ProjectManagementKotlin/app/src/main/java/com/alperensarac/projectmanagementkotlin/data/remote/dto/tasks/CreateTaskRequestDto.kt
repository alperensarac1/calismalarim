package com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks

import kotlinx.serialization.Serializable

/**
 * Backend:
 *
 * CreateTaskRequestDto
 *
 * POST /api/Tasks
 */
@Serializable
data class CreateTaskRequestDto(

    val projectId: Int,

    val title: String,

    val description: String?,

    /**
     * null ise görev henüz kimseye atanmaz.
     */
    val assignedToUserId: Int?,

    /**
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

    /**
     * Backend DateTime?
     *
     * Örnek:
     * 2026-08-15T00:00:00.000Z
     */
    val dueDate: String?,

    val estimatedHours: Double?
)