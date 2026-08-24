package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Projeyi siler.
 *
 * Gerçek authorization backend tarafından yapılır.
 */
class DeleteProjectUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    suspend operator fun invoke(
        projectId: Int
    ): AppResult<Unit> {

        return projectsRepository
            .deleteProject(
                projectId = projectId
            )
    }
}