package com.alperensarac.projectmanagementkotlin.data.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.timelogs.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.TaskTimeLogsApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.CreateTaskTimeLogRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs.UpdateTaskTimeLogRequestDto
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLogSummary
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskTimeLogsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TaskTimeLogsRepository Retrofit implementasyonudur.
 */
@Singleton
class TaskTimeLogsRepositoryImpl @Inject constructor(
    private val api: TaskTimeLogsApi,
    private val networkErrorMapper: NetworkErrorMapper
) : TaskTimeLogsRepository {

    override suspend fun getTimeLogs(
        taskId: Int
    ): AppResult<List<TaskTimeLog>> {

        return try {

            val response =
                api.getTimeLogs(taskId)

            val data =
                response.data

            if (!response.success || data == null) {

                return AppResult.Error(
                    businessError(
                        response.message,
                        response.errors,
                        "Zaman kayıtları alınamadı."
                    )
                )
            }

            AppResult.Success(
                data = data.map { it.toDomain() },
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun getSummary(
        taskId: Int
    ): AppResult<TaskTimeLogSummary> {

        return try {

            val response =
                api.getSummary(taskId)

            val data =
                response.data

            if (!response.success || data == null) {

                return AppResult.Error(
                    businessError(
                        response.message,
                        response.errors,
                        "Zaman kaydı özeti alınamadı."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun createTimeLog(
        taskId: Int,
        hours: Double,
        description: String?,
        workDate: String
    ): AppResult<TaskTimeLog> {

        return try {

            val response =
                api.createTimeLog(
                    taskId = taskId,

                    request =
                    CreateTaskTimeLogRequestDto(
                        hours = hours,
                        description = description,
                        workDate = workDate
                    )
                )

            val data =
                response.data

            if (!response.success || data == null) {

                return AppResult.Error(
                    businessError(
                        response.message,
                        response.errors,
                        "Zaman kaydı eklenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun updateTimeLog(
        taskId: Int,
        timeLogId: Int,
        hours: Double,
        description: String?,
        workDate: String
    ): AppResult<TaskTimeLog> {

        return try {

            val response =
                api.updateTimeLog(
                    taskId = taskId,
                    timeLogId = timeLogId,

                    request =
                    UpdateTaskTimeLogRequestDto(
                        hours = hours,
                        description = description,
                        workDate = workDate
                    )
                )

            val data =
                response.data

            if (!response.success || data == null) {

                return AppResult.Error(
                    businessError(
                        response.message,
                        response.errors,
                        "Zaman kaydı güncellenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun deleteTimeLog(
        taskId: Int,
        timeLogId: Int
    ): AppResult<Unit> {

        return try {

            val response =
                api.deleteTimeLog(
                    taskId = taskId,
                    timeLogId = timeLogId
                )

            if (!response.success) {

                return AppResult.Error(
                    businessError(
                        response.message,
                        response.errors,
                        "Zaman kaydı silinemedi."
                    )
                )
            }

            AppResult.Success(
                data = Unit,
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(throwable)
            )
        }
    }

    private fun businessError(
        message: String,
        errors: Map<String, List<String>>?,
        fallbackMessage: String
    ): NetworkError {

        if (!errors.isNullOrEmpty()) {

            return NetworkError.Validation(
                message =
                errors.values
                    .flatten()
                    .joinToString("\n")
                    .ifBlank {
                        message.ifBlank {
                            fallbackMessage
                        }
                    },

                fieldErrors = errors
            )
        }

        return NetworkError.Unknown(
            message =
            message.ifBlank {
                fallbackMessage
            }
        )
    }
}