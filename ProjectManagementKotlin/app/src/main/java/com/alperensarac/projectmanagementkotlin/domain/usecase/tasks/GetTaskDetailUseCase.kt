package com.alperensarac.projectmanagementkotlin.domain.usecase.tasks

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import javax.inject.Inject

/**
 * Tek bir görevin detay bilgilerini backend'den getirir.
 *
 * Endpoint:
 *
 * GET /api/Tasks/{id}
 */
class GetTaskDetailUseCase @Inject constructor(
    private val tasksRepository: TasksRepository
) {

    suspend operator fun invoke(
        taskId: Int
    ): AppResult<Task> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        return tasksRepository.getTaskById(
            taskId = taskId
        )
    }
}