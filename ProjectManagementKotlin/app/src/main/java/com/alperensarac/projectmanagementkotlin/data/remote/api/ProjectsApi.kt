package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.core.network.model.PagedResult
import com.alperensarac.projectmanagementkotlin.data.remote.dto.common.EmptyObjectDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.AddProjectMemberRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.CreateProjectRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.ProjectMemberResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.ProjectResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.UpdateProjectArchiveRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.UpdateProjectMemberRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.UpdateProjectRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Project ve ProjectMember endpointlerini tanımlayan Retrofit servisidir.
 *
 * Authentication altyapısının hangi Retrofit/OkHttp instance'ı üzerinden
 * sağlanacağı Hilt network katmanının sorumluluğundadır.
 */
interface ProjectsApi {

    // =========================================================================
    // PROJECT LIST
    // =========================================================================

    @GET("api/Projects")
    suspend fun getProjects(
        @Query("Page")
        page: Int,

        @Query("PageSize")
        pageSize: Int,

        @Query("Search")
        search: String? = null,

        @Query("Status")
        status: String? = null,

        @Query("IsArchived")
        isArchived: Boolean? = null,

        @Query("OwnerId")
        ownerId: Int? = null
    ): ApiResponse<PagedResult<ProjectResponseDto>>

    // =========================================================================
    // PROJECT DETAIL
    // =========================================================================

    @GET("api/Projects/{id}")
    suspend fun getProjectById(
        @Path("id")
        projectId: Int
    ): ApiResponse<ProjectResponseDto>

    // =========================================================================
    // CREATE PROJECT
    // =========================================================================

    @POST("api/Projects")
    suspend fun createProject(
        @Body
        request: CreateProjectRequestDto
    ): ApiResponse<ProjectResponseDto>

    // =========================================================================
    // UPDATE PROJECT
    // =========================================================================

    /**
     * Projenin:
     *
     * - adı
     * - açıklaması
     * - başlangıç tarihi
     * - bitiş tarihi
     * - durumu
     * - sahibi
     *
     * alanlarını günceller.
     */
    @PUT("api/Projects/{id}")
    suspend fun updateProject(
        @Path("id")
        projectId: Int,

        @Body
        request: UpdateProjectRequestDto
    ): ApiResponse<ProjectResponseDto>

    // =========================================================================
    // ARCHIVE PROJECT
    // =========================================================================

    /**
     * Projeyi arşivler veya arşivden çıkarır.
     */
    @PATCH("api/Projects/{id}/archive")
    suspend fun updateProjectArchiveStatus(
        @Path("id")
        projectId: Int,

        @Body
        request: UpdateProjectArchiveRequestDto
    ): ApiResponse<ProjectResponseDto>

    // =========================================================================
    // DELETE PROJECT
    // =========================================================================

    /**
     * Projeyi tamamen siler.
     */
    @DELETE("api/Projects/{id}")
    suspend fun deleteProject(
        @Path("id")
        projectId: Int
    ): ApiResponse<EmptyObjectDto>

    // =========================================================================
    // PROJECT MEMBERS
    // =========================================================================

    @GET("api/projects/{projectId}/members")
    suspend fun getProjectMembers(
        @Path("projectId")
        projectId: Int,

        @Query("includeInactive")
        includeInactive: Boolean = false
    ): ApiResponse<List<ProjectMemberResponseDto>>

    @POST("api/projects/{projectId}/members")
    suspend fun addProjectMember(
        @Path("projectId")
        projectId: Int,

        @Body
        request: AddProjectMemberRequestDto
    ): ApiResponse<ProjectMemberResponseDto>

    @PUT("api/projects/{projectId}/members/{userId}")
    suspend fun updateProjectMember(
        @Path("projectId")
        projectId: Int,

        @Path("userId")
        userId: Int,

        @Body
        request: UpdateProjectMemberRequestDto
    ): ApiResponse<ProjectMemberResponseDto>

    @DELETE("api/projects/{projectId}/members/{userId}")
    suspend fun removeProjectMember(
        @Path("projectId")
        projectId: Int,

        @Path("userId")
        userId: Int
    ): ApiResponse<EmptyObjectDto>
}