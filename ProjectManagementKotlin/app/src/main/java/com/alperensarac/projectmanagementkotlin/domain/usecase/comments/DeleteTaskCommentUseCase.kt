package com.alperensarac.projectmanagementkotlin.domain.usecase.comments

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskCommentsRepository
import javax.inject.Inject

/**
 * Görev yorumunu siler.
 */
class DeleteTaskCommentUseCase @Inject constructor(
    private val taskCommentsRepository: TaskCommentsRepository
) {

    suspend operator fun invoke(
        taskId: Int,
        commentId: Int
    ): AppResult<Unit> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        require(commentId > 0) {
            "Comment id sıfırdan büyük olmalıdır."
        }

        return taskCommentsRepository.deleteComment(
            taskId = taskId,
            commentId = commentId
        )
    }
}