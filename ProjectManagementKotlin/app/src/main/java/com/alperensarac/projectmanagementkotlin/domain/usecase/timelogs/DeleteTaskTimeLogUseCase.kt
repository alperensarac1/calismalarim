package com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskTimeLogsRepository
import javax.inject.Inject

class DeleteTaskTimeLogUseCase @Inject constructor(
    private val repository: TaskTimeLogsRepository
) {

    suspend operator fun invoke(
        taskId: Int,
        timeLogId: Int
    ): AppResult<Unit> {

        require(taskId > 0)
        require(timeLogId > 0)

        return repository.deleteTimeLog(
            taskId = taskId,
            timeLogId = timeLogId
        )
    }
}