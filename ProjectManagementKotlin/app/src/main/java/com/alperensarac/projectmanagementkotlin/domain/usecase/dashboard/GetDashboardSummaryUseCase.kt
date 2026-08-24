package com.alperensarac.projectmanagementkotlin.domain.usecase.dashboard

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardSummary
import com.alperensarac.projectmanagementkotlin.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Dashboard özet verilerini getirir.
 */
class GetDashboardSummaryUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {

    suspend operator fun invoke(): AppResult<DashboardSummary> {
        return dashboardRepository.getSummary()
    }
}