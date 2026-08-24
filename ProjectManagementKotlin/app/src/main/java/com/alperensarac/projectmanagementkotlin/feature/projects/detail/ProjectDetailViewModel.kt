package com.alperensarac.projectmanagementkotlin.feature.projects.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.GetCurrentUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.DeleteProjectUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectDetailUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectMembersUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.RemoveProjectMemberUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.UpdateProjectArchiveStatusUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.UpdateProjectMemberRoleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Proje detay ekranının state ve business işlemlerini yönetir.
 *
 * Fragment:
 * - UI render eder.
 * - Dialog gösterir.
 * - Navigation yapar.
 *
 * ViewModel:
 * - Yetki kontrolünü UI seviyesinde uygular.
 * - UseCase çağırır.
 * - State'i günceller.
 */
@HiltViewModel
class ProjectDetailViewModel @Inject constructor(

    private val getProjectDetailUseCase:
    GetProjectDetailUseCase,

    private val getProjectMembersUseCase:
    GetProjectMembersUseCase,

    private val getCurrentUserUseCase:
    GetCurrentUserUseCase,

    private val updateProjectMemberRoleUseCase:
    UpdateProjectMemberRoleUseCase,

    private val removeProjectMemberUseCase:
    RemoveProjectMemberUseCase,

    private val updateProjectArchiveStatusUseCase:
    UpdateProjectArchiveStatusUseCase,

    private val deleteProjectUseCase:
    DeleteProjectUseCase

) : ViewModel() {

    // =========================================================================
    // STATE
    // =========================================================================

    private val mutableUiState =
        MutableStateFlow(
            ProjectDetailUiState()
        )

    val uiState: StateFlow<ProjectDetailUiState> =
        mutableUiState.asStateFlow()

    // =========================================================================
    // EVENTS
    // =========================================================================

    private val eventChannel =
        Channel<ProjectDetailUiEvent>(
            capacity = Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // INITIAL LOAD
    // =========================================================================

    /**
     * Proje bilgisi, üye listesi ve mevcut kullanıcı bilgisi
     * birbirinden bağımsız olduğu için paralel yüklenir.
     */
    fun loadProject(
        projectId: Int,
        isRefresh: Boolean = false
    ) {

        if (
            projectId <= 0
        ) {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage =
                    "Geçersiz proje numarası."
                )

            return
        }

        val currentState =
            mutableUiState.value

        if (
            currentState.isLoading ||
            currentState.isRefreshing ||
            currentState.isProjectOperationInProgress
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                currentState.copy(

                    isLoading =
                    !isRefresh &&
                            currentState.project == null,

                    isRefreshing =
                    isRefresh,

                    errorMessage =
                    null
                )

            // -------------------------------------------------------------
            // Üç bağımsız request paralel çalıştırılır.
            // -------------------------------------------------------------

            val projectDeferred =
                async {

                    getProjectDetailUseCase(
                        projectId = projectId
                    )
                }

            val membersDeferred =
                async {

                    getProjectMembersUseCase(
                        projectId = projectId
                    )
                }

            val currentUserDeferred =
                async {

                    getCurrentUserUseCase()
                }

            reduceInitialResults(
                projectResult =
                projectDeferred.await(),

                membersResult =
                membersDeferred.await(),

                currentUserResult =
                currentUserDeferred.await()
            )
        }
    }

    fun refresh(
        projectId: Int
    ) {

        loadProject(
            projectId = projectId,
            isRefresh = true
        )
    }

    // =========================================================================
    // ARCHIVE
    // =========================================================================

    /**
     * Projeyi arşivler veya arşivden çıkarır.
     *
     * Backend:
     *
     * PATCH /api/Projects/{id}/archive
     */
    fun updateArchiveStatus(
        projectId: Int,
        isArchived: Boolean
    ) {

        val currentState =
            mutableUiState.value

        // -------------------------------------------------------------
        // Aynı anda birden fazla proje mutation işlemi yapılmasın.
        // -------------------------------------------------------------

        if (
            currentState.isProjectOperationInProgress
        ) {
            return
        }

        // -------------------------------------------------------------
        // UI seviyesinde permission.
        //
        // Gerçek authorization yine backend'dedir.
        // -------------------------------------------------------------

        if (
            !currentState.canManageProject
        ) {

            sendMessage(
                "Bu projeyi yönetme yetkiniz bulunmuyor."
            )

            return
        }

        val currentProject =
            currentState.project
                ?: return

        /*
         * Zaten aynı durumdaysa gereksiz PATCH göndermiyoruz.
         */
        if (
            currentProject.isArchived ==
            isArchived
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                currentState.copy(
                    isProjectOperationInProgress = true,
                    errorMessage = null
                )

            when (
                val result =
                    updateProjectArchiveStatusUseCase(
                        projectId = projectId,
                        isArchived = isArchived
                    )
            ) {

                // =============================================================
                // SUCCESS
                // =============================================================

                is AppResult.Success -> {

                    /*
                     * Endpoint güncellenmiş ProjectResponseDto dönüyor.
                     *
                     * Bu nedenle projeyi tekrar GET ile çekmeye gerek yok.
                     */
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isProjectOperationInProgress = false,
                            project = result.data,
                            errorMessage = null
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ProjectChanged(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: if (isArchived) {
                                    "Proje başarıyla arşivlendi."
                                } else {
                                    "Proje arşivden çıkarıldı."
                                }
                        )
                    )
                }

                // =============================================================
                // ERROR
                // =============================================================

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isProjectOperationInProgress = false
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ShowMessage(
                            message =
                            result.error
                                .toUserMessage()
                        )
                    )
                }
            }
        }
    }

    // =========================================================================
    // DELETE PROJECT
    // =========================================================================

    /**
     * Projeyi tamamen siler.
     *
     * Backend:
     *
     * DELETE /api/Projects/{id}
     */
    fun deleteProject(
        projectId: Int
    ) {

        val currentState =
            mutableUiState.value

        if (
            currentState.isProjectOperationInProgress
        ) {
            return
        }

        if (
            !currentState.canManageProject
        ) {

            sendMessage(
                "Bu projeyi silme yetkiniz bulunmuyor."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                currentState.copy(
                    isProjectOperationInProgress = true,
                    errorMessage = null
                )

            when (
                val result =
                    deleteProjectUseCase(
                        projectId = projectId
                    )
            ) {

                // =============================================================
                // SUCCESS
                // =============================================================

                is AppResult.Success -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isProjectOperationInProgress = false
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ProjectDeleted(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Proje başarıyla silindi."
                        )
                    )
                }

                // =============================================================
                // ERROR
                // =============================================================

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isProjectOperationInProgress = false
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ShowMessage(
                            message =
                            result.error
                                .toUserMessage()
                        )
                    )
                }
            }
        }
    }

    // =========================================================================
    // UPDATE MEMBER ROLE
    // =========================================================================

    /**
     * Proje üyesinin ProjectMemberRole değerini değiştirir.
     */
    fun updateMemberRole(
        projectId: Int,
        member: ProjectMember,
        newRole: ProjectMemberRole
    ) {

        val currentState =
            mutableUiState.value

        if (
            currentState.isMemberOperationInProgress
        ) {
            return
        }

        if (
            !currentState.canManageMembers
        ) {

            sendMessage(
                "Bu işlem için yetkiniz bulunmuyor."
            )

            return
        }
        if (
            currentState.project?.isArchived == true
        ) {

            sendMessage(
                "Arşivlenmiş projelerin üyelik rolleri değiştirilemez."
            )

            return
        }

        val existingRole =
            ProjectMemberRole.fromApiValue(
                member.projectRole
            )

        if (
            existingRole ==
            newRole
        ) {

            sendMessage(
                "Kullanıcı zaten bu proje rolüne sahip."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                currentState.copy(
                    isMemberOperationInProgress = true,
                    processingMemberUserId = member.userId
                )

            when (
                val result =
                    updateProjectMemberRoleUseCase(
                        projectId = projectId,
                        userId = member.userId,
                        role = newRole
                    )
            ) {

                is AppResult.Success -> {

                    val updatedMember =
                        result.data

                    val updatedMembers =
                        mutableUiState.value
                            .members
                            .map { currentMember ->

                                if (
                                    currentMember.userId ==
                                    updatedMember.userId
                                ) {

                                    updatedMember

                                } else {

                                    currentMember
                                }
                            }

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isMemberOperationInProgress = false,
                            processingMemberUserId = null,
                            members = updatedMembers
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ShowMessage(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Proje üyesinin rolü güncellendi."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isMemberOperationInProgress = false,
                            processingMemberUserId = null
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ShowMessage(
                            message =
                            result.error
                                .toUserMessage()
                        )
                    )
                }
            }
        }
    }

    // =========================================================================
    // REMOVE MEMBER
    // =========================================================================

    /**
     * Kullanıcıyı proje ekibinden çıkarır.
     */
    fun removeMember(
        projectId: Int,
        member: ProjectMember
    ) {

        val currentState =
            mutableUiState.value
        if (
            currentState.project?.isArchived == true
        ) {

            sendMessage(
                "Arşivlenmiş projelerin üyeleri değiştirilemez."
            )

            return
        }

        if (
            currentState.isMemberOperationInProgress
        ) {
            return
        }

        if (
            !currentState.canManageMembers
        ) {

            sendMessage(
                "Bu işlem için yetkiniz bulunmuyor."
            )

            return
        }

        /*
         * Backend yine son kontrolü yapar.
         *
         * Fakat proje sahibini çıkarmaya çalışmayı UI'da önceden
         * engellemek daha iyi kullanıcı deneyimidir.
         */
        if (
            member.isProjectOwner
        ) {

            sendMessage(
                "Proje sahibi ekipten çıkarılamaz."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                currentState.copy(
                    isMemberOperationInProgress = true,
                    processingMemberUserId = member.userId
                )

            when (
                val result =
                    removeProjectMemberUseCase(
                        projectId = projectId,
                        userId = member.userId
                    )
            ) {

                is AppResult.Success -> {

                    val updatedMembers =
                        mutableUiState.value
                            .members
                            .filterNot {
                                it.userId ==
                                        member.userId
                            }

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isMemberOperationInProgress = false,
                            processingMemberUserId = null,
                            members = updatedMembers
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ShowMessage(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Kullanıcı proje ekibinden çıkarıldı."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isMemberOperationInProgress = false,
                            processingMemberUserId = null
                        )

                    eventChannel.send(
                        ProjectDetailUiEvent.ShowMessage(
                            message =
                            result.error
                                .toUserMessage()
                        )
                    )
                }
            }
        }
    }

    // =========================================================================
    // INITIAL RESULT REDUCER
    // =========================================================================

    private fun reduceInitialResults(
        projectResult: AppResult<Project>,
        membersResult: AppResult<List<ProjectMember>>,
        currentUserResult: AppResult<AuthUser>
    ) {

        val previousState =
            mutableUiState.value

        val project =
            when (
                projectResult
            ) {

                is AppResult.Success ->
                    projectResult.data

                is AppResult.Error ->
                    previousState.project
            }

        val members =
            when (
                membersResult
            ) {

                is AppResult.Success ->
                    membersResult.data

                is AppResult.Error ->
                    previousState.members
            }

        val currentUser =
            when (
                currentUserResult
            ) {

                is AppResult.Success ->
                    currentUserResult.data

                is AppResult.Error ->
                    previousState.currentUser
            }

        val errorMessage =
            when {

                projectResult is AppResult.Error ->
                    projectResult.error
                        .toUserMessage()

                membersResult is AppResult.Error ->
                    membersResult.error
                        .toUserMessage()

                else ->
                    null
            }

        mutableUiState.value =
            previousState.copy(
                isLoading = false,
                isRefreshing = false,
                project = project,
                members = members,
                currentUser = currentUser,
                errorMessage = errorMessage
            )
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private fun sendMessage(
        message: String
    ) {

        viewModelScope.launch {

            eventChannel.send(
                ProjectDetailUiEvent.ShowMessage(
                    message = message
                )
            )
        }
    }
}