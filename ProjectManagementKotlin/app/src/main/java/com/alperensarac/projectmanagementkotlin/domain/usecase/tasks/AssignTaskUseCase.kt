package com.alperensarac.projectmanagementkotlin.domain.usecase.tasks

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import javax.inject.Inject

/**
 * Görev atamasını değiştirir.
 *
 * assignedToUserId:
 *
 * Int  -> kullanıcıya ata
 * null -> atamayı kaldır
 */
class AssignTaskUseCase @Inject constructor(
    private val tasksRepository: TasksRepository
) {

    suspend operator fun invoke(
        taskId: Int,
        assignedToUserId: Int?
    ): AppResult<Task> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        if (assignedToUserId != null) {

            require(assignedToUserId > 0) {
                "User id sıfırdan büyük olmalıdır."
            }
        }

        return tasksRepository.assignTask(
            taskId = taskId,
            assignedToUserId = assignedToUserId
        )
    }
}