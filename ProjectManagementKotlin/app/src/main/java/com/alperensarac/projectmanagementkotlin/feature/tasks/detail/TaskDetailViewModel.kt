package com.alperensarac.projectmanagementkotlin.feature.tasks.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.GetCurrentUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.comments.CreateTaskCommentUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.comments.DeleteTaskCommentUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.comments.GetTaskCommentsUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.comments.UpdateTaskCommentUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.history.GetTaskHistoriesUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectDetailUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectMembersUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.GetTaskDetailUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs.CreateTaskTimeLogUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs.DeleteTaskTimeLogUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs.GetTaskTimeLogSummaryUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs.GetTaskTimeLogsUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.timelogs.UpdateTaskTimeLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Görev detayını ve görev yorumlarını yönetir.
 */
@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val getTaskDetailUseCase: GetTaskDetailUseCase,
    private val getTaskCommentsUseCase: GetTaskCommentsUseCase,
    private val createTaskCommentUseCase: CreateTaskCommentUseCase,
    private val updateTaskCommentUseCase: UpdateTaskCommentUseCase,
    private val deleteTaskCommentUseCase: DeleteTaskCommentUseCase,
    private val getTaskTimeLogsUseCase: GetTaskTimeLogsUseCase,
    private val getTaskTimeLogSummaryUseCase: GetTaskTimeLogSummaryUseCase,
    private val createTaskTimeLogUseCase: CreateTaskTimeLogUseCase,
    private val updateTaskTimeLogUseCase: UpdateTaskTimeLogUseCase,
    private val deleteTaskTimeLogUseCase: DeleteTaskTimeLogUseCase,
    private val getTaskHistoriesUseCase: GetTaskHistoriesUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getProjectDetailUseCase: GetProjectDetailUseCase,
    private val getProjectMembersUseCase: GetProjectMembersUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            TaskDetailUiState()
        )

    val uiState: StateFlow<TaskDetailUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<TaskDetailUiEvent>(
            Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // LOAD
    // =========================================================================

    fun loadTask(
        taskId: Int,
        isRefresh: Boolean = false
    ) {

        if (taskId <= 0) {

            mutableUiState.update { state ->
                state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage =
                    "Geçersiz görev numarası."
                )
            }

            return
        }

        val currentState =
            mutableUiState.value

        if (
            currentState.isLoading ||
            currentState.isRefreshing
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    isLoading =
                    !isRefresh &&
                            state.task == null,

                    isRefreshing =
                    isRefresh,

                    isCommentsLoading =
                    state.comments.isEmpty(),

                    errorMessage =
                    null,

                    commentsErrorMessage =
                    null
                )
            }

            val taskDeferred =
                async {
                    getTaskDetailUseCase(
                        taskId
                    )
                }

            val commentsDeferred =
                async {
                    getTaskCommentsUseCase(
                        taskId
                    )
                }

            reduceLoadResults(
                taskResult =
                taskDeferred.await(),

                commentsResult =
                commentsDeferred.await()
            )
        }
    }

    fun refresh(
        taskId: Int
    ) {

        loadTask(
            taskId = taskId,
            isRefresh = true
        )
        loadTimeLogs(taskId)
    }

    // =========================================================================
    // COMMENT INPUT
    // =========================================================================

    fun onCommentTextChanged(
        value: String
    ) {

        mutableUiState.update { state ->
            state.copy(
                commentText = value
            )
        }
    }

    // =========================================================================
    // CREATE COMMENT
    // =========================================================================

    fun sendComment(
        taskId: Int
    ) {

        val currentState =
            mutableUiState.value

        if (currentState.isCommentSending) {
            return
        }

        val content =
            currentState.commentText
                .trim()

        if (content.isBlank()) {

            sendMessage(
                "Yorum içeriği boş olamaz."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->
                state.copy(
                    isCommentSending = true
                )
            }

            when (
                val result =
                    createTaskCommentUseCase(
                        taskId = taskId,
                        content = content
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isCommentSending = false,

                            comments =
                            state.comments +
                                    result.data,

                            commentText = "",

                            /*
                             * Üst görev modelindeki commentCount değerini de
                             * local olarak senkron tutuyoruz.
                             */
                            task =
                            state.task?.copy(
                                commentCount =
                                state.task.commentCount + 1
                            )
                        )
                    }

                    sendMessage(
                        result.message
                            ?: "Yorum başarıyla eklendi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update { state ->
                        state.copy(
                            isCommentSending = false
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
    // UPDATE COMMENT
    // =========================================================================

    /**
     * Var olan bir yorumu günceller.
     */
    fun updateComment(
        taskId: Int,
        comment: TaskComment,
        newContent: String
    ) {

        if (!comment.canEdit) {

            sendMessage(
                "Bu yorumu düzenleme yetkiniz bulunmuyor."
            )

            return
        }

        val normalizedContent =
            newContent.trim()

        if (normalizedContent.isBlank()) {

            sendMessage(
                "Yorum içeriği boş olamaz."
            )

            return
        }

        if (
            normalizedContent ==
            comment.content.trim()
        ) {

            sendMessage(
                "Yorum içeriğinde değişiklik yapılmadı."
            )

            return
        }

        val currentState =
            mutableUiState.value

        if (
            currentState.updatingCommentId != null ||
            currentState.deletingCommentId != null
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    updatingCommentId =
                    comment.id
                )
            }

            when (
                val result =
                    updateTaskCommentUseCase(
                        taskId = taskId,
                        commentId = comment.id,
                        content = normalizedContent
                    )
            ) {

                is AppResult.Success -> {

                    val updatedComment =
                        result.data

                    mutableUiState.update { state ->

                        state.copy(
                            updatingCommentId =
                            null,

                            /*
                             * Endpoint güncellenmiş CommentResponseDto
                             * döndürdüğü için yalnızca ilgili satırı
                             * değiştiriyoruz.
                             */
                            comments =
                            state.comments.map { existingComment ->

                                if (
                                    existingComment.id ==
                                    updatedComment.id
                                ) {
                                    updatedComment
                                } else {
                                    existingComment
                                }
                            }
                        )
                    }

                    sendMessage(
                        result.message
                            ?: "Yorum başarıyla güncellendi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update { state ->
                        state.copy(
                            updatingCommentId =
                            null
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
    // DELETE COMMENT
    // =========================================================================

    fun deleteComment(
        taskId: Int,
        comment: TaskComment
    ) {

        if (!comment.canDelete) {

            sendMessage(
                "Bu yorumu silme yetkiniz bulunmuyor."
            )

            return
        }

        val currentState =
            mutableUiState.value

        if (
            currentState.deletingCommentId != null ||
            currentState.updatingCommentId != null
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->
                state.copy(
                    deletingCommentId =
                    comment.id
                )
            }

            when (
                val result =
                    deleteTaskCommentUseCase(
                        taskId = taskId,
                        commentId = comment.id
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        val updatedCount =
                            ((state.task?.commentCount ?: 0) - 1)
                                .coerceAtLeast(0)

                        state.copy(
                            deletingCommentId =
                            null,

                            comments =
                            state.comments.filterNot {
                                it.id ==
                                        comment.id
                            },

                            /*
                             * Üst görev alanındaki yorum sayısını da
                             * senkron tutuyoruz.
                             */
                            task =
                            state.task?.copy(
                                commentCount =
                                updatedCount
                            )
                        )
                    }

                    sendMessage(
                        result.message
                            ?: "Yorum başarıyla silindi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update { state ->
                        state.copy(
                            deletingCommentId =
                            null
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
    // LOAD RESULT
    // =========================================================================

    private fun reduceLoadResults(
        taskResult:
        AppResult<com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task>,

        commentsResult:
        AppResult<List<TaskComment>>
    ) {

        val previousState =
            mutableUiState.value

        val task =
            when (taskResult) {

                is AppResult.Success ->
                    taskResult.data

                is AppResult.Error ->
                    previousState.task
            }

        val comments =
            when (commentsResult) {

                is AppResult.Success ->
                    commentsResult.data

                is AppResult.Error ->
                    previousState.comments
            }

        mutableUiState.value =
            previousState.copy(
                isLoading = false,
                isRefreshing = false,
                isCommentsLoading = false,

                task = task,
                comments = comments,

                errorMessage =
                if (
                    taskResult is
                            AppResult.Error
                ) {
                    taskResult.error
                        .toUserMessage()
                } else {
                    null
                },

                commentsErrorMessage =
                if (
                    commentsResult is
                            AppResult.Error
                ) {
                    commentsResult.error
                        .toUserMessage()
                } else {
                    null
                }
            )
        if (
            taskResult is AppResult.Success
        ) {

            loadPermissionContext(
                projectId =
                taskResult.data.projectId
            )
        }
    }

    // =========================================================================
// TIME LOGS
// =========================================================================

    /**
     * Time Log listesi ile summary bilgisini birlikte getirir.
     */
    fun loadTimeLogs(
        taskId: Int
    ) {

        if (taskId <= 0) {
            return
        }

        if (mutableUiState.value.isTimeLogsLoading) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->
                state.copy(
                    isTimeLogsLoading = true,
                    timeLogsErrorMessage = null
                )
            }

            val listDeferred =
                async {
                    getTaskTimeLogsUseCase(taskId)
                }

            val summaryDeferred =
                async {
                    getTaskTimeLogSummaryUseCase(taskId)
                }

            val listResult =
                listDeferred.await()

            val summaryResult =
                summaryDeferred.await()

            val previous =
                mutableUiState.value

            mutableUiState.value =
                previous.copy(

                    isTimeLogsLoading =
                    false,

                    timeLogs =
                    when (listResult) {

                        is AppResult.Success ->
                            listResult.data

                        is AppResult.Error ->
                            previous.timeLogs
                    },

                    timeLogSummary =
                    when (summaryResult) {

                        is AppResult.Success ->
                            summaryResult.data

                        is AppResult.Error ->
                            previous.timeLogSummary
                    },

                    timeLogsErrorMessage =
                    when {

                        listResult is AppResult.Error ->
                            listResult.error
                                .toUserMessage()

                        summaryResult is AppResult.Error ->
                            summaryResult.error
                                .toUserMessage()

                        else ->
                            null
                    }
                )
        }
    }

    /**
     * Yeni zaman kaydı oluşturur.
     */
    fun createTimeLog(
        taskId: Int,
        hours: Double,
        description: String?,
        workDate: String
    ) {

        if (mutableUiState.value.isTimeLogCreating) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isTimeLogCreating = true
                )
            }

            when (
                val result =
                    createTaskTimeLogUseCase(
                        taskId = taskId,
                        hours = hours,
                        description = description,
                        workDate = workDate
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isTimeLogCreating = false,

                            timeLogs =
                            listOf(result.data) +
                                    state.timeLogs
                        )
                    }

                    /*
                     * Summary değerleri server tarafından hesaplandığı için
                     * burada manuel hesaplamak yerine summary endpointini
                     * tekrar çağırıyoruz.
                     */
                    refreshTimeLogSummary(taskId)

                    sendMessage(
                        result.message
                            ?: "Zaman kaydı başarıyla eklendi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            isTimeLogCreating = false
                        )
                    }

                    sendMessage(
                        result.error.toUserMessage()
                    )
                }
            }
        }
    }

    /**
     * Zaman kaydını günceller.
     */
    fun updateTimeLog(
        taskId: Int,
        timeLog: TaskTimeLog,
        hours: Double,
        description: String?,
        workDate: String
    ) {

        if (!timeLog.canEdit) {

            sendMessage(
                "Bu zaman kaydını düzenleme yetkiniz bulunmuyor."
            )

            return
        }

        if (mutableUiState.value.processingTimeLogId != null) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    processingTimeLogId =
                    timeLog.id
                )
            }

            when (
                val result =
                    updateTaskTimeLogUseCase(
                        taskId = taskId,
                        timeLogId = timeLog.id,
                        hours = hours,
                        description = description,
                        workDate = workDate
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            processingTimeLogId =
                            null,

                            timeLogs =
                            state.timeLogs.map { existing ->

                                if (
                                    existing.id ==
                                    result.data.id
                                ) {
                                    result.data
                                } else {
                                    existing
                                }
                            }
                        )
                    }

                    refreshTimeLogSummary(taskId)

                    sendMessage(
                        result.message
                            ?: "Zaman kaydı güncellendi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            processingTimeLogId =
                            null
                        )
                    }

                    sendMessage(
                        result.error.toUserMessage()
                    )
                }
            }
        }
    }

    /**
     * Zaman kaydını siler.
     */
    fun deleteTimeLog(
        taskId: Int,
        timeLog: TaskTimeLog
    ) {

        if (!timeLog.canDelete) {

            sendMessage(
                "Bu zaman kaydını silme yetkiniz bulunmuyor."
            )

            return
        }

        if (mutableUiState.value.processingTimeLogId != null) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    processingTimeLogId =
                    timeLog.id
                )
            }

            when (
                val result =
                    deleteTaskTimeLogUseCase(
                        taskId = taskId,
                        timeLogId = timeLog.id
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            processingTimeLogId =
                            null,

                            timeLogs =
                            state.timeLogs.filterNot {
                                it.id ==
                                        timeLog.id
                            }
                        )
                    }

                    refreshTimeLogSummary(taskId)

                    sendMessage(
                        result.message
                            ?: "Zaman kaydı silindi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            processingTimeLogId =
                            null
                        )
                    }

                    sendMessage(
                        result.error.toUserMessage()
                    )
                }
            }
        }
    }

    /**
     * Sadece summary endpointini tekrar çağırır.
     *
     * Create / update / delete sonrasında server'ın gerçek hesapladığı
     * ActualHours, DifferenceHours gibi değerleri alırız.
     */
    private fun refreshTimeLogSummary(
        taskId: Int
    ) {

        viewModelScope.launch {

            when (
                val result =
                    getTaskTimeLogSummaryUseCase(
                        taskId
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            timeLogSummary =
                            result.data,

                            /*
                             * TaskResponseDto.actualHours da summary ile aynı
                             * değeri temsil ettiği için ekrandaki task modelini
                             * local olarak senkron tutuyoruz.
                             */
                            task =
                            state.task?.copy(
                                actualHours =
                                result.data.actualHours
                            )
                        )
                    }
                }

                is AppResult.Error -> {
                    /*
                     * Mutation başarılı oldu.
                     *
                     * Summary refresh'in başarısız olması mutation'ı başarısız
                     * kabul ettirmemeli.
                     */
                }
            }
        }
    }

    // =========================================================================
// TASK HISTORY
// =========================================================================

    /**
     * Göreve ait audit/history kayıtlarını backend'den getirir.
     *
     * Endpoint:
     *
     * GET /api/tasks/{taskId}/histories
     */
    fun loadHistories(
        taskId: Int
    ) {

        if (taskId <= 0) {
            return
        }

        if (
            mutableUiState.value
                .isHistoriesLoading
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    isHistoriesLoading = true,
                    historiesErrorMessage = null
                )
            }

            when (
                val result =
                    getTaskHistoriesUseCase(
                        taskId = taskId
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isHistoriesLoading = false,
                            histories = result.data,
                            historiesErrorMessage = null
                        )
                    }
                }

                is AppResult.Error -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isHistoriesLoading = false,

                            /*
                             * Daha önce geçmiş geldiyse onu koruyoruz.
                             */
                            historiesErrorMessage =
                            result.error
                                .toUserMessage()
                        )
                    }
                }
            }
        }
    }
    // =========================================================================
// PERMISSION CONTEXT
// =========================================================================

    /**
     * Task action butonlarını backend kurallarına uygun gösterebilmek için
     * gerekli context'i yükler.
     *
     * İhtiyacımız olan bilgiler:
     *
     * 1. Oturum açmış kullanıcı
     * 2. Görevin projesi
     * 3. Kullanıcının proje üyeliği
     *
     * Bunlar task endpoint'inin response'unda bulunmadığı için
     * mevcut endpoint'lerden paralel olarak alınır.
     */
    private fun loadPermissionContext(
        projectId: Int
    ) {

        if (
            projectId <= 0
        ) {
            return
        }

        if (
            mutableUiState.value
                .isPermissionContextLoading
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    isPermissionContextLoading = true
                )
            }

            /*
             * Üç request birbirinden bağımsız olduğu için
             * sequential yerine paralel çalıştırıyoruz.
             */
            val currentUserDeferred =
                async {

                    getCurrentUserUseCase()
                }

            val projectDeferred =
                async {

                    getProjectDetailUseCase(
                        projectId
                    )
                }

            val membersDeferred =
                async {

                    getProjectMembersUseCase(
                        projectId
                    )
                }

            val currentUserResult =
                currentUserDeferred.await()

            val projectResult =
                projectDeferred.await()

            val membersResult =
                membersDeferred.await()

            /*
             * Permission context yüklenemezse güvenli varsayılanımız:
             *
             * task action butonlarını göstermemek.
             *
             * Backend authorization yine gerçek güvenlik katmanıdır.
             */
            val currentUser =
                when (
                    currentUserResult
                ) {

                    is AppResult.Success ->
                        currentUserResult.data

                    is AppResult.Error ->
                        null
                }

            val project =
                when (
                    projectResult
                ) {

                    is AppResult.Success ->
                        projectResult.data

                    is AppResult.Error ->
                        null
                }

            val members =
                when (
                    membersResult
                ) {

                    is AppResult.Success ->
                        membersResult.data

                    is AppResult.Error ->
                        emptyList()
                }

            /*
             * ProjectMember.userId backend User.id değeridir.
             *
             * Burada oturum açmış kullanıcının aktif üyeliğini buluyoruz.
             */
            val currentProjectMember =
                currentUser
                    ?.let { user ->

                        members.firstOrNull { member ->

                            member.userId ==
                                    user.id &&
                                    member.isActive
                        }
                    }

            mutableUiState.update { state ->

                state.copy(
                    isPermissionContextLoading = false,
                    currentUser = currentUser,
                    project = project,
                    currentProjectMember =
                    currentProjectMember
                )
            }
        }
    }
    /**
     * Mutation endpointinin döndürdüğü güncel Task modelini ekrana uygular.
     *
     * Böylece status/assignment sonrasında tekrar GET /api/Tasks/{id}
     * yapmak zorunda kalmayız.
     */
    fun applyUpdatedTask(
        task: com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
    ) {

        mutableUiState.update { state ->

            state.copy(
                task = task
            )
        }
    }
    private fun sendMessage(
        message: String
    ) {

        viewModelScope.launch {

            eventChannel.send(
                TaskDetailUiEvent.ShowMessage(
                    message = message
                )
            )
        }
    }
}