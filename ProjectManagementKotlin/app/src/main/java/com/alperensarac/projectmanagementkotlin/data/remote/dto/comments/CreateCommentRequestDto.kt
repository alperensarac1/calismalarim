package com.alperensarac.projectmanagementkotlin.data.remote.dto.comments

import kotlinx.serialization.Serializable

/**
 * POST /api/tasks/{taskId}/comments
 *
 * request body'sidir.
 *
 * Backend:
 *
 * public sealed class CreateCommentRequestDto
 * {
 *     public string Content { get; set; }
 * }
 */
@Serializable
data class CreateCommentRequestDto(
    val content: String
)