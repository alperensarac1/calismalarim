package com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks

import kotlinx.serialization.Serializable

/**
 * Backend UpdateTaskRequestDto karşılığı.
 *
 * PUT /api/Tasks/{id}
 */
@Serializable
data class UpdateTaskRequestDto(

    val title: String,

    val description: String?,

    val assignedToUserId: Int?,

    val status: String,

    val priority: String,

    val dueDate: String?,

    val estimatedHours: Double?
)