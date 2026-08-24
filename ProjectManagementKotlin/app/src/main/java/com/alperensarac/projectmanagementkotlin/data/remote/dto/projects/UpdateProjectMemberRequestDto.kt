package com.alperensarac.projectmanagementkotlin.data.remote.dto.projects

import kotlinx.serialization.Serializable

/**
 * PUT /api/Projects/{projectId}/members/{userId}
 *
 * endpoint request modelidir.
 */
@Serializable
data class UpdateProjectMemberRequestDto(
    val role: String
)