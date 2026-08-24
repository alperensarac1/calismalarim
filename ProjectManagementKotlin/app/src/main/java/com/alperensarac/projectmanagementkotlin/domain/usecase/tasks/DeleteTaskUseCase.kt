package com.alperensarac.projectmanagementkotlin.domain.usecase.tasks

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val tasksRepository: TasksRepository
) {

    suspend operator fun invoke(
        taskId: Int
    ): AppResult<Unit> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        return tasksRepository.deleteTask(
            taskId
        )
    }
}