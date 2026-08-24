package com.alperensarac.projectmanagementkotlin.domain.model.history

/**
 * Görev geçmişinin domain modelidir.
 *
 * Network DTO bağımlılığı içermez.
 */
data class TaskHistory(
    val id: Int,

    val taskId: Int,

    val changedByUserId: Int,

    val changedByUserFullName: String,

    val changedByUserEmail: String,

    val changeType: String,

    val oldValue: String?,

    val newValue: String?,

    val description: String?,

    val createdAtUtc: String
)