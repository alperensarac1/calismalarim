package com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks

import kotlinx.serialization.Serializable

/**
 * PATCH /api/Tasks/{id}/assign
 *
 * assignedToUserId nullable olduğu için:
 *
 * {
 *     "assignedToUserId": null
 * }
 *
 * gönderilerek görev ataması kaldırılabilir.
 */
@Serializable
data class AssignTaskRequestDto(
    val assignedToUserId: Int?
)