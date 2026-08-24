package com.alperensarac.projectmanagementkotlin.domain.model.projects

/**
 * Uygulama içerisinde kullanılan proje domain modelidir.
 *
 * Retrofit veya Kotlin Serialization bağımlılığı içermez.
 */
data class Project(
    val id: Int,
    val name: String,
    val description: String?,
    val startDateUtc: String,
    val endDateUtc: String?,
    val status: String,

    val ownerId: Int,
    val ownerFullName: String,
    val ownerEmail: String,

    val isArchived: Boolean,
    val archivedAtUtc: String?,

    val memberCount: Int,
    val taskCount: Int,

    val createdAtUtc: String,
    val updatedAtUtc: String?
)