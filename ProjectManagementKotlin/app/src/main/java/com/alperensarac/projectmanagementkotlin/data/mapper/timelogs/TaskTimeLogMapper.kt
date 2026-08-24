package com.alperensarac.projectmanagementkotlin.data.mapper.timelogs

import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.TaskTimeLogResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.TaskTimeLogSummaryDto
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLogSummary

/**
 * Network DTO -> Domain.
 */
fun TaskTimeLogResponseDto.toDomain(): TaskTimeLog {

    return TaskTimeLog(
        id = id,
        taskId = taskId,

        userId = userId,
        userFullName = userFullName,
        userEmail = userEmail,

        hours = hours,

        description = description,

        workDateUtc = workDate,
        createdAtUtc = createdAt,

        canEdit = canEdit,
        canDelete = canDelete
    )
}

/**
 * Summary DTO -> Domain.
 */
fun TaskTimeLogSummaryDto.toDomain(): TaskTimeLogSummary {

    return TaskTimeLogSummary(
        taskId = taskId,
        taskTitle = taskTitle,

        estimatedHours = estimatedHours,
        actualHours = actualHours,
        differenceHours = differenceHours,

        timeLogCount = timeLogCount,
        contributorCount = contributorCount,

        lastWorkDateUtc = lastWorkDate
    )
}