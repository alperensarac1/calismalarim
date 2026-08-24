package com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs

import kotlinx.serialization.Serializable

/**
 * PUT /api/tasks/{taskId}/time-logs/{timeLogId}
 */
@Serializable
data class UpdateTaskTimeLogRequestDto(
    val hours: Double,
    val description: String?,
    val workDate: String
)