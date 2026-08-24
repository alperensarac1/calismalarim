package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.core.network.model.PagedResult
import com.alperensarac.projectmanagementkotlin.data.remote.dto.common.EmptyObjectDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.AssignTaskRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.CreateTaskRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.TaskResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.UpdateTaskRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.UpdateTaskStatusRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * ASP.NET Core TasksController Retrofit karşılığıdır.
 */
interface TasksApi {

    // =========================================================================
    // LIST
    // =========================================================================

    @GET("api/Tasks")
    suspend fun getTasks(
        @Query("Page")
        page: Int,

        @Query("PageSize")
        pageSize: Int,

        @Query("Search")
        search: String? = null,

        @Query("ProjectId")
        projectId: Int? = null,

        @Query("AssignedToUserId")
        assignedToUserId: Int? = null,

        @Query("Status")
        status: String? = null,

        @Query("Priority")
        priority: String? = null,

        @Query("IsOverdue")
        isOverdue: Boolean? = null
    ): ApiResponse<PagedResult<TaskResponseDto>>

    // =========================================================================
    // DETAIL
    // =========================================================================

    @GET("api/Tasks/{id}")
    suspend fun getTaskById(
        @Path("id")
        taskId: Int
    ): ApiResponse<TaskResponseDto>

    // =========================================================================
    // CREATE
    // =========================================================================

    /**
     * POST /api/Tasks
     *
     * Backend CreatedAtAction kullandığı için HTTP 201 döner.
     */
    @POST("api/Tasks")
    suspend fun createTask(
        @Body
        request: CreateTaskRequestDto
    ): ApiResponse<TaskResponseDto>

    // =========================================================================
    // UPDATE
    // =========================================================================

    @PUT("api/Tasks/{id}")
    suspend fun updateTask(
        @Path("id")
        taskId: Int,

        @Body
        request: UpdateTaskRequestDto
    ): ApiResponse<TaskResponseDto>

    // =========================================================================
    // STATUS
    // =========================================================================

    @PATCH("api/Tasks/{id}/status")
    suspend fun updateTaskStatus(
        @Path("id")
        taskId: Int,

        @Body
        request: UpdateTaskStatusRequestDto
    ): ApiResponse<TaskResponseDto>

    // =========================================================================
    // ASSIGN
    // =========================================================================

    @PATCH("api/Tasks/{id}/assign")
    suspend fun assignTask(
        @Path("id")
        taskId: Int,

        @Body
        request: AssignTaskRequestDto
    ): ApiResponse<TaskResponseDto>

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * DELETE /api/Tasks/{id}
     */
    @DELETE("api/Tasks/{id}")
    suspend fun deleteTask(
        @Path("id")
        taskId: Int
    ): ApiResponse<EmptyObjectDto>
}