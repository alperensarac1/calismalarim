package com.alperensarac.projectmanagementkotlin.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

/**
 * GET /api/Dashboard/summary endpointinin data modelidir.
 *
 * Bu sınıf backend JSON sözleşmesini birebir temsil eder.
 * UI katmanında doğrudan kullanılmaz; domain modeline dönüştürülür.
 */
@Serializable
data class DashboardSummaryDto(
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

    /*
     * Backend tarafındaki decimal alanlar Kotlin'de Double olarak tutulur.
     */
    val totalEstimatedHours: Double,
    val totalActualHours: Double,
    val myLoggedHours: Double,
    val taskCompletionPercentage: Double,
    val timeUsagePercentage: Double,

    /*
     * Backend UTC tarih döndürmektedir.
     */
    val generatedAtUtc: String
)