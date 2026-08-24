package com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks

import kotlinx.serialization.Serializable

/**
 * PATCH /api/Tasks/{id}/status
 *
 * Backend:
 *
 * public sealed class UpdateTaskStatusRequestDto
 * {
 *     public ProjectTaskStatus Status { get; set; }
 * }
 */
@Serializable
data class UpdateTaskStatusRequestDto(
    val status: String
)