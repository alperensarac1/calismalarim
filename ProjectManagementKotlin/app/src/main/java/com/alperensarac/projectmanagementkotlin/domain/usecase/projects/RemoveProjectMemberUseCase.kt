package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Kullanıcıyı proje ekibinden çıkarır.
 */
class RemoveProjectMemberUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    suspend operator fun invoke(
        projectId: Int,
        userId: Int
    ): AppResult<Unit> {

        require(projectId > 0) {
            "Project id sıfırdan büyük olmalıdır."
        }

        require(userId > 0) {
            "User id sıfırdan büyük olmalıdır."
        }

        return projectsRepository.removeProjectMember(
            projectId = projectId,
            userId = userId
        )
    }
}