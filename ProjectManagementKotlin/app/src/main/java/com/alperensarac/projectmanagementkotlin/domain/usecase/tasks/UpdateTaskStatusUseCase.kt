package com.alperensarac.projectmanagementkotlin.domain.usecase.tasks

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import javax.inject.Inject

/**
 * Görevin durumunu değiştirir.
 */
class UpdateTaskStatusUseCase @Inject constructor(
    private val tasksRepository: TasksRepository
) {

    suspend operator fun invoke(
        taskId: Int,
        status: TaskStatus
    ): AppResult<Task> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        return tasksRepository.updateTaskStatus(
            taskId = taskId,
            status = status
        )
    }
}