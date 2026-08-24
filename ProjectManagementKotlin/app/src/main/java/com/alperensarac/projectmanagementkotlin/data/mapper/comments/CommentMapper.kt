package com.alperensarac.projectmanagementkotlin.data.mapper.comments

import com.alperensarac.projectmanagementkotlin.data.remote.dto.comments.CommentResponseDto
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment

/**
 * Network DTO -> Domain model.
 */
fun CommentResponseDto.toDomain(): TaskComment {

    return TaskComment(
        id = id,
        taskId = taskId,

        userId = userId,
        userFullName = userFullName,
        userEmail = userEmail,

        content = content,

        createdAtUtc = createdAt,
        updatedAtUtc = updatedAt,

        canEdit = canEdit,
        canDelete = canDelete
    )
}