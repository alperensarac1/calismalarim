package com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLogSummary
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskTimeLogsRepository
import javax.inject.Inject

class GetTaskTimeLogSummaryUseCase @Inject constructor(
    private val repository: TaskTimeLogsRepository
) {

    suspend operator fun invoke(
        taskId: Int
    ): AppResult<TaskTimeLogSummary> {

        require(taskId > 0)

        return repository.getSummary(taskId)
    }
}