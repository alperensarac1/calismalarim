package com.alperensarac.projectmanagementkotlin.data.remote.dto.comments

import kotlinx.serialization.Serializable

/**
 * Backend:
 *
 * ProjectManagement.Application.DTOs.Comments.UpdateCommentRequestDto
 *
 * PUT /api/tasks/{taskId}/comments/{commentId}
 */
@Serializable
data class UpdateCommentRequestDto(
    val content: String
)