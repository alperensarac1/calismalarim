package com.alperensarac.projectmanagementkotlin.data.remote.dto.projects

import kotlinx.serialization.Serializable

/**
 * Backend ProjectMemberResponseDto modelinin Android karşılığıdır.
 *
 * Backend:
 *
 * ProjectManagement.Application.DTOs.ProjectMembers.ProjectMemberResponseDto
 */
@Serializable
data class ProjectMemberResponseDto(
    /**
     * ProjectMember kaydının kendi primary key değeri.
     */
    val id: Int,

    /**
     * Üyenin bağlı olduğu proje.
     */
    val projectId: Int,

    /**
     * Gerçek kullanıcı id'si.
     */
    val userId: Int,

    val firstName: String,

    val lastName: String,

    val fullName: String,

    val email: String,

    /**
     * Kullanıcının sistem rolü.
     *
     * Örnek:
     *
     * Admin
     * ProjectManager
     * TeamMember
     */
    val systemRole: String,

    /**
     * Projedeki üyelik rolü.
     *
     * Member
     * Contributor
     * Viewer
     */
    val projectRole: String,

    /**
     * Projeye katıldığı tarih.
     */
    val joinedAt: String,

    /**
     * Kullanıcı hesabının aktif olup olmadığı.
     */
    val isActive: Boolean,

    /**
     * Bu üyenin proje sahibi olup olmadığını belirtir.
     */
    val isProjectOwner: Boolean
)