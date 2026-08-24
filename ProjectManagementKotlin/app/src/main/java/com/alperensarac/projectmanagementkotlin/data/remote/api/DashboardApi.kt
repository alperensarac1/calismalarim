package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.data.remote.dto.dashboard.DashboardRecentTaskDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.dashboard.DashboardSummaryDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Dashboard endpointlerini tanımlayan Retrofit servisidir.
 *
 * Bu endpointler korumalıdır. Authorization header,
 * AuthInterceptor tarafından otomatik olarak eklenecektir.
 */
interface DashboardApi {

    /**
     * Proje, görev ve zaman özeti getirir.
     */
    @GET("api/Dashboard/summary")
    suspend fun getSummary(): ApiResponse<DashboardSummaryDto>

    /**
     * En son güncellenen veya oluşturulan görevleri getirir.
     *
     * Varsayılan count değeri 10'dur.
     */
    @GET("api/Dashboard/recent-tasks")
    suspend fun getRecentTasks(
        @Query("count") count: Int = 10
    ): ApiResponse<List<DashboardRecentTaskDto>>
}