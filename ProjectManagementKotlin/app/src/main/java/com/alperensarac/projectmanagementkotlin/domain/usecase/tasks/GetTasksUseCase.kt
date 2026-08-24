package com.alperensarac.projectmanagementkotlin.domain.usecase.tasks

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskFilter
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Sayfalı görev listesini getirir.
 */
class GetTasksUseCase @Inject constructor(
    private val tasksRepository: TasksRepository
) {

    operator fun invoke(
        filter: TaskFilter
    ): Flow<PagingData<Task>> {

        return tasksRepository.getTasks(
            filter = filter
        )
    }
}