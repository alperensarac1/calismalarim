package com.alperensarac.projectmanagementkotlin.data.remote.dto.projects

import kotlinx.serialization.Serializable


@Serializable
data class ProjectResponseDto(
    val id: Int,
    val name: String,
    val description: String? = null,

    /*
     * Backend DateTime alanlarını şimdilik ISO tarih String olarak
     * taşıyoruz.
     */
    val startDate: String,
    val endDate: String? = null,

    /*
     * Backend enum JSON içerisinde string olarak dönüyor:
     *
     * Planning
     * Active
     * ...
     */
    val status: String,

    val ownerId: Int,
    val ownerFullName: String,
    val ownerEmail: String,

    val isArchived: Boolean,
    val archivedAt: String? = null,

    val memberCount: Int,
    val taskCount: Int,

    val createdAt: String,
    val updatedAt: String? = null
)