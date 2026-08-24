package com.alperensarac.projectmanagementkotlin.domain.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardRecentTask
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardSummary

/**
 * Dashboard işlemlerinin domain sözleşmesidir.
 */
interface DashboardRepository {

    suspend fun getSummary(): AppResult<DashboardSummary>

    suspend fun getRecentTasks(
        count: Int
    ): AppResult<List<DashboardRecentTask>>
}