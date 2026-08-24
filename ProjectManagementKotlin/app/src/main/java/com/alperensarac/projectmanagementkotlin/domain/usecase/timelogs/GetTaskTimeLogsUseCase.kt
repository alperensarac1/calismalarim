package com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskTimeLogsRepository
import javax.inject.Inject

class GetTaskTimeLogsUseCase @Inject constructor(
    private val repository: TaskTimeLogsRepository
) {

    suspend operator fun invoke(
        taskId: Int
    ): AppResult<List<TaskTimeLog>> {

        require(taskId > 0)

        return repository.getTimeLogs(taskId)
    }
}