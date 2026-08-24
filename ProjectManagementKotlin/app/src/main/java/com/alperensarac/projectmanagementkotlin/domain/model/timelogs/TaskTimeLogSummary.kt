package com.alperensarac.projectmanagementkotlin.domain.model.timelogs

/**
 * Bir göreve ait zaman kayıtlarının özetidir.
 */
data class TaskTimeLogSummary(
    val taskId: Int,

    val taskTitle: String,

    val estimatedHours: Double?,

    val actualHours: Double,

    val differenceHours: Double?,

    val timeLogCount: Int,

    val contributorCount: Int,

    val lastWorkDateUtc: String?
)