package com.alperensarac.projectmanagementkotlin.feature.projects.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.GetCurrentUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.CreateProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Yeni proje oluşturma ekranını yönetir.
 */
@HiltViewModel
class CreateProjectViewModel @Inject constructor(

    private val createProjectUseCase:
    CreateProjectUseCase,

    private val getCurrentUserUseCase:
    GetCurrentUserUseCase

) : ViewModel() {

    // =========================================================================
    // STATE
    // =========================================================================

    private val mutableUiState =
        MutableStateFlow(
            CreateProjectUiState()
        )

    val uiState:
            StateFlow<CreateProjectUiState> =
        mutableUiState.asStateFlow()

    // =========================================================================
    // EVENTS
    // =========================================================================

    private val eventChannel =
        Channel<CreateProjectUiEvent>(
            capacity = Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    init {

        loadCurrentUser()
    }

    // =========================================================================
    // CURRENT USER
    // =========================================================================

    private fun loadCurrentUser() {

        if (
            mutableUiState.value.isLoadingPermission
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoadingPermission = true,
                    generalError = null
                )

            when (
                val result =
                    getCurrentUserUseCase()
            ) {

                is AppResult.Success -> {

                    val user =
                        result.data

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoadingPermission = false,
                            currentUser = user,

                            generalError =
                            if (
                                user.isAdmin ||
                                user.isProjectManager
                            ) {
                                null
                            } else {
                                "Proje oluşturma yetkiniz bulunmuyor."
                            }
                        )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoadingPermission = false,

                            generalError =
                            result.error
                                .toUserMessage()
                        )
                }
            }
        }
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    fun createProject(
        name: String,
        description: String,
        startDate: String,
        endDate: String,
        status: ProjectStatus,
        ownerIdText: String
    ) {

        val state =
            mutableUiState.value

        if (
            state.isSubmitting ||
            state.isLoadingPermission
        ) {
            return
        }

        /*
         * Fragment'taki visibility güvenlik kontrolü değildir.
         *
         * Bu nedenle ViewModel seviyesinde de permission kontrolü
         * yapıyoruz.
         */
        if (
            !state.canCreateProject
        ) {

            mutableUiState.value =
                state.copy(
                    generalError =
                    "Proje oluşturma yetkiniz bulunmuyor."
                )

            return
        }

        val normalizedName =
            name.trim()

        val normalizedDescription =
            description.trim()

        val normalizedStartDate =
            startDate.trim()

        val normalizedEndDate =
            endDate.trim()

        /*
         * ProjectManager için OwnerId alanını tamamen yok sayıyoruz.
         *
         * Backend de aynı davranışı gösteriyor:
         *
         * ProjectManager -> ownerId = currentUserId
         */
        val normalizedOwnerId =
            if (
                state.canSelectOwner
            ) {

                ownerIdText.trim()

            } else {

                ""
            }

        // ---------------------------------------------------------------------
        // VALIDATION
        // ---------------------------------------------------------------------

        val nameError =
            when {

                normalizedName.isBlank() ->
                    "Proje adı zorunludur."

                normalizedName.length > 200 ->
                    "Proje adı en fazla 200 karakter olabilir."

                else ->
                    null
            }

        val descriptionError =
            if (
                normalizedDescription.length > 5000
            ) {

                "Proje açıklaması en fazla 5000 karakter olabilir."

            } else {

                null
            }

        val startDateError =
            if (
                normalizedStartDate.isBlank()
            ) {

                "Proje başlangıç tarihi zorunludur."

            } else {

                null
            }

        /*
         * Her iki tarih de yyyy-MM-ddT00:00:00 formatında.
         *
         * Sabit ISO sıralamasında lexicographical karşılaştırma
         * burada kronolojik karşılaştırmayla aynıdır.
         */
        val endDateError =
            if (
                normalizedStartDate.isNotBlank() &&
                normalizedEndDate.isNotBlank() &&
                normalizedEndDate <
                normalizedStartDate
            ) {

                "Proje bitiş tarihi başlangıç tarihinden önce olamaz."

            } else {

                null
            }

        val ownerId =
            if (
                normalizedOwnerId.isBlank()
            ) {

                null

            } else {

                normalizedOwnerId
                    .toIntOrNull()
            }

        /*
         * Owner validation yalnız Admin için anlamlıdır.
         */
        val ownerIdError =
            if (
                state.canSelectOwner
            ) {

                when {

                    normalizedOwnerId.isBlank() ->
                        null

                    ownerId == null ->
                        "Proje sahibi ID değeri sayı olmalıdır."

                    ownerId <= 0 ->
                        "Proje sahibi ID değeri geçerli değildir."

                    else ->
                        null
                }

            } else {

                null
            }

        val hasValidationError =
            nameError != null ||
                    descriptionError != null ||
                    startDateError != null ||
                    endDateError != null ||
                    ownerIdError != null

        if (
            hasValidationError
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
        // REQUEST
        // ---------------------------------------------------------------------

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isSubmitting = true,

                    nameError = null,
                    descriptionError = null,
                    startDateError = null,
                    endDateError = null,
                    ownerIdError = null,

                    generalError = null
                )

            when (
                val result =
                    createProjectUseCase(
                        name =
                        normalizedName,

                        description =
                        normalizedDescription
                            .takeIf {
                                it.isNotBlank()
                            },

                        startDate =
                        normalizedStartDate,

                        endDate =
                        normalizedEndDate
                            .takeIf {
                                it.isNotBlank()
                            },

                        status =
                        status,

                        /*
                         * Admin -> seçilen ID veya null
                         *
                         * ProjectManager -> null
                         *
                         * Backend ProjectManager için currentUserId'yi
                         * zaten owner yapacak.
                         */
                        ownerId =
                        if (
                            state.canSelectOwner
                        ) {
                            ownerId
                        } else {
                            null
                        }
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isSubmitting = false
                        )

                    eventChannel.send(
                        CreateProjectUiEvent.ProjectCreated(
                            projectId =
                            result.data.id,

                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Proje başarıyla oluşturuldu."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isSubmitting = false,

                            generalError =
                            result.error
                                .toUserMessage()
                        )
                }
            }
        }
    }

    // =========================================================================
    // CLEAR ERROR
    // =========================================================================

    fun clearGeneralError() {

        if (
            mutableUiState.value.generalError != null
        ) {

            mutableUiState.value =
                mutableUiState.value.copy(
                    generalError = null
                )
        }
    }
}