package com.alperensarac.projectmanagementkotlin.data.mapper.history

import com.alperensarac.projectmanagementkotlin.data.remote.dto.history.TaskHistoryResponseDto
import com.alperensarac.projectmanagementkotlin.domain.model.history.TaskHistory

/**
 * Network DTO -> Domain model dönüşümü.
 */
fun TaskHistoryResponseDto.toDomain(): TaskHistory {

    return TaskHistory(
        id = id,
        taskId = taskId,

        changedByUserId = changedByUserId,
        changedByUserFullName = changedByUserFullName,
        changedByUserEmail = changedByUserEmail,

        changeType = changeType,

        oldValue = oldValue,
        newValue = newValue,

        description = description,

        createdAtUtc = createdAt
    )
}