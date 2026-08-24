package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Tek proje detayını getirir.
 */
class GetProjectDetailUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    suspend operator fun invoke(
        projectId: Int
    ): AppResult<Project> {
        require(projectId > 0) {
            "Project id sıfırdan büyük olmalıdır."
        }

        return projectsRepository.getProjectById(
            projectId = projectId
        )
    }
}