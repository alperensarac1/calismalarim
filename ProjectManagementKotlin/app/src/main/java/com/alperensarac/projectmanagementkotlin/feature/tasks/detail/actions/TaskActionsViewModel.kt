package com.alperensarac.projectmanagementkotlin.feature.tasks.detail.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectMembersUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.AssignTaskUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.DeleteTaskUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.UpdateTaskStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Görev detayındaki mutation işlemleri.
 *
 * Buraya özellikle:
 *
 * - Status update
 * - Assignment update
 * - Assignment için proje üyelerini getirme
 *
 * işlemlerini taşıyoruz.
 *
 * Böylece TaskDetailViewModel yorum, history, time-log gibi read/detail
 * state'lerini yönetmeye devam ediyor.
 */
@HiltViewModel
class TaskActionsViewModel @Inject constructor(
    private val updateTaskStatusUseCase: UpdateTaskStatusUseCase,
    private val assignTaskUseCase: AssignTaskUseCase,
    private val getProjectMembersUseCase: GetProjectMembersUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            TaskActionsUiState()
        )

    val uiState: StateFlow<TaskActionsUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<TaskActionsUiEvent>(
            capacity = Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // STATUS
    // =========================================================================

    fun updateStatus(
        taskId: Int,
        currentStatus: String,
        newStatus: TaskStatus
    ) {

        if (mutableUiState.value.isProcessing) {
            return
        }

        /*
         * Aynı status ise gereksiz HTTP çağrısı göndermiyoruz.
         */
        val existing =
            TaskStatus.fromApiValue(
                currentStatus
            )

        if (existing == newStatus) {

            sendMessage(
                "Görev zaten seçilen durumda."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isProcessing = true
                )
            }

            when (
                val result =
                    updateTaskStatusUseCase(
                        taskId = taskId,
                        status = newStatus
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update {
                        it.copy(
                            isProcessing = false
                        )
                    }

                    eventChannel.send(
                        TaskActionsUiEvent.StatusUpdated(
                            task = result.data,

                            message =
                            result.message
                                ?: "Görev durumu başarıyla değiştirildi."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            isProcessing = false
                        )
                    }

                    sendMessage(
                        result.error.toUserMessage()
                    )
                }
            }
        }
    }

    // =========================================================================
    // PROJECT MEMBERS
    // =========================================================================

    /**
     * Atama dialogu açılmadan önce proje üyelerini getirir.
     */
    fun loadProjectMembers(
        projectId: Int
    ) {

        if (projectId <= 0) {
            return
        }

        if (mutableUiState.value.isMembersLoading) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isMembersLoading = true,
                    membersErrorMessage = null
                )
            }

            when (
                val result =
                    getProjectMembersUseCase(
                        projectId = projectId
                    )
            ) {

                is AppResult.Success -> {

                    val assignableMembers =
                        result.data.filter { member ->

                            val role =
                                ProjectMemberRole.fromApiValue(
                                    member.projectRole
                                )

                            member.isActive &&
                                    role != ProjectMemberRole.VIEWER
                        }

                    mutableUiState.update {
                        it.copy(
                            isMembersLoading = false,
                            members = assignableMembers,
                            membersErrorMessage = null
                        )
                    }

                    eventChannel.send(
                        TaskActionsUiEvent.ProjectMembersLoaded(
                            members = assignableMembers
                        )
                    )
                }

                is AppResult.Error -> {

                    val message =
                        result.error.toUserMessage()

                    mutableUiState.update {
                        it.copy(
                            isMembersLoading = false,
                            membersErrorMessage = message
                        )
                    }

                    sendMessage(
                        message
                    )
                }
            }
        }
    }

    // =========================================================================
    // ASSIGN
    // =========================================================================

    fun assignTask(
        taskId: Int,
        currentAssignedUserId: Int?,
        newAssignedUserId: Int?
    ) {

        if (mutableUiState.value.isProcessing) {
            return
        }

        if (
            currentAssignedUserId ==
            newAssignedUserId
        ) {

            sendMessage(
                "Görev atamasında değişiklik yapılmadı."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isProcessing = true
                )
            }

            when (
                val result =
                    assignTaskUseCase(
                        taskId = taskId,

                        /*
                         * null ise backend görev atamasını kaldırır.
                         */
                        assignedToUserId =
                        newAssignedUserId
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update {
                        it.copy(
                            isProcessing = false
                        )
                    }

                    eventChannel.send(
                        TaskActionsUiEvent.AssignmentUpdated(
                            task = result.data,

                            message =
                            result.message
                                ?: "Görev ataması başarıyla değiştirildi."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            isProcessing = false
                        )
                    }

                    sendMessage(
                        result.error.toUserMessage()
                    )
                }
            }
        }
    }

    // =========================================================================
// DELETE TASK
// =========================================================================

    fun deleteTask(
        taskId: Int
    ) {

        val currentState =
            mutableUiState.value

        if (currentState.isAnyOperationRunning) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isDeleting = true
                )
            }

            when (
                val result =
                    deleteTaskUseCase(
                        taskId
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update {
                        it.copy(
                            isDeleting = false
                        )
                    }

                    eventChannel.send(
                        TaskActionsUiEvent.TaskDeleted(
                            message =
                            result.message
                                ?: "Görev başarıyla silindi."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            isDeleting = false
                        )
                    }

                    sendMessage(
                        result.error.toUserMessage()
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
                TaskActionsUiEvent.ShowMessage(
                    message = message
                )
            )
        }
    }
}