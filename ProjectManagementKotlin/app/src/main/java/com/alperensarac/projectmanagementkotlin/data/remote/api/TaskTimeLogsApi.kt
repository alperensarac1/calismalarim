package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.data.remote.dto.common.EmptyObjectDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.CreateTaskTimeLogRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.TaskTimeLogResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.TaskTimeLogSummaryDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.UpdateTaskTimeLogRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Görev zaman kayıtlarının Retrofit API tanımıdır.
 */
interface TaskTimeLogsApi {

    /**
     * Görevin bütün zaman kayıtlarını getirir.
     */
    @GET("api/tasks/{taskId}/time-logs")
    suspend fun getTimeLogs(
        @Path("taskId")
        taskId: Int
    ): ApiResponse<List<TaskTimeLogResponseDto>>

    /**
     * Görevin zaman kaydı özetini getirir.
     */
    @GET("api/tasks/{taskId}/time-logs/summary")
    suspend fun getSummary(
        @Path("taskId")
        taskId: Int
    ): ApiResponse<TaskTimeLogSummaryDto>

    /**
     * Yeni zaman kaydı oluşturur.
     *
     * HTTP 201
     */
    @POST("api/tasks/{taskId}/time-logs")
    suspend fun createTimeLog(
        @Path("taskId")
        taskId: Int,

        @Body
        request: CreateTaskTimeLogRequestDto
    ): ApiResponse<TaskTimeLogResponseDto>

    /**
     * Zaman kaydını günceller.
     */
    @PUT("api/tasks/{taskId}/time-logs/{timeLogId}")
    suspend fun updateTimeLog(
        @Path("taskId")
        taskId: Int,

        @Path("timeLogId")
        timeLogId: Int,

        @Body
        request: UpdateTaskTimeLogRequestDto
    ): ApiResponse<TaskTimeLogResponseDto>

    /**
     * Zaman kaydını siler.
     */
    @DELETE("api/tasks/{taskId}/time-logs/{timeLogId}")
    suspend fun deleteTimeLog(
        @Path("taskId")
        taskId: Int,

        @Path("timeLogId")
        timeLogId: Int
    ): ApiResponse<EmptyObjectDto>
}