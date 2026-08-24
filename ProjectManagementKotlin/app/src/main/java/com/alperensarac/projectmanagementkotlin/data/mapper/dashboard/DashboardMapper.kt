package com.alperensarac.projectmanagementkotlin.data.mapper.dashboard

import com.alperensarac.projectmanagementkotlin.data.remote.dto.dashboard.DashboardRecentTaskDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.dashboard.DashboardSummaryDto
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardRecentTask
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardSummary

/**
 * Dashboard summary DTO modelini domain modeline dönüştürür.
 */
fun DashboardSummaryDto.toDomain(): DashboardSummary {
    return DashboardSummary(
        totalProjectCount = totalProjectCount,
        activeProjectCount = activeProjectCount,
        planningProjectCount = planningProjectCount,
        completedProjectCount = completedProjectCount,
        archivedProjectCount = archivedProjectCount,

        totalTaskCount = totalTaskCount,
        todoTaskCount = todoTaskCount,
        inProgressTaskCount = inProgressTaskCount,
        inReviewTaskCount = inReviewTaskCount,
        doneTaskCount = doneTaskCount,
        overdueTaskCount = overdueTaskCount,

        myAssignedTaskCount = myAssignedTaskCount,
        myOverdueTaskCount = myOverdueTaskCount,

        totalEstimatedHours = totalEstimatedHours,
        totalActualHours = totalActualHours,
        myLoggedHours = myLoggedHours,
        taskCompletionPercentage = taskCompletionPercentage,
        timeUsagePercentage = timeUsagePercentage,

        generatedAtUtc = generatedAtUtc
    )
}

/**
 * Dashboard görev DTO modelini domain modeline dönüştürür.
 */
fun DashboardRecentTaskDto.toDomain(): DashboardRecentTask {
    return DashboardRecentTask(
        id = id,
        title = title,
        projectId = projectId,
        projectName = projectName,
        status = status,
        priority = priority,
        assignedToUserId = assignedToUserId,
        assignedToUserFullName = assignedToUserFullName,
        dueDateUtc = dueDate,
        isOverdue = isOverdue,
        createdAtUtc = createdAt,
        updatedAtUtc = updatedAt
    )
}