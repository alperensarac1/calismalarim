package com.alperensarac.projectmanagementkotlin.domain.repository

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskFilter
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TasksRepository {

    fun getTasks(
        filter: TaskFilter
    ): Flow<PagingData<Task>>

    suspend fun getTaskById(
        taskId: Int
    ): AppResult<Task>

    suspend fun createTask(
        projectId: Int,
        title: String,
        description: String?,
        assignedToUserId: Int?,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: String?,
        estimatedHours: Double?
    ): AppResult<Task>

    suspend fun updateTask(
        taskId: Int,
        title: String,
        description: String?,
        assignedToUserId: Int?,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: String?,
        estimatedHours: Double?
    ): AppResult<Task>

    suspend fun updateTaskStatus(
        taskId: Int,
        status: TaskStatus
    ): AppResult<Task>

    suspend fun assignTask(
        taskId: Int,
        assignedToUserId: Int?
    ): AppResult<Task>

    suspend fun deleteTask(
        taskId: Int
    ): AppResult<Unit>
}