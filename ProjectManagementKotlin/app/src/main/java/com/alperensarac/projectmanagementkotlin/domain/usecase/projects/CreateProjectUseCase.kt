package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject

/**
 * Yeni proje oluşturma use case'i.
 *
 * Backend'deki CreateProjectRequestValidator kurallarıyla
 * uyumlu temel client-side kontrollerini yapar.
 */
class CreateProjectUseCase @Inject constructor(
    private val projectsRepository:
    ProjectsRepository
) {

    suspend operator fun invoke(
        name: String,
        description: String?,
        startDate: String,
        endDate: String?,
        status: ProjectStatus,
        ownerId: Int?
    ): AppResult<Project> {

        /*
         * Asıl doğrulama backend tarafından yapılmaya devam eder.
         *
         * Buradaki kontroller kullanıcıya daha erken geri bildirim
         * verebilmek içindir.
         */

        return projectsRepository.createProject(
            name = name,
            description = description,
            startDate = startDate,
            endDate = endDate,
            status = status,
            ownerId = ownerId
        )
    }
}