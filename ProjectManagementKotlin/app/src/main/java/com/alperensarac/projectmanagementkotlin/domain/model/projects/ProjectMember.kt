package com.alperensarac.projectmanagementkotlin.domain.model.projects

/**
 * Uygulama içerisindeki proje üyesi domain modelidir.
 *
 * Network veya Retrofit bağımlılığı içermez.
 */
data class ProjectMember(
    val id: Int,
    val projectId: Int,
    val userId: Int,

    val firstName: String,
    val lastName: String,
    val fullName: String,
    val email: String,

    val systemRole: String,
    val projectRole: String,

    val joinedAtUtc: String,

    val isActive: Boolean,
    val isProjectOwner: Boolean
)