package com.alperensarac.projectmanagementkotlin.domain.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLogSummary

/**
 * Time Log domain repository sözleşmesidir.
 */
interface TaskTimeLogsRepository {

    suspend fun getTimeLogs(
        taskId: Int
    ): AppResult<List<TaskTimeLog>>

    suspend fun getSummary(
        taskId: Int
    ): AppResult<TaskTimeLogSummary>

    suspend fun createTimeLog(
        taskId: Int,
        hours: Double,
        description: String?,
        workDate: String
    ): AppResult<TaskTimeLog>

    suspend fun updateTimeLog(
        taskId: Int,
        timeLogId: Int,
        hours: Double,
        description: String?,
        workDate: String
    ): AppResult<TaskTimeLog>

    suspend fun deleteTimeLog(
        taskId: Int,
        timeLogId: Int
    ): AppResult<Unit>
}