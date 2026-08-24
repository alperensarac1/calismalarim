package com.alperensarac.projectmanagementkotlin.domain.usecase.comments

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskCommentsRepository
import javax.inject.Inject

/**
 * Görev yorumlarını getirir.
 */
class GetTaskCommentsUseCase @Inject constructor(
    private val taskCommentsRepository: TaskCommentsRepository
) {

    suspend operator fun invoke(
        taskId: Int
    ): AppResult<List<TaskComment>> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        return taskCommentsRepository.getComments(
            taskId = taskId
        )
    }
}