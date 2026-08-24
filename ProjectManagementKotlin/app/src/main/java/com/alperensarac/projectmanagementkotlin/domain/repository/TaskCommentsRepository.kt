package com.alperensarac.projectmanagementkotlin.domain.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment

/**
 * Görev yorumlarının domain repository sözleşmesidir.
 */
interface TaskCommentsRepository {

    suspend fun getComments(
        taskId: Int
    ): AppResult<List<TaskComment>>

    suspend fun createComment(
        taskId: Int,
        content: String
    ): AppResult<TaskComment>

    /**
     * Mevcut yorumu günceller.
     */
    suspend fun updateComment(
        taskId: Int,
        commentId: Int,
        content: String
    ): AppResult<TaskComment>

    suspend fun deleteComment(
        taskId: Int,
        commentId: Int
    ): AppResult<Unit>
}