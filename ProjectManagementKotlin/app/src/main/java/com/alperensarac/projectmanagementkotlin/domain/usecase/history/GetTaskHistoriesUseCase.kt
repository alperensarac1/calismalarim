package com.alperensarac.projectmanagementkotlin.domain.usecase.history

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.history.TaskHistory
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskHistoriesRepository
import javax.inject.Inject

/**
 * Bir görevin değişiklik geçmişini getirir.
 */
class GetTaskHistoriesUseCase @Inject constructor(
    private val taskHistoriesRepository: TaskHistoriesRepository
) {

    suspend operator fun invoke(
        taskId: Int
    ): AppResult<List<TaskHistory>> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        return taskHistoriesRepository.getHistories(
            taskId = taskId
        )
    }
}