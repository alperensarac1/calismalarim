package com.alperensarac.projectmanagementkotlin.data.mapper.tasks

import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.TaskResponseDto
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task

/**
 * TaskResponseDto -> Task dönüşümü.
 */
fun TaskResponseDto.toDomain(): Task {

    return Task(
        id = id,
        title = title,
        description = description,

        projectId = projectId,
        projectName = projectName,

        assignedToUserId = assignedToUserId,
        assignedToUserFullName = assignedToUserFullName,

        createdByUserId = createdByUserId,
        createdByUserFullName = createdByUserFullName,

        status = status,
        priority = priority,

        dueDateUtc = dueDate,

        estimatedHours = estimatedHours,
        actualHours = actualHours,

        completedAtUtc = completedAt,

        isOverdue = isOverdue,

        commentCount = commentCount,

        createdAtUtc = createdAt,
        updatedAtUtc = updatedAt
    )
}