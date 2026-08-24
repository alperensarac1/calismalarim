package com.alperensarac.projectmanagementkotlin.domain.usecase.dashboard

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardRecentTask
import com.alperensarac.projectmanagementkotlin.domain.repository.DashboardRepository
import javax.inject.Inject

/**
 * Dashboard üzerinde gösterilecek son görevleri getirir.
 */
class GetRecentTasksUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {

    suspend operator fun invoke(
        count: Int = DEFAULT_TASK_COUNT
    ): AppResult<List<DashboardRecentTask>> {
        require(count in MINIMUM_TASK_COUNT..MAXIMUM_TASK_COUNT) {
            "Son görev sayısı $MINIMUM_TASK_COUNT ile $MAXIMUM_TASK_COUNT arasında olmalıdır."
        }

        return dashboardRepository.getRecentTasks(
            count = count
        )
    }

    private companion object {
        const val DEFAULT_TASK_COUNT = 10
        const val MINIMUM_TASK_COUNT = 1
        const val MAXIMUM_TASK_COUNT = 50
    }
}