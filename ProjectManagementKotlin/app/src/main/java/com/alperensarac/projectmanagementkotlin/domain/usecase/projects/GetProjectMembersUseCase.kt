package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Bir projeye atanmış üyeleri getirir.
 */
class GetProjectMembersUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    suspend operator fun invoke(
        projectId: Int
    ): AppResult<List<ProjectMember>> {
        require(projectId > 0) {
            "Project id sıfırdan büyük olmalıdır."
        }

        return projectsRepository.getProjectMembers(
            projectId = projectId
        )
    }
}