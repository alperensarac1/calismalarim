package com.alperensarac.projectmanagementkotlin.data.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.history.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.TaskHistoriesApi
import com.alperensarac.projectmanagementkotlin.domain.model.history.TaskHistory
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskHistoriesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TaskHistoriesRepository Retrofit implementasyonudur.
 */
@Singleton
class TaskHistoriesRepositoryImpl @Inject constructor(
    private val taskHistoriesApi: TaskHistoriesApi,
    private val networkErrorMapper: NetworkErrorMapper
) : TaskHistoriesRepository {

    override suspend fun getHistories(
        taskId: Int
    ): AppResult<List<TaskHistory>> {

        return try {

            val response =
                taskHistoriesApi.getHistories(
                    taskId = taskId
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors
                    )
                )
            }

            AppResult.Success(
                data =
                data.map { dto ->
                    dto.toDomain()
                },
                message =
                response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    private fun createBusinessError(
        message: String,
        errors: Map<String, List<String>>?
    ): NetworkError {

        if (!errors.isNullOrEmpty()) {

            val combinedMessage =
                errors.values
                    .flatten()
                    .joinToString("\n")
                    .ifBlank {
                        message.ifBlank {
                            "Görev geçmişi alınamadı."
                        }
                    }

            return NetworkError.Validation(
                message = combinedMessage,
                fieldErrors = errors
            )
        }

        return NetworkError.Unknown(
            message =
            message.ifBlank {
                "Görev geçmişi alınamadı."
            }
        )
    }
}