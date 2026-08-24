package com.alperensarac.projectmanagementkotlin.feature.projects.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.GetCurrentUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectDetailUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.UpdateProjectUseCase
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
 * Proje düzenleme ekranının ViewModel'i.
 *
 * Sorumlulukları:
 *
 * - proje detayını yüklemek
 * - mevcut kullanıcıyı yüklemek
 * - permission hesaplaması için state sağlamak
 * - form state'ini saklamak
 * - validation yapmak
 * - PUT /api/Projects/{id} işlemini başlatmak
 */
@HiltViewModel
class EditProjectViewModel @Inject constructor(

    private val getProjectDetailUseCase:
    GetProjectDetailUseCase,

    private val getCurrentUserUseCase:
    GetCurrentUserUseCase,

    private val updateProjectUseCase:
    UpdateProjectUseCase

) : ViewModel() {

    // =========================================================================
    // STATE
    // =========================================================================

    private val mutableUiState =
        MutableStateFlow(
            EditProjectUiState()
        )

    val uiState: StateFlow<EditProjectUiState> =
        mutableUiState.asStateFlow()

    // =========================================================================
    // EVENT
    // =========================================================================

    private val eventChannel =
        Channel<EditProjectUiEvent>(
            capacity = Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // LOAD
    // =========================================================================

    fun loadProject(
        projectId: Int
    ) {

        if (
            projectId <= 0 ||
            mutableUiState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoading = true,
                    generalError = null
                )

            /*
             * Proje ve current user birbirinden bağımsız olduğu için
             * paralel getiriyoruz.
             */
            val projectDeferred =
                async {

                    getProjectDetailUseCase(
                        projectId = projectId
                    )
                }

            val currentUserDeferred =
                async {

                    getCurrentUserUseCase()
                }

            val projectResult =
                projectDeferred.await()

            val currentUserResult =
                currentUserDeferred.await()

            reduceLoadResults(
                projectResult = projectResult,
                currentUserResult = currentUserResult
            )
        }
    }

    private fun reduceLoadResults(
        projectResult: AppResult<Project>,
        currentUserResult: AppResult<AuthUser>
    ) {

        if (
            projectResult is AppResult.Error
        ) {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoading = false,

                    generalError =
                    projectResult.error
                        .toUserMessage()
                )

            return
        }

        if (
            currentUserResult is AppResult.Error
        ) {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoading = false,

                    generalError =
                    currentUserResult.error
                        .toUserMessage()
                )

            return
        }

        val project =
            (projectResult as AppResult.Success)
                .data

        val currentUser =
            (currentUserResult as AppResult.Success)
                .data

        val status =
            ProjectStatus.fromApiValue(
                project.status
            ) ?: ProjectStatus.PLANNING

        mutableUiState.value =
            mutableUiState.value.copy(

                isLoading = false,

                project = project,

                currentUser = currentUser,

                name =
                project.name,

                description =
                project.description
                    .orEmpty(),

                startDate =
                EditProjectUiState
                    .normalizeBackendDate(
                        project.startDateUtc
                    ),

                endDate =
                project.endDateUtc
                    ?.let {
                        EditProjectUiState
                            .normalizeBackendDate(
                                it
                            )
                    }
                    .orEmpty(),

                selectedStatus =
                status,

                ownerIdText =
                project.ownerId
                    .toString(),

                nameError = null,
                descriptionError = null,
                startDateError = null,
                endDateError = null,
                ownerIdError = null,
                statusError = null,

                generalError =
                null
            )
    }

    // =========================================================================
    // FORM EVENTS
    // =========================================================================

    fun onNameChanged(
        value: String
    ) {

        if (
            mutableUiState.value.name ==
            value
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                name = value,
                nameError = null
            )
    }

    fun onDescriptionChanged(
        value: String
    ) {

        if (
            mutableUiState.value.description ==
            value
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                description = value,
                descriptionError = null
            )
    }

    fun onStartDateChanged(
        value: String
    ) {

        mutableUiState.value =
            mutableUiState.value.copy(
                startDate = value,
                startDateError = null,
                endDateError = null
            )
    }

    fun onEndDateChanged(
        value: String
    ) {

        mutableUiState.value =
            mutableUiState.value.copy(
                endDate = value,
                endDateError = null
            )
    }

    fun onStatusChanged(
        status: ProjectStatus
    ) {

        mutableUiState.value =
            mutableUiState.value.copy(
                selectedStatus = status,
                statusError = null
            )
    }

    fun onOwnerIdChanged(
        value: String
    ) {

        if (
            mutableUiState.value.ownerIdText ==
            value
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                ownerIdText = value,
                ownerIdError = null
            )
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    fun updateProject(
        projectId: Int
    ) {

        val state =
            mutableUiState.value

        if (
            state.isBusy
        ) {
            return
        }

        if (
            !state.canEditProject
        ) {

            mutableUiState.value =
                state.copy(
                    generalError =
                    "Bu projeyi düzenleme yetkiniz bulunmuyor."
                )

            return
        }

        if (
            !state.isFormChanged
        ) {
            return
        }

        val name =
            state.name.trim()

        val description =
            state.description.trim()

        val startDate =
            state.startDate.trim()

        val endDate =
            state.endDate.trim()

        // ---------------------------------------------------------------------
        // LOCAL VALIDATION
        // ---------------------------------------------------------------------

        val nameError =
            when {

                name.isBlank() ->
                    "Proje adı zorunludur."

                name.length > 200 ->
                    "Proje adı en fazla 200 karakter olabilir."

                else ->
                    null
            }

        val descriptionError =
            if (
                description.length > 5000
            ) {

                "Proje açıklaması en fazla 5000 karakter olabilir."

            } else {

                null
            }

        val startDateError =
            if (
                startDate.isBlank()
            ) {

                "Proje başlangıç tarihi zorunludur."

            } else {

                null
            }

        /*
         * yyyy-MM-dd aynı uzunluk ve sıralanabilir format olduğu için
         * ilk 10 karakter üzerinde lexical karşılaştırma güvenlidir.
         */
        val endDateError =
            if (
                startDate.isNotBlank() &&
                endDate.isNotBlank() &&
                extractDate(endDate) <
                extractDate(startDate)
            ) {

                "Proje bitiş tarihi başlangıç tarihinden önce olamaz."

            } else {

                null
            }

        val ownerIdError =
            if (
                state.canChangeOwner
            ) {

                val ownerText =
                    state.ownerIdText.trim()

                when {

                    ownerText.isBlank() ->
                        null

                    ownerText.toIntOrNull() == null ->
                        "Proje sahibi ID değeri sayısal olmalıdır."

                    ownerText.toInt() <= 0 ->
                        "Proje sahibi ID değeri geçerli değildir."

                    else ->
                        null
                }

            } else {

                null
            }

        val hasError =
            nameError != null ||
                    descriptionError != null ||
                    startDateError != null ||
                    endDateError != null ||
                    ownerIdError != null

        if (
            hasError
        ) {

            mutableUiState.value =
                state.copy(
                    nameError = nameError,
                    descriptionError = descriptionError,
                    startDateError = startDateError,
                    endDateError = endDateError,
                    ownerIdError = ownerIdError,
                    generalError = null
                )

            return
        }

        // ---------------------------------------------------------------------
        // OWNER
        // ---------------------------------------------------------------------

        /*
         * ProjectManager owner değiştiremez.
         *
         * Bu yüzden ProjectManager request'inde OwnerId = null gönderiyoruz.
         *
         * Admin için girilmiş owner ID gönderilir.
         */
        val ownerId =
            if (
                state.canChangeOwner
            ) {

                state.ownerIdText
                    .trim()
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?.toInt()

            } else {

                null
            }

        // ---------------------------------------------------------------------
        // REQUEST
        // ---------------------------------------------------------------------

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isSaving = true,

                    nameError = null,
                    descriptionError = null,
                    startDateError = null,
                    endDateError = null,
                    ownerIdError = null,
                    statusError = null,

                    generalError = null
                )

            when (
                val result =
                    updateProjectUseCase(
                        projectId = projectId,

                        name = name,

                        description =
                        description
                            .takeIf {
                                it.isNotBlank()
                            },

                        startDate = startDate,

                        endDate =
                        endDate
                            .takeIf {
                                it.isNotBlank()
                            },

                        status =
                        state.selectedStatus,

                        ownerId =
                        ownerId
                    )
            ) {

                // =============================================================
                // SUCCESS
                // =============================================================

                is AppResult.Success -> {

                    val updatedProject =
                        result.data

                    val updatedStatus =
                        ProjectStatus.fromApiValue(
                            updatedProject.status
                        ) ?: state.selectedStatus

                    /*
                     * Backend'den dönen proje artık yeni original state.
                     *
                     * Bu nedenle isFormChanged otomatik false olur.
                     */
                    mutableUiState.value =
                        mutableUiState.value.copy(

                            isSaving = false,

                            project =
                            updatedProject,

                            name =
                            updatedProject.name,

                            description =
                            updatedProject.description
                                .orEmpty(),

                            startDate =
                            EditProjectUiState
                                .normalizeBackendDate(
                                    updatedProject.startDateUtc
                                ),

                            endDate =
                            updatedProject.endDateUtc
                                ?.let {
                                    EditProjectUiState
                                        .normalizeBackendDate(
                                            it
                                        )
                                }
                                .orEmpty(),

                            selectedStatus =
                            updatedStatus,

                            ownerIdText =
                            updatedProject.ownerId
                                .toString(),

                            generalError =
                            null
                        )

                    eventChannel.send(
                        EditProjectUiEvent.ProjectUpdated(
                            projectId =
                            updatedProject.id,

                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Proje başarıyla güncellendi."
                        )
                    )
                }

                // =============================================================
                // ERROR
                // =============================================================

                is AppResult.Error -> {

                    handleUpdateError(
                        error = result.error
                    )
                }
            }
        }
    }

    // =========================================================================
    // BACKEND VALIDATION
    // =========================================================================

    private fun handleUpdateError(
        error: NetworkError
    ) {

        if (
            error is NetworkError.Validation
        ) {

            val fields =
                error.fieldErrors

            val nameError =
                findFieldError(
                    fields,
                    "Name"
                )

            val descriptionError =
                findFieldError(
                    fields,
                    "Description"
                )

            val startDateError =
                findFieldError(
                    fields,
                    "StartDate"
                )

            val endDateError =
                findFieldError(
                    fields,
                    "EndDate"
                )

            val ownerIdError =
                findFieldError(
                    fields,
                    "OwnerId"
                )

            val statusError =
                findFieldError(
                    fields,
                    "Status"
                )

            val hasFieldError =
                nameError != null ||
                        descriptionError != null ||
                        startDateError != null ||
                        endDateError != null ||
                        ownerIdError != null ||
                        statusError != null

            mutableUiState.value =
                mutableUiState.value.copy(

                    isSaving = false,

                    nameError = nameError,
                    descriptionError = descriptionError,
                    startDateError = startDateError,
                    endDateError = endDateError,
                    ownerIdError = ownerIdError,
                    statusError = statusError,

                    generalError =
                    if (
                        hasFieldError
                    ) {
                        null
                    } else {
                        error.toUserMessage()
                    }
                )

            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                isSaving = false,

                generalError =
                error.toUserMessage()
            )
    }

    private fun findFieldError(
        fieldErrors: Map<String, List<String>>,
        fieldName: String
    ): String? {

        return fieldErrors
            .entries
            .firstOrNull {

                it.key.equals(
                    fieldName,
                    ignoreCase = true
                )
            }
            ?.value
            ?.firstOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private fun extractDate(
        value: String
    ): String {

        return if (
            value.length >= 10
        ) {
            value.take(10)
        } else {
            value
        }
    }
}