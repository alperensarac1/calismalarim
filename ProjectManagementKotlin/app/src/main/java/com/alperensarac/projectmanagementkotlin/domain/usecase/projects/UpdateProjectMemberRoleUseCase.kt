package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Proje üyesinin proje içerisindeki rolünü günceller.
 */
class UpdateProjectMemberRoleUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    suspend operator fun invoke(
        projectId: Int,
        userId: Int,
        role: ProjectMemberRole
    ): AppResult<ProjectMember> {

        require(projectId > 0) {
            "Project id sıfırdan büyük olmalıdır."
        }

        require(userId > 0) {
            "User id sıfırdan büyük olmalıdır."
        }

        /*
         * Repository network katmanıyla konuştuğu için API'nin beklediği
         * String değer burada hazırlanır.
         *
         * Örneğin:
         *
         * ProjectMemberRole.VIEWER
         *
         * ->
         *
         * "Viewer"
         */
        return projectsRepository.updateProjectMemberRole(
            projectId = projectId,
            userId = userId,
            role = role.apiValue
        )
    }
}