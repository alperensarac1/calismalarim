package com.alperensarac.projectmanagementkotlin.feature.projects.detail.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.AddProjectMemberUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Projeye üye ekleme dialog'unu yönetir.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class AddProjectMemberViewModel @Inject constructor(
    private val searchUsersUseCase: SearchUsersUseCase,
    private val addProjectMemberUseCase: AddProjectMemberUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            AddProjectMemberUiState()
        )

    val uiState: StateFlow<AddProjectMemberUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<AddProjectMemberUiEvent>(
            capacity = Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    /**
     * Arama değeri değiştikçe yeni Paging akışı oluşur.
     *
     * 400 ms debounce ile her karakterde HTTP isteği atılmasını
     * engelliyoruz.
     */
    val users: Flow<PagingData<User>> =
        mutableUiState
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .distinctUntilChanged { old, new ->
                old.search == new.search
            }
            .flatMapLatest { state ->

                searchUsersUseCase(
                    search = state.search
                )
            }
            .cachedIn(
                viewModelScope
            )

    fun onSearchChanged(
        value: String
    ) {
        mutableUiState.update { state ->
            state.copy(
                search = value
            )
        }
    }

    fun selectUser(
        user: User
    ) {
        mutableUiState.update { state ->
            state.copy(
                selectedUser = user,
                errorMessage = null
            )
        }
    }

    fun selectRole(
        role: ProjectMemberRole
    ) {
        mutableUiState.update { state ->
            state.copy(
                selectedRole = role
            )
        }
    }

    /**
     * POST /api/projects/{projectId}/members
     */
    fun addMember(
        projectId: Int
    ) {

        val currentState =
            mutableUiState.value

        if (currentState.isSaving) {
            return
        }

        val selectedUser =
            currentState.selectedUser

        if (selectedUser == null) {

            sendMessage(
                "Lütfen projeye eklenecek kullanıcıyı seçin."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->
                state.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            when (
                val result =
                    addProjectMemberUseCase(
                        projectId = projectId,
                        userId = selectedUser.id,
                        role = currentState.selectedRole
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->
                        state.copy(
                            isSaving = false
                        )
                    }

                    eventChannel.send(
                        AddProjectMemberUiEvent.MemberAdded(
                            message =
                            result.message
                                ?: "Kullanıcı projeye başarıyla eklendi."
                        )
                    )
                }

                is AppResult.Error -> {

                    val message =
                        result.error.toUserMessage()

                    mutableUiState.update { state ->
                        state.copy(
                            isSaving = false,
                            errorMessage = message
                        )
                    }

                    eventChannel.send(
                        AddProjectMemberUiEvent.ShowMessage(
                            message = message
                        )
                    )
                }
            }
        }
    }

    private fun sendMessage(
        message: String
    ) {
        viewModelScope.launch {

            eventChannel.send(
                AddProjectMemberUiEvent.ShowMessage(
                    message = message
                )
            )
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS =
            400L
    }
}