package com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs

import kotlinx.serialization.Serializable

/**
 * POST /api/tasks/{taskId}/time-logs
 */
@Serializable
data class CreateTaskTimeLogRequestDto(

    /**
     * Backend:
     *
     * decimal Hours
     */
    val hours: Double,

    /**
     * Nullable.
     */
    val description: String?,

    /**
     * Backend DateTime.
     *
     * API'ye ISO-8601 string gönderiyoruz.
     */
    val workDate: String
)