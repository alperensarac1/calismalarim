package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Proje temel bilgilerini günceller.
 *
 * Status değişikliği de backend tasarımına uygun olarak
 * bu use-case üzerinden yapılır.
 */
class UpdateProjectUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    suspend operator fun invoke(
        projectId: Int,
        name: String,
        description: String?,
        startDate: String,
        endDate: String?,
        status: ProjectStatus,
        ownerId: Int?
    ): AppResult<Project> {

        return projectsRepository.updateProject(
            projectId = projectId,
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            status = status,
            ownerId = ownerId
        )
    }
}