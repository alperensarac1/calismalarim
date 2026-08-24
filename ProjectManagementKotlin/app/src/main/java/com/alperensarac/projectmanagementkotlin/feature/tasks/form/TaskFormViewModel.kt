package com.alperensarac.projectmanagementkotlin.feature.tasks.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectDetailUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectMembersUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.CreateTaskUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.GetTaskDetailUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.UpdateTaskUseCase
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
 * Görev oluşturma ve düzenleme formunun ViewModel'idir.
 *
 * Sorumlulukları:
 *
 * - proje detayını yüklemek,
 * - proje üyelerini yüklemek,
 * - edit modunda Task'ı taskId üzerinden geri yüklemek,
 * - atanabilecek kullanıcıları yönetmek,
 * - status / priority / assignee state'ini saklamak,
 * - create / update işlemlerini gerçekleştirmek.
 *
 * Process-death açısından önemli nokta:
 *
 * Fragment içindeki initialTask yalnızca memory referansıdır.
 * Bundle içerisindeki taskId ise Android tarafından yeniden oluşturulur.
 * Bu ViewModel taskId ile GET /api/Tasks/{id} çağrısı yaparak edit formunu
 * tekrar ayağa kaldırabilir.
 */
@HiltViewModel
class TaskFormViewModel @Inject constructor(

    private val createTaskUseCase:
    CreateTaskUseCase,

    private val updateTaskUseCase:
    UpdateTaskUseCase,

    private val getProjectMembersUseCase:
    GetProjectMembersUseCase,

    private val getProjectDetailUseCase:
    GetProjectDetailUseCase,

    private val getTaskDetailUseCase:
    GetTaskDetailUseCase

) : ViewModel() {

    // =========================================================================
    // STATE
    // =========================================================================

    private val mutableUiState =
        MutableStateFlow(
            TaskFormUiState()
        )

    val uiState:
            StateFlow<TaskFormUiState> =
        mutableUiState.asStateFlow()

    // =========================================================================
    // EVENTS
    // =========================================================================

    private val eventChannel =
        Channel<TaskFormUiEvent>(
            Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // EDIT TASK RESTORE
    // =========================================================================

    /**
     * Edit edilen görevi backend'den yeniden getirir.
     *
     * Normal açılışta caller zaten Task nesnesini verir ve initializeForEdit()
     * kullanılır. Process death sonrası bu nesne kaybolduğu için ARG_TASK_ID
     * üzerinden bu method çağrılır.
     */
    fun loadTaskForEdit(
        taskId: Int
    ) {

        if (
            taskId <= 0 ||
            mutableUiState.value.isTaskLoading
        ) {
            return
        }

        /*
         * Aynı Task zaten state'te bulunuyorsa gereksiz GET göndermiyoruz.
         */
        if (
            mutableUiState.value
                .editingTask
                ?.id == taskId
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    isTaskLoading = true,
                    errorMessage = null
                )
            }

            when (
                val result =
                    getTaskDetailUseCase(
                        taskId = taskId
                    )
            ) {

                is AppResult.Success -> {

                    applyTaskForEdit(
                        task = result.data
                    )
                }

                is AppResult.Error -> {

                    val message =
                        result.error
                            .toUserMessage()

                    mutableUiState.update { state ->

                        state.copy(
                            isTaskLoading = false,
                            editingTask = null,
                            errorMessage = message
                        )
                    }

                    eventChannel.send(
                        TaskFormUiEvent.ShowMessage(
                            message
                        )
                    )
                }
            }
        }
    }

    // =========================================================================
    // PROJECT
    // =========================================================================

    fun loadProject(
        projectId: Int
    ) {

        if (
            projectId <= 0 ||
            mutableUiState.value.isProjectLoading
        ) {
            return
        }

        if (
            !mutableUiState.value
                .projectStartDateUtc
                .isNullOrBlank()
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    isProjectLoading = true,
                    errorMessage = null
                )
            }

            when (
                val result =
                    getProjectDetailUseCase(
                        projectId = projectId
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isProjectLoading = false,
                            projectStartDateUtc =
                            result.data.startDateUtc,
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isProjectLoading = false,
                            projectStartDateUtc = null,
                            errorMessage =
                            result.error
                                .toUserMessage()
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // MEMBERS
    // =========================================================================

    fun loadMembers(
        projectId: Int
    ) {

        if (
            projectId <= 0 ||
            mutableUiState.value.isMembersLoading
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    isMembersLoading = true,
                    errorMessage = null
                )
            }

            when (
                val result =
                    getProjectMembersUseCase(
                        projectId
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isMembersLoading = false,
                            members =
                            result.data
                                .filter { member ->
                                    member.isActive
                                },
                            errorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isMembersLoading = false,
                            errorMessage =
                            result.error
                                .toUserMessage()
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // EDIT INITIALIZATION
    // =========================================================================

    /**
     * Caller'ın memory üzerinden verdiği Task ile edit state'ini hazırlar.
     */
    fun initializeForEdit(
        task: Task
    ) {

        applyTaskForEdit(
            task = task
        )
    }

    /**
     * Hem normal edit açılışı hem process-death restore aynı state dönüşümünü
     * kullansın diye tek noktada topladık.
     */
    private fun applyTaskForEdit(
        task: Task
    ) {

        mutableUiState.update { state ->

            state.copy(
                isTaskLoading = false,
                editingTask = task,
                selectedAssignedUserId =
                task.assignedToUserId,
                selectedStatus =
                TaskStatus.fromApiValue(
                    task.status
                ) ?: TaskStatus.TODO,
                selectedPriority =
                TaskPriority.fromApiValue(
                    task.priority
                ) ?: TaskPriority.MEDIUM,
                errorMessage = null
            )
        }
    }

    // =========================================================================
    // FORM SELECTIONS
    // =========================================================================

    fun selectAssignedUser(
        userId: Int?
    ) {

        mutableUiState.update { state ->

            state.copy(
                selectedAssignedUserId = userId,
                errorMessage = null
            )
        }
    }

    fun selectStatus(
        status: TaskStatus
    ) {

        mutableUiState.update { state ->

            state.copy(
                selectedStatus = status,
                errorMessage = null
            )
        }
    }

    fun selectPriority(
        priority: TaskPriority
    ) {

        mutableUiState.update { state ->

            state.copy(
                selectedPriority = priority,
                errorMessage = null
            )
        }
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    fun createTask(
        projectId: Int,
        title: String,
        description: String?,
        dueDate: String?,
        estimatedHours: Double?
    ) {

        val state =
            mutableUiState.value

        if (
            state.isBusy
        ) {
            return
        }

        if (
            !validateSelectedAssignee(
                state = state
            )
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { current ->

                current.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            when (
                val result =
                    createTaskUseCase(
                        projectId = projectId,
                        title = title,
                        description = description,
                        assignedToUserId =
                        state.selectedAssignedUserId,
                        status =
                        state.selectedStatus,
                        priority =
                        state.selectedPriority,
                        dueDate = dueDate,
                        estimatedHours = estimatedHours
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { current ->

                        current.copy(
                            isSaving = false,
                            errorMessage = null
                        )
                    }

                    eventChannel.send(
                        TaskFormUiEvent.TaskSaved(
                            task = result.data,
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Görev başarıyla oluşturuldu."
                        )
                    )
                }

                is AppResult.Error -> {

                    val message =
                        result.error
                            .toUserMessage()

                    mutableUiState.update { current ->

                        current.copy(
                            isSaving = false,
                            errorMessage = message
                        )
                    }

                    eventChannel.send(
                        TaskFormUiEvent.ShowMessage(
                            message
                        )
                    )
                }
            }
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    fun updateTask(
        taskId: Int,
        title: String,
        description: String?,
        dueDate: String?,
        estimatedHours: Double?
    ) {

        val state =
            mutableUiState.value

        if (
            state.isBusy
        ) {
            return
        }

        if (
            !validateSelectedAssignee(
                state = state
            )
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { current ->

                current.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            when (
                val result =
                    updateTaskUseCase(
                        taskId = taskId,
                        title = title,
                        description = description,
                        assignedToUserId =
                        state.selectedAssignedUserId,
                        status =
                        state.selectedStatus,
                        priority =
                        state.selectedPriority,
                        dueDate = dueDate,
                        estimatedHours = estimatedHours
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { current ->

                        current.copy(
                            isSaving = false,
                            editingTask = result.data,
                            errorMessage = null
                        )
                    }

                    eventChannel.send(
                        TaskFormUiEvent.TaskSaved(
                            task = result.data,
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Görev başarıyla güncellendi."
                        )
                    )
                }

                is AppResult.Error -> {

                    val message =
                        result.error
                            .toUserMessage()

                    mutableUiState.update { current ->

                        current.copy(
                            isSaving = false,
                            errorMessage = message
                        )
                    }

                    eventChannel.send(
                        TaskFormUiEvent.ShowMessage(
                            message
                        )
                    )
                }
            }
        }
    }

    // =========================================================================
    // VALIDATION
    // =========================================================================

    private fun validateSelectedAssignee(
        state: TaskFormUiState
    ): Boolean {

        val selectedUserId =
            state.selectedAssignedUserId
                ?: return true

        val isValid =
            state.assignableMembers
                .any { member ->
                    member.userId ==
                            selectedUserId
                }

        if (
            isValid
        ) {
            return true
        }

        sendMessage(
            "Seçili kullanıcı artık bu göreve atanamaz. " +
                    "Lütfen başka bir proje üyesi seçin veya görevi atamasız bırakın."
        )

        return false
    }

    // =========================================================================
    // MESSAGE
    // =========================================================================

    private fun sendMessage(
        message: String
    ) {

        viewModelScope.launch {

            eventChannel.send(
                TaskFormUiEvent.ShowMessage(
                    message
                )
            )
        }
    }
}
