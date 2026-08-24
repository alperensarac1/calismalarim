package com.alperensarac.projectmanagementkotlin.domain.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.history.TaskHistory

/**
 * Görev geçmişi repository sözleşmesidir.
 */
interface TaskHistoriesRepository {

    suspend fun getHistories(
        taskId: Int
    ): AppResult<List<TaskHistory>>
}