package com.alperensarac.projectmanagementkotlin.data.remote.dto.projects

import kotlinx.serialization.Serializable

/**
 * PUT /api/Projects/{id}
 *
 * Backend UpdateProjectRequestDto karşılığıdır.
 *
 * Status için ayrı bir PATCH endpoint yoktur.
 * Proje durumu normal update request'i içerisinde değiştirilir.
 */
@Serializable
data class UpdateProjectRequestDto(
    val name: String,
    val description: String? = null,
    val startDate: String,
    val endDate: String? = null,
    val status: String,
    val ownerId: Int? = null
)