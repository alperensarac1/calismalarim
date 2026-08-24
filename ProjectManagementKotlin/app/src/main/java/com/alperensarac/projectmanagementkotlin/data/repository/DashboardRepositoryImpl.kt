package com.alperensarac.projectmanagementkotlin.data.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.dashboard.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.DashboardApi
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardRecentTask
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardSummary
import com.alperensarac.projectmanagementkotlin.domain.repository.DashboardRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DashboardRepository interface'inin Retrofit tabanlı implementasyonudur.
 */
@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val networkErrorMapper: NetworkErrorMapper
) : DashboardRepository {

    override suspend fun getSummary(): AppResult<DashboardSummary> {
        return try {
            val response = dashboardApi.getSummary()
            val responseData = response.data

            if (!response.success || responseData == null) {
                return AppResult.Error(
                    error = createBusinessError(
                        message = response.message,
                        errors = response.errors
                    )
                )
            }

            AppResult.Success(
                data = responseData.toDomain(),
                message = response.message
            )
        } catch (throwable: Throwable) {
            AppResult.Error(
                error = networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun getRecentTasks(
        count: Int
    ): AppResult<List<DashboardRecentTask>> {
        return try {
            val response = dashboardApi.getRecentTasks(
                count = count
            )

            val responseData = response.data

            if (!response.success || responseData == null) {
                return AppResult.Error(
                    error = createBusinessError(
                        message = response.message,
                        errors = response.errors
                    )
                )
            }

            AppResult.Success(
                data = responseData.map { dto ->
                    dto.toDomain()
                },
                message = response.message
            )
        } catch (throwable: Throwable) {
            AppResult.Error(
                error = networkErrorMapper.map(throwable)
            )
        }
    }

    /**
     * HTTP başarılı olsa bile success=false dönen business hatalarını
     * uygulama hata modeline dönüştürür.
     */
    private fun createBusinessError(
        message: String,
        errors: Map<String, List<String>>?
    ): NetworkError {
        if (!errors.isNullOrEmpty()) {
            val combinedMessage = errors
                .values
                .flatten()
                .joinToString(separator = "\n")

            return NetworkError.Validation(
                message = combinedMessage.ifBlank {
                    message.ifBlank {
                        "Dashboard verileri alınamadı."
                    }
                },
                fieldErrors = errors
            )
        }

        return NetworkError.Unknown(
            message = message.ifBlank {
                "Dashboard verileri alınamadı."
            }
        )
    }
}