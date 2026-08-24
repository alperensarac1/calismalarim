package com.alperensarac.projectmanagementkotlin.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.projects.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.ProjectsApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.AddProjectMemberRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.CreateProjectRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.UpdateProjectArchiveRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.UpdateProjectMemberRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.projects.UpdateProjectRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.paging.ProjectsPagingSource
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectFilter
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * ProjectsRepository'nin Retrofit/Paging implementasyonudur.
 */
@Singleton
class ProjectsRepositoryImpl @Inject constructor(
    private val projectsApi: ProjectsApi,
    private val networkErrorMapper: NetworkErrorMapper
) : ProjectsRepository {

    // =========================================================================
    // PROJECT LIST
    // =========================================================================

    override fun getProjects(
        filter: ProjectFilter
    ): Flow<PagingData<Project>> {

        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {

                ProjectsPagingSource(
                    projectsApi = projectsApi,
                    filter = filter
                )
            }
        ).flow
    }

    // =========================================================================
    // PROJECT DETAIL
    // =========================================================================

    override suspend fun getProjectById(
        projectId: Int
    ): AppResult<Project> {

        return try {

            val response =
                projectsApi.getProjectById(
                    projectId = projectId
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Proje bilgileri alınamadı."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // CREATE PROJECT
    // =========================================================================

    override suspend fun createProject(
        name: String,
        description: String?,
        startDate: String,
        endDate: String?,
        status: ProjectStatus,
        ownerId: Int?
    ): AppResult<Project> {

        return try {

            val response =
                projectsApi.createProject(
                    request =
                    CreateProjectRequestDto(
                        name = name.trim(),

                        description =
                        description
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            },

                        startDate =
                        startDate,

                        endDate =
                        endDate
                            ?.takeIf {
                                it.isNotBlank()
                            },

                        status =
                        status.apiValue,

                        ownerId =
                        ownerId
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Proje oluşturulamadı."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }
// =========================================================================
// UPDATE PROJECT
// =========================================================================

    override suspend fun updateProject(
        projectId: Int,
        name: String,
        description: String?,
        startDate: String,
        endDate: String?,
        status: ProjectStatus,
        ownerId: Int?
    ): AppResult<Project> {

        return try {

            val response =
                projectsApi.updateProject(
                    projectId = projectId,

                    request =
                    UpdateProjectRequestDto(
                        name = name.trim(),

                        description =
                        description
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            },

                        startDate =
                        startDate,

                        endDate =
                        endDate
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            },

                        status =
                        status.apiValue,

                        ownerId =
                        ownerId
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Proje güncellenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

// =========================================================================
// ARCHIVE PROJECT
// =========================================================================

    override suspend fun updateProjectArchiveStatus(
        projectId: Int,
        isArchived: Boolean
    ): AppResult<Project> {

        return try {

            val response =
                projectsApi.updateProjectArchiveStatus(
                    projectId = projectId,

                    request =
                    UpdateProjectArchiveRequestDto(
                        isArchived =
                        isArchived
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,

                        fallbackMessage =
                        if (
                            isArchived
                        ) {
                            "Proje arşivlenemedi."
                        } else {
                            "Proje arşivden çıkarılamadı."
                        }
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

// =========================================================================
// DELETE PROJECT
// =========================================================================

    override suspend fun deleteProject(
        projectId: Int
    ): AppResult<Unit> {

        return try {

            val response =
                projectsApi.deleteProject(
                    projectId = projectId
                )

            if (
                !response.success
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Proje silinemedi."
                    )
                )
            }

            AppResult.Success(
                data = Unit,
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }
    // =========================================================================
    // GET MEMBERS
    // =========================================================================

    override suspend fun getProjectMembers(
        projectId: Int,
        includeInactive: Boolean
    ): AppResult<List<ProjectMember>> {

        return try {

            val response =
                projectsApi.getProjectMembers(
                    projectId = projectId,
                    includeInactive = includeInactive
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Proje üyeleri alınamadı."
                    )
                )
            }

            AppResult.Success(
                data =
                data.map { dto ->
                    dto.toDomain()
                },

                message =
                response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // ADD MEMBER
    // =========================================================================

    override suspend fun addProjectMember(
        projectId: Int,
        userId: Int,
        role: String
    ): AppResult<ProjectMember> {

        return try {

            val response =
                projectsApi.addProjectMember(
                    projectId = projectId,

                    request =
                    AddProjectMemberRequestDto(
                        userId = userId,
                        role = role
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcı projeye eklenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // UPDATE MEMBER ROLE
    // =========================================================================

    override suspend fun updateProjectMemberRole(
        projectId: Int,
        userId: Int,
        role: String
    ): AppResult<ProjectMember> {

        return try {

            val response =
                projectsApi.updateProjectMember(
                    projectId = projectId,
                    userId = userId,

                    request =
                    UpdateProjectMemberRequestDto(
                        role = role
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Proje üyesinin rolü güncellenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // REMOVE MEMBER
    // =========================================================================

    override suspend fun removeProjectMember(
        projectId: Int,
        userId: Int
    ): AppResult<Unit> {

        return try {

            val response =
                projectsApi.removeProjectMember(
                    projectId = projectId,
                    userId = userId
                )

            if (
                !response.success
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcı projeden çıkarılamadı."
                    )
                )
            }

            AppResult.Success(
                data = Unit,
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // BUSINESS ERROR
    // =========================================================================

    private fun createBusinessError(
        message: String,
        errors: Map<String, List<String>>?,
        fallbackMessage: String
    ): NetworkError {

        if (
            !errors.isNullOrEmpty()
        ) {

            val validationMessage =
                errors.values
                    .flatten()
                    .joinToString("\n")
                    .ifBlank {

                        message.ifBlank {
                            fallbackMessage
                        }
                    }

            return NetworkError.Validation(
                message =
                validationMessage,

                fieldErrors =
                errors
            )
        }

        return NetworkError.Unknown(
            message =
            message.ifBlank {
                fallbackMessage
            }
        )
    }

    private companion object {

        const val PAGE_SIZE =
            20

        const val PREFETCH_DISTANCE =
            5
    }
}