package com.alperensarac.projectmanagementkotlin.data.remote.dto.projects

import kotlinx.serialization.Serializable

/**
 * PATCH /api/Projects/{id}/archive
 *
 * true  -> arşivle
 * false -> arşivden çıkar
 */
@Serializable
data class UpdateProjectArchiveRequestDto(
    val isArchived: Boolean
)