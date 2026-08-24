package com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs

import kotlinx.serialization.Serializable

/**
 * GET /api/tasks/{taskId}/time-logs/summary
 *
 * response modelidir.
 */
@Serializable
data class TaskTimeLogSummaryDto(
    val taskId: Int,

    val taskTitle: String,

    val estimatedHours: Double? = null,

    val actualHours: Double,

    val differenceHours: Double? = null,

    val timeLogCount: Int,

    val contributorCount: Int,

    val lastWorkDate: String? = null
)