package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Projeyi arşivler veya arşivden çıkarır.
 */
class UpdateProjectArchiveStatusUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    suspend operator fun invoke(
        projectId: Int,
        isArchived: Boolean
    ): AppResult<Project> {

        return projectsRepository
            .updateProjectArchiveStatus(
                projectId = projectId,
                isArchived = isArchived
            )
    }
}