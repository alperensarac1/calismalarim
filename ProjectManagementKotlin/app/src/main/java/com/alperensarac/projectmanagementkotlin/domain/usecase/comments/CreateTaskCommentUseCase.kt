package com.alperensarac.projectmanagementkotlin.domain.usecase.comments

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskCommentsRepository
import javax.inject.Inject

/**
 * Yeni görev yorumu oluşturur.
 */
class CreateTaskCommentUseCase @Inject constructor(
    private val taskCommentsRepository: TaskCommentsRepository
) {

    suspend operator fun invoke(
        taskId: Int,
        content: String
    ): AppResult<TaskComment> {

        require(taskId > 0) {
            "Task id sıfırdan büyük olmalıdır."
        }

        val normalizedContent =
            content.trim()

        require(normalizedContent.isNotBlank()) {
            "Yorum içeriği boş olamaz."
        }

        return taskCommentsRepository.createComment(
            taskId = taskId,
            content = normalizedContent
        )
    }
}