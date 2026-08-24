package com.alperensarac.projectmanagementkotlin.data.remote.dto.projects

import kotlinx.serialization.Serializable

/**
 * POST /api/Projects/{projectId}/members
 *
 * endpoint request modelidir.
 */
@Serializable
data class AddProjectMemberRequestDto(
    val userId: Int,

    /**
     * Backend enum string olarak serialize edilmektedir.
     *
     * Örnek:
     * Member
     * Contributor
     * Viewer
     */
    val role: String
)