package com.alperensarac.projectmanagementkotlin.domain.usecase.projects

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectFilter
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Sayfalı proje listesini getirir.
 */
class GetProjectsUseCase @Inject constructor(
    private val projectsRepository: ProjectsRepository
) {

    operator fun invoke(
        filter: ProjectFilter
    ): Flow<PagingData<Project>> {
        return projectsRepository.getProjects(filter)
    }
}