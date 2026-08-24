package com.alperensarac.projectmanagementkotlin.domain.repository

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectFilter
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import kotlinx.coroutines.flow.Flow

/**
 * Project ve ProjectMember işlemlerinin domain repository sözleşmesidir.
 *
 * Domain katmanı Retrofit DTO'larını bilmez.
 */
interface ProjectsRepository {

    // =========================================================================
    // LIST
    // =========================================================================

    fun getProjects(
        filter: ProjectFilter
    ): Flow<PagingData<Project>>

    // =========================================================================
    // DETAIL
    // =========================================================================

    suspend fun getProjectById(
        projectId: Int
    ): AppResult<Project>

    // =========================================================================
    // CREATE
    // =========================================================================

    suspend fun createProject(
        name: String,
        description: String?,
        startDate: String,
        endDate: String?,
        status: ProjectStatus,
        ownerId: Int?
    ): AppResult<Project>

    // =========================================================================
    // UPDATE
    // =========================================================================

    suspend fun updateProject(
        projectId: Int,
        name: String,
        description: String?,
        startDate: String,
        endDate: String?,
        status: ProjectStatus,
        ownerId: Int?
    ): AppResult<Project>

    // =========================================================================
    // ARCHIVE
    // =========================================================================

    suspend fun updateProjectArchiveStatus(
        projectId: Int,
        isArchived: Boolean
    ): AppResult<Project>

    // =========================================================================
    // DELETE
    // =========================================================================

    suspend fun deleteProject(
        projectId: Int
    ): AppResult<Unit>

    // =========================================================================
    // MEMBERS
    // =========================================================================

    suspend fun getProjectMembers(
        projectId: Int,
        includeInactive: Boolean = false
    ): AppResult<List<ProjectMember>>

    suspend fun addProjectMember(
        projectId: Int,
        userId: Int,
        role: String
    ): AppResult<ProjectMember>

    suspend fun updateProjectMemberRole(
        projectId: Int,
        userId: Int,
        role: String
    ): AppResult<ProjectMember>

    suspend fun removeProjectMember(
        projectId: Int,
        userId: Int
    ): AppResult<Unit>
}