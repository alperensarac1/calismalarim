package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.data.remote.dto.history.TaskHistoryResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Görev geçmişi Retrofit servisi.
 *
 * Backend Controller:
 *
 * [Route("api/tasks/{taskId:int}/histories")]
 */
interface TaskHistoriesApi {

    /**
     * Bir göreve ait bütün geçmiş kayıtlarını getirir.
     *
     * GET /api/tasks/{taskId}/histories
     */
    @GET("api/tasks/{taskId}/histories")
    suspend fun getHistories(
        @Path("taskId")
        taskId: Int
    ): ApiResponse<List<TaskHistoryResponseDto>>
}