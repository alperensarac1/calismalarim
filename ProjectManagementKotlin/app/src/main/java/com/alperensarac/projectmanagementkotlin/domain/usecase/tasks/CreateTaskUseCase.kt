package com.alperensarac.projectmanagementkotlin.domain.usecase.tasks

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val tasksRepository: TasksRepository
) {

    suspend operator fun invoke(
        projectId: Int,
        title: String,
        description: String?,
        assignedToUserId: Int?,
        status: TaskStatus,
        priority: TaskPriority,
        dueDate: String?,
        estimatedHours: Double?
    ): AppResult<Task> {

        require(projectId > 0) {
            "Project id sıfırdan büyük olmalıdır."
        }

        val normalizedTitle =
            title.trim()

        require(normalizedTitle.isNotBlank()) {
            "Görev başlığı boş olamaz."
        }

        if (estimatedHours != null) {

            require(estimatedHours > 0.0) {
                "Tahmini süre sıfırdan büyük olmalıdır."
            }
        }

        return tasksRepository.createTask(
            projectId = projectId,
            title = normalizedTitle,

            description =
            description
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                },

            assignedToUserId =
            assignedToUserId,

            status =
            status,

            priority =
            priority,

            dueDate =
            dueDate,

            estimatedHours =
            estimatedHours
        )
    }
}