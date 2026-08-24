package com.alperensarac.projectmanagementkotlin.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.tasks.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.TasksApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.AssignTaskRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.CreateTaskRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.UpdateTaskRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.UpdateTaskStatusRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.paging.TasksPagingSource
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskFilter
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class TasksRepositoryImpl @Inject constructor(
    private val tasksApi: TasksApi,
    private val networkErrorMapper: NetworkErrorMapper
) : TasksRepository {

    // =========================================================================
    // LIST
    // =========================================================================

    override fun getTasks(
        filter: TaskFilter
    ): Flow<PagingData<Task>> {

        return Pager(
            config =
            PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),

            pagingSourceFactory = {

                TasksPagingSource(
                    tasksApi = tasksApi,
                    filter = filter
                )
            }
        ).flow
    }

    // =========================================================================
    // DETAIL
    // =========================================================================

    override suspend fun getTaskById(
        taskId: Int
    ): AppResult<Task> {

        return executeTaskRequest(
            fallbackMessage =
            "Görev bilgileri alınamadı."
        ) {

            tasksApi.getTaskById(
                taskId
            )
        }
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    override suspend fun createTask(
        projectId: Int,
        title: String,
        description: String?,
        assignedToUserId: Int?,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: String?,
        estimatedHours: Double?
    ): AppResult<Task> {

        return executeTaskRequest(
            fallbackMessage =
            "Görev oluşturulamadı."
        ) {

            tasksApi.createTask(
                request =
                CreateTaskRequestDto(
                    projectId = projectId,
                    title = title,
                    description = description,
                    assignedToUserId = assignedToUserId,
                    status = status.apiValue,
                    priority = priority.apiValue,
                    dueDate = dueDate,
                    estimatedHours = estimatedHours
                )
            )
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    override suspend fun updateTask(
        taskId: Int,
        title: String,
        description: String?,
        assignedToUserId: Int?,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: String?,
        estimatedHours: Double?
    ): AppResult<Task> {

        return executeTaskRequest(
            fallbackMessage =
            "Görev güncellenemedi."
        ) {

            tasksApi.updateTask(
                taskId = taskId,

                request =
                UpdateTaskRequestDto(
                    title = title,
                    description = description,
                    assignedToUserId = assignedToUserId,
                    status = status.apiValue,
                    priority = priority.apiValue,
                    dueDate = dueDate,
                    estimatedHours = estimatedHours
                )
            )
        }
    }

    // =========================================================================
    // STATUS
    // =========================================================================

    override suspend fun updateTaskStatus(
        taskId: Int,
        status: TaskStatus
    ): AppResult<Task> {

        return executeTaskRequest(
            fallbackMessage =
            "Görev durumu değiştirilemedi."
        ) {

            tasksApi.updateTaskStatus(
                taskId = taskId,

                request =
                UpdateTaskStatusRequestDto(
                    status =
                    status.apiValue
                )
            )
        }
    }

    // =========================================================================
    // ASSIGN
    // =========================================================================

    override suspend fun assignTask(
        taskId: Int,
        assignedToUserId: Int?
    ): AppResult<Task> {

        return executeTaskRequest(
            fallbackMessage =
            "Görev ataması değiştirilemedi."
        ) {

            tasksApi.assignTask(
                taskId = taskId,

                request =
                AssignTaskRequestDto(
                    assignedToUserId =
                    assignedToUserId
                )
            )
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    override suspend fun deleteTask(
        taskId: Int
    ): AppResult<Unit> {

        return try {

            val response =
                tasksApi.deleteTask(
                    taskId
                )

            if (!response.success) {

                return AppResult.Error(
                    createBusinessError(
                        message =
                        response.message,

                        errors =
                        response.errors,

                        fallbackMessage =
                        "Görev silinemedi."
                    )
                )
            }

            AppResult.Success(
                data = Unit,
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // COMMON TASK RESPONSE
    // =========================================================================

    /**
     * TaskResponseDto döndüren endpointlerde aynı başarı/hata kodunu
     * tekrar tekrar yazmamak için ortaklaştırdık.
     */
    private suspend fun executeTaskRequest(
        fallbackMessage: String,
        request:
        suspend () ->
        com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse<
                com.alperensarac.projectmanagementkotlin.data.remote.dto.tasks.TaskResponseDto
                >
    ): AppResult<Task> {

        return try {

            val response =
                request()

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage = fallbackMessage
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
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

                fieldErrors =
                errors
            )
        }

        return NetworkError.Unknown(
            message =
            message.ifBlank {
                fallbackMessage
            }
        )
    }

    private companion object {

        const val PAGE_SIZE =
            20

        const val PREFETCH_DISTANCE =
            5
    }
}