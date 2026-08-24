package com.alperensarac.projectmanagementkotlin.domain.model.dashboard

/**
 * Uygulama içerisindeki Dashboard özet modelidir.
 *
 * Network veya serialization bağımlılığı içermez.
 */
data class DashboardSummary(
    val totalProjectCount: Int,
    val activeProjectCount: Int,
    val planningProjectCount: Int,
    val completedProjectCount: Int,
    val archivedProjectCount: Int,

    val totalTaskCount: Int,
    val todoTaskCount: Int,
    val inProgressTaskCount: Int,
    val inReviewTaskCount: Int,
    val doneTaskCount: Int,
    val overdueTaskCount: Int,

    val myAssignedTaskCount: Int,
    val myOverdueTaskCount: Int,

    val totalEstimatedHours: Double,
    val totalActualHours: Double,
    val myLoggedHours: Double,
    val taskCompletionPercentage: Double,
    val timeUsagePercentage: Double,

    val generatedAtUtc: String
)