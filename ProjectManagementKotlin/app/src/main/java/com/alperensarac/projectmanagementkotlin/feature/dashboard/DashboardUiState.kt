package com.alperensarac.projectmanagementkotlin.feature.dashboard

import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardRecentTask
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardSummary

/**
 * Dashboard ekranının kalıcı UI durumudur.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val summary: DashboardSummary? = null,
    val recentTasks: List<DashboardRecentTask> = emptyList(),
    val errorMessage: String? = null
) {

    val hasContent: Boolean
        get() = summary != null

    val isRecentTasksEmpty: Boolean
        get() = recentTasks.isEmpty()
}