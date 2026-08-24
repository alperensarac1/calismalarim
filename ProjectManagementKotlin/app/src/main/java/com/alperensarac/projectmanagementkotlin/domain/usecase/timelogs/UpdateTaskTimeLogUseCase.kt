package com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskTimeLogsRepository
import javax.inject.Inject

class UpdateTaskTimeLogUseCase @Inject constructor(
    private val repository: TaskTimeLogsRepository
) {

    suspend operator fun invoke(
        taskId: Int,
        timeLogId: Int,
        hours: Double,
        description: String?,
        workDate: String
    ): AppResult<TaskTimeLog> {

        require(taskId > 0)
        require(timeLogId > 0)

        require(hours > 0.0) {
            "Çalışma süresi sıfırdan büyük olmalıdır."
        }

        require(workDate.isNotBlank()) {
            "Çalışma tarihi boş olamaz."
        }

        return repository.updateTimeLog(
            taskId = taskId,
            timeLogId = timeLogId,
            hours = hours,
            description =
            description
                ?.trim()
                ?.takeIf { it.isNotBlank() },
            workDate = workDate
        )
    }
}