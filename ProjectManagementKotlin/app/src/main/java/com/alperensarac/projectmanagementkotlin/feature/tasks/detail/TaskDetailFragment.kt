package com.alperensarac.projectmanagementkotlin.feature.tasks.detail

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.ApiDateFormatter
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.DialogEditCommentBinding
import com.alperensarac.projectmanagementkotlin.databinding.DialogTaskTimeLogBinding
import com.alperensarac.projectmanagementkotlin.databinding.FragmentTaskDetailBinding
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.feature.tasks.detail.actions.TaskActionsUiEvent
import com.alperensarac.projectmanagementkotlin.feature.tasks.detail.actions.TaskActionsViewModel
import com.alperensarac.projectmanagementkotlin.feature.tasks.detail.comments.TaskCommentAdapter
import com.alperensarac.projectmanagementkotlin.feature.tasks.detail.history.TaskHistoryAdapter
import com.alperensarac.projectmanagementkotlin.feature.tasks.detail.timelogs.TaskTimeLogAdapter
import com.alperensarac.projectmanagementkotlin.feature.tasks.form.TaskFormDialogFragment
import com.alperensarac.projectmanagementkotlin.feature.tasks.navigation.TaskNavigationResult
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Görev detay ekranı.
 *
 * Desteklenen işlemler:
 *
 * - Görev detayını görüntüleme
 * - Yorumları görüntüleme
 * - Yeni yorum ekleme
 * - Yorum düzenleme
 * - Yorum silme
 */
@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private var _binding:
            FragmentTaskDetailBinding? =
        null

    private val binding:
            FragmentTaskDetailBinding
        get() =
            checkNotNull(_binding)

    private val viewModel:
            TaskDetailViewModel
            by viewModels()

    @Inject
    lateinit var dateTimeFormatter:
            DateTimeFormatter

    private lateinit var commentAdapter:
            TaskCommentAdapter

    private lateinit var historyAdapter:
            TaskHistoryAdapter
    private val taskActionsViewModel:
            TaskActionsViewModel
            by viewModels()
    /**
     * Adapter callback'lerinin operasyon state'ine ulaşması için son state.
     */
    private var latestUiState =
        TaskDetailUiState()

    private val taskId: Int
        get() =
            requireArguments().getInt(
                ARG_TASK_ID,
                INVALID_TASK_ID
            )
    @Inject
    lateinit var apiDateFormatter: ApiDateFormatter

    private lateinit var timeLogAdapter: TaskTimeLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentTaskDetailBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        configureCommentList()
        configureListeners()
        configureCommentInput()
        configureHistoryList()
        observeUiState()
        observeUiEvents()
        configureTimeLogList()
        observeTaskActions()
        configureTaskFormResult()
        if (savedInstanceState == null) {

            /*
             * Task + Comments
             */
            viewModel.loadTask(
                taskId
            )

            /*
             * Time Logs + Summary
             */
            viewModel.loadTimeLogs(
                taskId
            )

            /*
             * Task History
             */
            viewModel.loadHistories(
                taskId
            )
        }
    }

    // =========================================================================
    // COMMENT LIST
    // =========================================================================

    private fun configureCommentList() {

        commentAdapter =
            TaskCommentAdapter(

                dateTimeFormatter =
                dateTimeFormatter,

                updatingCommentId = {
                    latestUiState.updatingCommentId
                },

                deletingCommentId = {
                    latestUiState.deletingCommentId
                },

                onEditClicked = { comment ->

                    showEditCommentDialog(
                        comment
                    )
                },

                onDeleteClicked = { comment ->

                    showDeleteCommentDialog(
                        comment
                    )
                }
            )

        binding.recyclerViewTaskComments.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                commentAdapter

            isNestedScrollingEnabled =
                false
        }
    }

    // =========================================================================
    // COMMENT INPUT
    // =========================================================================

    private fun configureCommentInput() {

        binding.editTextTaskComment
            .doAfterTextChanged { editable ->

                viewModel.onCommentTextChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // LISTENERS
    // =========================================================================

    private fun configureListeners() {

        // =========================================================================
        // BACK
        // =========================================================================

        binding.buttonTaskDetailBack
            .setOnClickListener {

                findNavController()
                    .navigateUp()
            }

        // =========================================================================
        // REFRESH
        // =========================================================================

        binding.swipeRefreshTaskDetail
            .setOnRefreshListener {

                viewModel.refresh(
                    taskId
                )

                viewModel.loadTimeLogs(
                    taskId
                )

                viewModel.loadHistories(
                    taskId
                )
            }

        // =========================================================================
        // RETRY
        // =========================================================================

        binding.buttonRetryTaskDetail
            .setOnClickListener {

                viewModel.loadTask(
                    taskId
                )
            }

        // =========================================================================
        // COMMENT
        // =========================================================================

        binding.buttonSendTaskComment
            .setOnClickListener {

                /*
                 * Buton disabled olsa bile permission'ı ayrıca kontrol ediyoruz.
                 */
                if (
                    !latestUiState.canCreateComment
                ) {

                    showWriteOperationNotAllowedMessage()

                    return@setOnClickListener
                }

                if (
                    !latestUiState.canSendComment
                ) {
                    return@setOnClickListener
                }

                viewModel.sendComment(
                    taskId
                )
            }

        // =========================================================================
        // CHANGE STATUS
        // =========================================================================

        binding.buttonChangeTaskStatus
            .setOnClickListener {

                /*
                 * Buton görünürlüğüne tek başına güvenmiyoruz.
                 *
                 * UI yanlışlıkla görünür hale gelse bile ikinci bir
                 * permission guard burada bulunur.
                 */
                if (
                    !latestUiState.canChangeTaskStatus
                ) {

                    showTaskActionNotAllowedMessage()

                    return@setOnClickListener
                }

                val task =
                    latestUiState.task
                        ?: return@setOnClickListener

                showTaskStatusDialog(
                    task = task
                )
            }

        // =========================================================================
        // CHANGE ASSIGNMENT
        // =========================================================================

        binding.buttonChangeTaskAssignment
            .setOnClickListener {

                if (
                    !latestUiState.canChangeTaskAssignment
                ) {

                    showTaskActionNotAllowedMessage()

                    return@setOnClickListener
                }

                val task =
                    latestUiState.task
                        ?: return@setOnClickListener

                /*
                 * Assignment dialog açılmadan önce projenin
                 * atanabilir üyelerini getiriyoruz.
                 */
                taskActionsViewModel
                    .loadProjectMembers(
                        projectId =
                        task.projectId
                    )
            }

        // =========================================================================
        // EDIT TASK
        // =========================================================================

        binding.buttonEditTask
            .setOnClickListener {

                if (
                    !latestUiState.canEditTask
                ) {

                    showTaskActionNotAllowedMessage()

                    return@setOnClickListener
                }

                val task =
                    latestUiState.task
                        ?: return@setOnClickListener

                /*
                 * Kullanıcı hızlıca iki kez tıklarsa iki dialog
                 * açılmasını engelliyoruz.
                 */
                val existingDialog =
                    childFragmentManager
                        .findFragmentByTag(
                            EDIT_TASK_DIALOG_TAG
                        )

                if (
                    existingDialog != null
                ) {
                    return@setOnClickListener
                }

                TaskFormDialogFragment
                    .newEditInstance(
                        task
                    )
                    .show(
                        childFragmentManager,
                        EDIT_TASK_DIALOG_TAG
                    )
            }

        // =========================================================================
        // DELETE TASK
        // =========================================================================

        binding.buttonDeleteTask
            .setOnClickListener {

                if (
                    !latestUiState.canDeleteTask
                ) {

                    showTaskActionNotAllowedMessage()

                    return@setOnClickListener
                }

                showDeleteTaskDialog()
            }
    }

    // =========================================================================
    // EDIT COMMENT
    // =========================================================================

    private fun showEditCommentDialog(
        comment: TaskComment
    ) {

        if (!comment.canEdit) {

            Snackbar.make(
                binding.root,
                R.string.comment_edit_not_allowed,
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }

        val dialogBinding =
            DialogEditCommentBinding.inflate(
                layoutInflater
            )

        /*
         * Mevcut yorum dialog'a otomatik gelir.
         */
        dialogBinding.editTextCommentContent
            .setText(
                comment.content
            )

        dialogBinding.editTextCommentContent
            .setSelection(
                comment.content.length
            )

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(
                    dialogBinding.root
                )
                .setNegativeButton(
                    R.string.action_cancel,
                    null
                )
                .setPositiveButton(
                    R.string.action_save,
                    null
                )
                .create()

        /*
         * Pozitif butonun otomatik dismiss davranışını yönetebilmek için
         * listener'ı dialog açıldıktan sonra bağlıyoruz.
         */
        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            )
                .setOnClickListener {

                    val newContent =
                        dialogBinding
                            .editTextCommentContent
                            .text
                            ?.toString()
                            .orEmpty()
                            .trim()

                    if (newContent.isBlank()) {

                        dialogBinding
                            .textInputLayoutEditComment
                            .error =
                            getString(
                                R.string.comment_content_required
                            )

                        return@setOnClickListener
                    }

                    if (
                        newContent ==
                        comment.content.trim()
                    ) {

                        dialogBinding
                            .textInputLayoutEditComment
                            .error =
                            getString(
                                R.string.comment_content_not_changed
                            )

                        return@setOnClickListener
                    }

                    dialogBinding
                        .textInputLayoutEditComment
                        .error =
                        null

                    viewModel.updateComment(
                        taskId = taskId,
                        comment = comment,
                        newContent = newContent
                    )

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    // =========================================================================
    // DELETE COMMENT
    // =========================================================================

    private fun showDeleteCommentDialog(
        comment: TaskComment
    ) {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                R.string.comment_delete_dialog_title
            )
            .setMessage(
                R.string.comment_delete_dialog_message
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.comment_delete
            ) { _, _ ->

                viewModel.deleteComment(
                    taskId = taskId,
                    comment = comment
                )
            }
            .show()
    }

    // =========================================================================
    // OBSERVERS
    // =========================================================================

    private fun observeUiState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.uiState.collect { state ->

                    latestUiState =
                        state

                    renderUiState(
                        state
                    )
                }
            }
        }

    }

    private fun observeUiEvents() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.events.collect { event ->

                    when (event) {

                        is TaskDetailUiEvent.ShowMessage -> {

                            Snackbar.make(
                                binding.root,
                                event.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // RENDER
    // =========================================================================

    private fun renderUiState(
        state: TaskDetailUiState
    ) {

        binding.cardTimeLogSummary.isVisible =
            state.timeLogSummary != null
        binding.progressIndicatorTaskDetail.isVisible =
            state.isLoading &&
                    !state.hasContent

        binding.swipeRefreshTaskDetail.isRefreshing =
            state.isRefreshing

        binding.layoutTaskDetailContent.isVisible =
            state.hasContent

        binding.layoutTaskDetailError.isVisible =
            !state.errorMessage.isNullOrBlank() &&
                    !state.hasContent

        binding.textViewTaskDetailError.text =
            state.errorMessage.orEmpty()

        state.task?.let(
            ::renderTask
        )
        // ---------------------------------------------------------------------
        // TASK ACTION PERMISSIONS
        // ---------------------------------------------------------------------

        renderTaskActionPermissions(
            state
        )



        // ---------------------------------------------------------------------
        // COMMENTS
        // ---------------------------------------------------------------------

        binding.progressIndicatorComments.isVisible =
            state.isCommentsLoading

        commentAdapter.submitList(
            state.comments
        )

        /*
         * update/delete id'leri comment modelinin parçası olmadığı için
         * operasyon görünümünü ayrıca refresh ediyoruz.
         */
        commentAdapter.refreshOperationState()

        binding.textViewCommentsEmpty.isVisible =
            state.hasContent &&
                    state.areCommentsEmpty

        binding.textViewCommentsError.isVisible =
            !state.commentsErrorMessage
                .isNullOrBlank()

        binding.textViewCommentsError.text =
            state.commentsErrorMessage
                .orEmpty()

        binding.textViewCommentsTitle.text =
            getString(
                R.string.comments_title_count,
                state.comments.size
            )

        // ---------------------------------------------------------------------
        // NEW COMMENT
        // ---------------------------------------------------------------------

        // ---------------------------------------------------------------------
// NEW COMMENT
// ---------------------------------------------------------------------

        /*
         * Viewer veya arşivlenmiş proje gibi yorum yazma yetkisi
         * olmayan durumlarda input'u tamamen kapatıyoruz.
         */
        binding.editTextTaskComment
            .isEnabled =
            state.canCreateComment

        binding.buttonSendTaskComment
            .isEnabled =
            state.canSendComment

        binding.progressIndicatorSendComment
            .isVisible =
            state.isCommentSending

        /*
         * Kullanıcı permission sahibi değilse input görünür kalabilir
         * fakat disabled olur.
         *
         * Böylece "yorum özelliği yok" izlenimi yerine bu görevde
         * yazma yetkisi olmadığı anlaşılır.
         */
        binding.editTextTaskComment
            .alpha =
            if (
                state.canCreateComment
            ) {
                1.0f
            } else {
                DISABLED_VIEW_ALPHA
            }

        binding.buttonSendTaskComment
            .alpha =
            if (
                state.canCreateComment
            ) {
                1.0f
            } else {
                DISABLED_VIEW_ALPHA
            }

        if (
            binding.editTextTaskComment
                .text
                ?.toString() !=
            state.commentText
        ) {

            binding.editTextTaskComment
                .setText(
                    state.commentText
                )

            binding.editTextTaskComment
                .setSelection(
                    state.commentText.length
                )
        }
        if (
            binding.editTextTaskComment
                .text
                ?.toString() !=
            state.commentText
        ) {

            binding.editTextTaskComment
                .setText(
                    state.commentText
                )

            binding.editTextTaskComment
                .setSelection(
                    state.commentText.length
                )
        }
        // -------------------------------------------------------------------------
// TIME LOGS
// -------------------------------------------------------------------------

        binding.progressIndicatorTimeLogs.isVisible =
            state.isTimeLogsLoading

        binding.textViewTimeLogsEmpty.isVisible =
            state.hasContent &&
                    state.areTimeLogsEmpty

        binding.textViewTimeLogsError.isVisible =
            !state.timeLogsErrorMessage
                .isNullOrBlank()

        binding.textViewTimeLogsError.text =
            state.timeLogsErrorMessage
                .orEmpty()

        timeLogAdapter.submitList(
            state.timeLogs
        )

        timeLogAdapter.refreshOperationState()

        state.timeLogSummary?.let { summary ->

            binding.cardTimeLogSummary.isVisible =
                true

            binding.textViewTimeLogEstimated.text =
                getString(
                    R.string.time_log_summary_estimated,

                    summary.estimatedHours
                        ?: 0.0
                )

            binding.textViewTimeLogActual.text =
                getString(
                    R.string.time_log_summary_actual,
                    summary.actualHours
                )

            binding.textViewTimeLogDifference.text =
                summary.differenceHours
                    ?.let {

                        getString(
                            R.string.time_log_summary_difference,
                            it
                        )
                    }
                    ?: getString(
                        R.string.time_log_summary_difference_empty
                    )

            binding.textViewTimeLogCount.text =
                getString(
                    R.string.time_log_summary_count,
                    summary.timeLogCount
                )

            binding.textViewTimeLogContributors.text =
                getString(
                    R.string.time_log_summary_contributors,
                    summary.contributorCount
                )

            binding.textViewTimeLogLastWorkDate.text =
                summary.lastWorkDateUtc
                    ?.let {

                        getString(
                            R.string.time_log_summary_last_work,

                            dateTimeFormatter
                                .formatUtcDateTime(it)
                        )
                    }
                    ?: getString(
                        R.string.time_log_summary_no_work
                    )
        }

        binding.buttonAddTimeLog
            .isEnabled =
            state.canCreateTimeLog

        binding.buttonAddTimeLog
            .alpha =
            if (
                state.canCreateTimeLog
            ) {
                1.0f
            } else {
                DISABLED_VIEW_ALPHA
            }

        binding.swipeRefreshTaskDetail.isEnabled =
            !state.isTimeLogCreating &&
                    state.processingTimeLogId == null
        // -------------------------------------------------------------------------
// TASK HISTORY
// -------------------------------------------------------------------------

        binding.progressIndicatorTaskHistories.isVisible =
            state.isHistoriesLoading

        binding.textViewTaskHistoriesError.isVisible =
            !state.historiesErrorMessage
                .isNullOrBlank()

        binding.textViewTaskHistoriesError.text =
            state.historiesErrorMessage
                .orEmpty()

        binding.textViewTaskHistoriesEmpty.isVisible =
            state.hasContent &&
                    state.areHistoriesEmpty

        historyAdapter.submitList(
            state.histories
        )
    }

    private fun renderTask(
        task: Task
    ) {

        binding.textViewTaskDetailTitle.text =
            task.title

        binding.textViewTaskDetailProject.text =
            getString(
                R.string.task_detail_project_format,
                task.projectName
            )

        binding.textViewTaskDetailDescription.text =
            task.description
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: getString(
                    R.string.tasks_no_description
                )

        binding.textViewTaskDetailStatus.text =
            task.status

        binding.textViewTaskDetailPriority.text =
            task.priority

        binding.textViewTaskDetailAssignedUser.text =
            getString(
                R.string.task_detail_assigned_format,

                task.assignedToUserFullName
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: getString(
                        R.string.tasks_unassigned
                    )
            )

        binding.textViewTaskDetailCreatedBy.text =
            getString(
                R.string.task_detail_created_by_format,
                task.createdByUserFullName
            )

        binding.textViewTaskDetailDueDate.text =
            getString(
                R.string.task_detail_due_date_format,

                task.dueDateUtc
                    ?.let {
                        dateTimeFormatter
                            .formatUtcDateTime(
                                it
                            )
                    }
                    ?: getString(
                        R.string.task_detail_not_specified
                    )
            )

        binding.textViewTaskDetailEstimatedHours.text =
            task.estimatedHours
                ?.let {

                    getString(
                        R.string.task_detail_estimated_hours_format,
                        it
                    )
                }
                ?: getString(
                    R.string.task_detail_estimated_hours_empty
                )

        binding.textViewTaskDetailActualHours.text =
            getString(
                R.string.task_detail_actual_hours_format,
                task.actualHours
            )

        binding.textViewTaskDetailCommentCount.text =
            getString(
                R.string.task_detail_comment_count_format,
                task.commentCount
            )

        binding.textViewTaskDetailCompletedAt.text =
            task.completedAtUtc
                ?.let {

                    getString(
                        R.string.task_detail_completed_at_format,

                        dateTimeFormatter
                            .formatUtcDateTime(
                                it
                            )
                    )
                }
                ?: getString(
                    R.string.task_detail_not_completed
                )

        binding.textViewTaskDetailCreatedAt.text =
            getString(
                R.string.task_detail_created_at_format,

                dateTimeFormatter
                    .formatUtcDateTime(
                        task.createdAtUtc
                    )
            )

        binding.textViewTaskDetailUpdatedAt.text =
            task.updatedAtUtc
                ?.let {

                    getString(
                        R.string.task_detail_updated_at_format,

                        dateTimeFormatter
                            .formatUtcDateTime(
                                it
                            )
                    )
                }
                ?: getString(
                    R.string.task_detail_never_updated
                )

        binding.textViewTaskDetailOverdue.isVisible =
            task.isOverdue
    }

    private fun configureTimeLogList() {

        timeLogAdapter =
            TaskTimeLogAdapter(

                dateTimeFormatter =
                dateTimeFormatter,

                processingTimeLogId = {
                    latestUiState.processingTimeLogId
                },

                onEditClicked = { item ->

                    showTimeLogDialog(
                        existingTimeLog = item
                    )
                },

                onDeleteClicked = { item ->

                    showDeleteTimeLogDialog(
                        item
                    )
                }
            )

        binding.recyclerViewTimeLogs.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                timeLogAdapter

            isNestedScrollingEnabled =
                false
        }

        binding.buttonAddTimeLog
            .setOnClickListener {

                if (
                    !latestUiState.canCreateTimeLog
                ) {

                    showWriteOperationNotAllowedMessage()

                    return@setOnClickListener
                }

                showTimeLogDialog(
                    existingTimeLog = null
                )
            }
    }
    // =========================================================================
// TIME LOG DIALOG
// =========================================================================

    private fun showTimeLogDialog(
        existingTimeLog: TaskTimeLog?
    ) {

        /*
         * Create işleminde kullanıcının time-log oluşturma yetkisini
         * tekrar kontrol ediyoruz.
         *
         * Edit işleminde ise item'ın kendi canEdit alanı backend tarafından
         * hesaplandığı için onu kullanıyoruz.
         */
        if (
            existingTimeLog == null &&
            !latestUiState.canCreateTimeLog
        ) {

            showWriteOperationNotAllowedMessage()

            return
        }

        if (
            existingTimeLog != null &&
            !existingTimeLog.canEdit
        ) {

            Snackbar.make(
                binding.root,
                "Bu zaman kaydını düzenleme yetkiniz bulunmuyor.",
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }

        val dialogBinding =
            DialogTaskTimeLogBinding.inflate(
                layoutInflater
            )

        val isEditing =
            existingTimeLog != null

        // ---------------------------------------------------------------------
        // DIALOG TITLE
        // ---------------------------------------------------------------------

        dialogBinding.textViewTimeLogDialogTitle
            .text =
            if (
                isEditing
            ) {

                getString(
                    R.string.time_log_edit_title
                )

            } else {

                getString(
                    R.string.time_log_add_title
                )
            }

        // ---------------------------------------------------------------------
        // INITIAL VALUES
        // ---------------------------------------------------------------------

        if (
            existingTimeLog != null
        ) {

            dialogBinding.editTextTimeLogHours
                .setText(
                    existingTimeLog.hours
                        .toString()
                )

            dialogBinding.editTextTimeLogDescription
                .setText(
                    existingTimeLog.description
                        .orEmpty()
                )

            dialogBinding.editTextTimeLogWorkDate
                .setText(
                    apiDateFormatter
                        .apiDateToDisplayDate(
                            existingTimeLog.workDateUtc
                        )
                )

        } else {

            /*
             * Yeni kayıt açılırken çalışma tarihi varsayılan olarak bugün.
             */
            dialogBinding.editTextTimeLogWorkDate
                .setText(
                    apiDateFormatter
                        .todayDisplayDate()
                )
        }

        // ---------------------------------------------------------------------
        // WORK DATE FIELD
        // ---------------------------------------------------------------------

        /*
         * Kullanıcı tarihi manuel yazmasın.
         *
         * Böylece:
         *
         * 16/08/2026
         * 2026-08-16
         * abc
         *
         * gibi geçersiz girişlerin önüne geçiyoruz.
         */
        dialogBinding.editTextTimeLogWorkDate.apply {

            isFocusable =
                false

            isFocusableInTouchMode =
                false

            isCursorVisible =
                false

            isClickable =
                true

            setOnClickListener {

                showTimeLogDatePicker(
                    dialogBinding =
                    dialogBinding
                )
            }
        }

        // ---------------------------------------------------------------------
        // CREATE DIALOG
        // ---------------------------------------------------------------------

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(
                    dialogBinding.root
                )
                .setNegativeButton(
                    R.string.action_cancel,
                    null
                )
                .setPositiveButton(
                    R.string.action_save,
                    null
                )
                .create()

        // ---------------------------------------------------------------------
        // SAVE
        // ---------------------------------------------------------------------

        dialog.setOnShowListener {

            dialog
                .getButton(
                    AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {

                    // =============================================================
                    // HOURS
                    // =============================================================

                    dialogBinding
                        .textInputLayoutTimeLogHours
                        .error =
                        null

                    val hoursText =
                        dialogBinding
                            .editTextTimeLogHours
                            .text
                            ?.toString()
                            .orEmpty()
                            .trim()

                    val hours =
                        hoursText
                            .replace(
                                ",",
                                "."
                            )
                            .toDoubleOrNull()

                    if (
                        hours == null ||
                        hours <= 0.0
                    ) {

                        dialogBinding
                            .textInputLayoutTimeLogHours
                            .error =
                            getString(
                                R.string.time_log_invalid_hours
                            )

                        return@setOnClickListener
                    }

                    // =============================================================
                    // WORK DATE
                    // =============================================================

                    dialogBinding
                        .textInputLayoutTimeLogWorkDate
                        .error =
                        null

                    val displayDate =
                        dialogBinding
                            .editTextTimeLogWorkDate
                            .text
                            ?.toString()
                            .orEmpty()
                            .trim()

                    if (
                        displayDate.isBlank()
                    ) {

                        dialogBinding
                            .textInputLayoutTimeLogWorkDate
                            .error =
                            getString(
                                R.string.time_log_invalid_date
                            )

                        return@setOnClickListener
                    }

                    /*
                     * DatePicker zaten gelecek tarih seçimine izin vermiyor.
                     *
                     * Buna rağmen burada ayrıca kontrol yapıyoruz.
                     *
                     * Böylece View herhangi bir nedenle programatik olarak
                     * gelecekteki tarih alırsa HTTP request göndermeyiz.
                     */
                    if (
                        isFutureDisplayDate(
                            displayDate
                        )
                    ) {

                        dialogBinding
                            .textInputLayoutTimeLogWorkDate
                            .error =
                            "Çalışma tarihi gelecekte olamaz."

                        return@setOnClickListener
                    }

                    val apiDate =
                        apiDateFormatter
                            .displayDateToApiUtc(
                                displayDate
                            )

                    if (
                        apiDate == null
                    ) {

                        dialogBinding
                            .textInputLayoutTimeLogWorkDate
                            .error =
                            getString(
                                R.string.time_log_invalid_date
                            )

                        return@setOnClickListener
                    }

                    // =============================================================
                    // DESCRIPTION
                    // =============================================================

                    val description =
                        dialogBinding
                            .editTextTimeLogDescription
                            .text
                            ?.toString()
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            }

                    // =============================================================
                    // CREATE
                    // =============================================================

                    if (
                        existingTimeLog == null
                    ) {

                        /*
                         * Dialog açık kaldığı sırada permission context
                         * değişmiş olabilir.
                         *
                         * Mutation'dan hemen önce son kez kontrol ediyoruz.
                         */
                        if (
                            !latestUiState.canCreateTimeLog
                        ) {

                            dialog.dismiss()

                            showWriteOperationNotAllowedMessage()

                            return@setOnClickListener
                        }

                        viewModel.createTimeLog(

                            taskId =
                            taskId,

                            hours =
                            hours,

                            description =
                            description,

                            workDate =
                            apiDate
                        )

                    } else {

                        // =========================================================
                        // UPDATE
                        // =========================================================

                        if (
                            !existingTimeLog.canEdit
                        ) {

                            dialog.dismiss()

                            Snackbar.make(
                                binding.root,
                                "Bu zaman kaydını düzenleme yetkiniz bulunmuyor.",
                                Snackbar.LENGTH_SHORT
                            ).show()

                            return@setOnClickListener
                        }

                        viewModel.updateTimeLog(

                            taskId =
                            taskId,

                            timeLog =
                            existingTimeLog,

                            hours =
                            hours,

                            description =
                            description,

                            workDate =
                            apiDate
                        )
                    }

                    dialog.dismiss()
                }
        }

        dialog.show()
    }
    private fun showDeleteTimeLogDialog(
        timeLog: TaskTimeLog
    ) {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                R.string.time_log_delete_title
            )
            .setMessage(
                R.string.time_log_delete_message
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.action_delete
            ) { _, _ ->

                viewModel.deleteTimeLog(
                    taskId = taskId,
                    timeLog = timeLog
                )
            }
            .show()
    }
    /**
     * Görev geçmişi timeline RecyclerView'unu hazırlar.
     */
    private fun configureHistoryList() {

        historyAdapter =
            TaskHistoryAdapter(
                dateTimeFormatter =
                dateTimeFormatter
            )

        binding.recyclerViewTaskHistories.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                historyAdapter

            /*
             * Ana ekran zaten NestedScrollView kullandığı için RecyclerView'un
             * kendi scroll'unu kapatıyoruz.
             */
            isNestedScrollingEnabled =
                false
        }
    }
    override fun onDestroyView() {

        if (::commentAdapter.isInitialized) {

            binding.recyclerViewTaskComments.adapter =
                null
        }

        if (::timeLogAdapter.isInitialized) {

            binding.recyclerViewTimeLogs.adapter =
                null
        }

        if (::historyAdapter.isInitialized) {

            binding.recyclerViewTaskHistories.adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }
    private fun showTaskStatusDialog(
        task: Task
    ) {
        if (
            !latestUiState.canChangeTaskStatus
        ) {

            showTaskActionNotAllowedMessage()

            return
        }
        val statuses =
            TaskStatus.entries

        val labels =
            statuses.map { status ->

                when (status) {

                    TaskStatus.TODO ->
                        getString(
                            R.string.tasks_status_todo
                        )

                    TaskStatus.IN_PROGRESS ->
                        getString(
                            R.string.tasks_status_in_progress
                        )

                    TaskStatus.IN_REVIEW ->
                        getString(
                            R.string.tasks_status_in_review
                        )

                    TaskStatus.DONE ->
                        getString(
                            R.string.tasks_status_done
                        )
                }
            }
                .toTypedArray()

        val currentStatus =
            TaskStatus.fromApiValue(
                task.status
            )

        val currentIndex =
            statuses
                .indexOf(
                    currentStatus
                )
                .takeIf {
                    it >= 0
                }
                ?: 0

        var selectedIndex =
            currentIndex

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setTitle(
                    R.string.task_action_status_dialog_title
                )
                .setSingleChoiceItems(
                    labels,
                    currentIndex
                ) { _, position ->

                    selectedIndex =
                        position
                }
                .setNegativeButton(
                    R.string.action_cancel,
                    null
                )
                .setPositiveButton(
                    R.string.action_save,
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            )
                .setOnClickListener {

                    val selectedStatus =
                        statuses[selectedIndex]

                    if (
                        selectedStatus ==
                        currentStatus
                    ) {

                        Snackbar.make(
                            binding.root,
                            R.string.task_action_status_not_changed,
                            Snackbar.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    taskActionsViewModel
                        .updateStatus(
                            taskId = task.id,
                            currentStatus = task.status,
                            newStatus = selectedStatus
                        )

                    dialog.dismiss()
                }
        }

        dialog.show()
    }
    private fun showTaskAssignmentDialog(
        task: Task,
        members: List<com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember>
    ) {
        if (
            !latestUiState.canChangeTaskAssignment
        ) {

            showTaskActionNotAllowedMessage()

            return
        }
        /*
         * İlk seçenek null assignment temsil ediyor.
         */
        val labels =
            buildList {

                add(
                    getString(
                        R.string.task_action_unassigned
                    )
                )

                members.forEach { member ->

                    add(
                        getString(
                            R.string.task_action_member_format,
                            member.fullName,
                            member.projectRole
                        )
                    )
                }
            }
                .toTypedArray()

        /*
         * 0 = Atamayı kaldır
         *
         * member index'i bu yüzden +1.
         */
        val currentIndex =
            if (
                task.assignedToUserId ==
                null
            ) {

                0

            } else {

                val memberIndex =
                    members.indexOfFirst { member ->

                        member.userId ==
                                task.assignedToUserId
                    }

                if (memberIndex >= 0) {
                    memberIndex + 1
                } else {
                    0
                }
            }

        var selectedIndex =
            currentIndex

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setTitle(
                    R.string.task_action_assignment_dialog_title
                )
                .setSingleChoiceItems(
                    labels,
                    currentIndex
                ) { _, position ->

                    selectedIndex =
                        position
                }
                .setNegativeButton(
                    R.string.action_cancel,
                    null
                )
                .setPositiveButton(
                    R.string.action_save,
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            )
                .setOnClickListener {

                    val selectedUserId =
                        if (
                            selectedIndex == 0
                        ) {

                            null

                        } else {

                            members[
                                selectedIndex - 1
                            ].userId
                        }

                    if (
                        selectedUserId ==
                        task.assignedToUserId
                    ) {

                        Snackbar.make(
                            binding.root,
                            R.string.task_action_assignment_not_changed,
                            Snackbar.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    taskActionsViewModel
                        .assignTask(
                            taskId = task.id,

                            currentAssignedUserId =
                            task.assignedToUserId,

                            newAssignedUserId =
                            selectedUserId
                        )

                    dialog.dismiss()
                }
        }

        dialog.show()
    }
    private fun observeTaskActions() {

        // =========================================================================
        // ACTION STATE
        // =========================================================================

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                taskActionsViewModel
                    .uiState
                    .collect { actionState ->

                        val isBusy =
                            actionState.isAnyOperationRunning ||
                                    actionState.isMembersLoading

                        // ---------------------------------------------------------
                        // PROGRESS
                        // ---------------------------------------------------------

                        binding.progressIndicatorTaskAction
                            .isVisible =
                            isBusy

                        // ---------------------------------------------------------
                        // STATUS
                        // ---------------------------------------------------------

                        binding.buttonChangeTaskStatus
                            .isEnabled =
                            latestUiState.canChangeTaskStatus &&
                                    !isBusy

                        // ---------------------------------------------------------
                        // ASSIGNMENT
                        // ---------------------------------------------------------

                        binding.buttonChangeTaskAssignment
                            .isEnabled =
                            latestUiState.canChangeTaskAssignment &&
                                    !isBusy

                        // ---------------------------------------------------------
                        // EDIT
                        // ---------------------------------------------------------

                        binding.buttonEditTask
                            .isEnabled =
                            latestUiState.canEditTask &&
                                    !isBusy

                        // ---------------------------------------------------------
                        // DELETE
                        // ---------------------------------------------------------

                        binding.buttonDeleteTask
                            .isEnabled =
                            latestUiState.canDeleteTask &&
                                    !isBusy
                    }
            }
        }

        // =========================================================================
        // ACTION EVENTS
        // =========================================================================

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                taskActionsViewModel
                    .events
                    .collect { event ->

                        when (
                            event
                        ) {

                            // =====================================================
                            // STATUS UPDATED
                            // =====================================================

                            is TaskActionsUiEvent.StatusUpdated -> {

                                /*
                                 * Task endpoint'i güncellenmiş TaskResponseDto
                                 * döndürdüğü için ekranda direkt uyguluyoruz.
                                 */
                                viewModel.applyUpdatedTask(
                                    event.task
                                )

                                /*
                                 * Backend status değişikliğini history'ye
                                 * yazmış olabilir.
                                 */
                                viewModel.loadHistories(
                                    taskId =
                                    event.task.id
                                )

                                notifyTaskChanged(
                                    taskId =
                                    event.task.id
                                )

                                Snackbar.make(
                                    binding.root,
                                    event.message,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                            // =====================================================
                            // PROJECT MEMBERS LOADED
                            // =====================================================

                            is TaskActionsUiEvent.ProjectMembersLoaded -> {

                                /*
                                 * Request gönderildiğinden beri permission değişmiş
                                 * olabilir.
                                 *
                                 * Bu yüzden event geldiğinde tekrar kontrol ediyoruz.
                                 */
                                if (
                                    !latestUiState
                                        .canChangeTaskAssignment
                                ) {
                                    return@collect
                                }

                                val task =
                                    latestUiState.task
                                        ?: return@collect

                                showTaskAssignmentDialog(
                                    task = task,
                                    members = event.members
                                )
                            }

                            // =====================================================
                            // ASSIGNMENT UPDATED
                            // =====================================================

                            is TaskActionsUiEvent.AssignmentUpdated -> {

                                viewModel.applyUpdatedTask(
                                    event.task
                                )

                                viewModel.loadHistories(
                                    taskId =
                                    event.task.id
                                )

                                notifyTaskChanged(
                                    taskId =
                                    event.task.id
                                )

                                Snackbar.make(
                                    binding.root,
                                    event.message,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                            // =====================================================
                            // TASK DELETED
                            // =====================================================

                            is TaskActionsUiEvent.TaskDeleted -> {

                                findNavController()
                                    .previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.apply {

                                        set(
                                            TaskNavigationResult.TASK_ID,
                                            taskId
                                        )

                                        set(
                                            TaskNavigationResult.TASK_DELETED,
                                            true
                                        )
                                    }

                                /*
                                 * Silinen task'ın detay ekranında kalamayız.
                                 */
                                findNavController()
                                    .navigateUp()
                            }

                            // =====================================================
                            // MESSAGE
                            // =====================================================

                            is TaskActionsUiEvent.ShowMessage -> {

                                Snackbar.make(
                                    binding.root,
                                    event.message,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }
    }
    private fun showDeleteTaskDialog() {

        if (
            !latestUiState.canDeleteTask
        ) {

            showTaskActionNotAllowedMessage()

            return
        }

        val task =
            latestUiState.task
                ?: return

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                R.string.task_delete_dialog_title
            )
            .setMessage(
                getString(
                    R.string.task_delete_dialog_message,
                    task.title
                )
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.action_delete
            ) { _, _ ->

                /*
                 * Dialog açıldıktan sonra permission teorik olarak
                 * değişmiş olabilir.
                 *
                 * Mutasyon göndermeden önce son kez kontrol ediyoruz.
                 */
                if (
                    !latestUiState.canDeleteTask
                ) {

                    showTaskActionNotAllowedMessage()

                    return@setPositiveButton
                }

                taskActionsViewModel
                    .deleteTask(
                        task.id
                    )
            }
            .show()
    }
    private fun configureTaskFormResult() {

        childFragmentManager
            .setFragmentResultListener(
                TaskFormDialogFragment.REQUEST_TASK_SAVED,
                viewLifecycleOwner
            ) { _, result ->

                val changedTaskId =
                    result.getInt(
                        TaskFormDialogFragment.RESULT_TASK_ID,
                        taskId
                    )

                /*
                 * Güncel TaskResponseDto'yu yeniden alıyoruz.
                 */
                viewModel.loadTask(
                    taskId
                )

                /*
                 * Update sonucunda backend history üretmiş olabilir.
                 */
                viewModel.loadHistories(
                    taskId
                )

                /*
                 * EstimatedHours değiştirilmiş olabilir.
                 *
                 * Bu nedenle summary de güncellenmeli.
                 */
                viewModel.loadTimeLogs(
                    taskId
                )

                /*
                 * TasksFragment de geri dönüldüğünde güncel listeyi alsın.
                 */
                notifyTaskChanged(
                    changedTaskId
                )
            }
    }

    /**
     * TasksFragment'e görevde değişiklik olduğunu bildirir.
     *
     * Bu method doğrudan TasksFragment'i çağırmaz.
     * Navigation back stack üzerinden haberleşir.
     */
    private fun notifyTaskChanged(
        taskId: Int
    ) {

        findNavController()
            .previousBackStackEntry
            ?.savedStateHandle
            ?.apply {

                set(
                    TaskNavigationResult.TASK_ID,
                    taskId
                )

                set(
                    TaskNavigationResult.TASK_CHANGED,
                    true
                )
            }
    }
    // =========================================================================
    // TASK ACTION PERMISSION MESSAGE
    // =========================================================================

    private fun showTaskActionNotAllowedMessage() {

        Snackbar.make(
            binding.root,
            "Bu görev üzerinde bu işlemi yapma yetkiniz bulunmuyor.",
            Snackbar.LENGTH_SHORT
        ).show()
    }
// =========================================================================
// TASK ACTION PERMISSIONS
// =========================================================================

    /**
     * TaskDetailUiState içinde hesaplanan permission değerlerini
     * gerçek View görünürlüğüne dönüştürür.
     *
     * Fragment burada business rule hesaplamaz.
     *
     * Örneğin:
     *
     * "Member kendisine atanmış görevin status'ünü değiştirebilir"
     *
     * gibi bir kural burada bulunmaz.
     *
     * Bu karar TaskDetailUiState tarafından verilir.
     */
    private fun renderTaskActionPermissions(
        state: TaskDetailUiState
    ) {

        /*
         * Permission context henüz yüklenirken aksiyonları
         * göstermiyoruz.
         *
         * Böylece ekran açılırken birkaç milisaniyelik yanlış
         * button görünmesi oluşmaz.
         */
        val shouldShowActions =
            state.hasContent &&
                    !state.isPermissionContextLoading &&
                    state.hasAnyTaskAction

        binding.layoutTaskActions
            .isVisible =
            shouldShowActions

        // ---------------------------------------------------------------------
        // STATUS
        // ---------------------------------------------------------------------

        binding.buttonChangeTaskStatus
            .isVisible =
            state.canChangeTaskStatus

        // ---------------------------------------------------------------------
        // ASSIGN
        // ---------------------------------------------------------------------

        binding.buttonChangeTaskAssignment
            .isVisible =
            state.canChangeTaskAssignment

        // ---------------------------------------------------------------------
        // EDIT
        // ---------------------------------------------------------------------

        binding.buttonEditTask
            .isVisible =
            state.canEditTask

        // ---------------------------------------------------------------------
        // DELETE
        // ---------------------------------------------------------------------

        binding.buttonDeleteTask
            .isVisible =
            state.canDeleteTask
    }

    // =========================================================================
    // WRITE PERMISSION MESSAGE
    // =========================================================================

    private fun showWriteOperationNotAllowedMessage() {

        Snackbar.make(
            binding.root,
            "Bu görev üzerinde içerik ekleme yetkiniz bulunmuyor.",
            Snackbar.LENGTH_SHORT
        ).show()
    }
    // =========================================================================
// TIME LOG DATE PICKER
// =========================================================================

    /**
     * Time Log çalışma tarihini DatePicker üzerinden seçtirir.
     *
     * Backend iş kuralı:
     *
     * WorkDate gelecekte olamaz.
     *
     * Bu nedenle:
     *
     * datePicker.maxDate = bugün
     *
     * kullanıyoruz.
     */
    private fun showTimeLogDatePicker(
        dialogBinding: DialogTaskTimeLogBinding
    ) {

        val calendar =
            createCalendarFromTimeLogDate(
                dialogBinding
                    .editTextTimeLogWorkDate
                    .text
                    ?.toString()
                    .orEmpty()
            )

        val datePickerDialog =
            DatePickerDialog(
                requireContext(),

                { _, year, month, dayOfMonth ->

                    calendar.set(
                        Calendar.YEAR,
                        year
                    )

                    calendar.set(
                        Calendar.MONTH,
                        month
                    )

                    calendar.set(
                        Calendar.DAY_OF_MONTH,
                        dayOfMonth
                    )

                    val formatter =
                        SimpleDateFormat(
                            DISPLAY_DATE_PATTERN,
                            Locale.getDefault()
                        ).apply {

                            isLenient =
                                false
                        }

                    val selectedDate =
                        formatter.format(
                            calendar.time
                        )

                    dialogBinding
                        .editTextTimeLogWorkDate
                        .setText(
                            selectedDate
                        )

                    dialogBinding
                        .textInputLayoutTimeLogWorkDate
                        .error =
                        null
                },

                calendar.get(
                    Calendar.YEAR
                ),

                calendar.get(
                    Calendar.MONTH
                ),

                calendar.get(
                    Calendar.DAY_OF_MONTH
                )
            )

        /*
         * En önemli satır.
         *
         * Kullanıcı bugünden sonraki hiçbir tarihi seçemez.
         */
        datePickerDialog
            .datePicker
            .maxDate =
            getTodayEndForDatePicker()

        datePickerDialog.show()
    }

    /**
     * Time Log input'unda hali hazırda bir tarih varsa
     * DatePicker o tarihte açılır.
     *
     * Parse edilemiyorsa bugüne düşer.
     */
    private fun createCalendarFromTimeLogDate(
        value: String
    ): Calendar {

        val calendar =
            Calendar.getInstance()

        val normalizedValue =
            value.trim()

        if (
            normalizedValue.isBlank()
        ) {
            return calendar
        }

        val formatter =
            SimpleDateFormat(
                DISPLAY_DATE_PATTERN,
                Locale.getDefault()
            ).apply {

                isLenient =
                    false
            }

        val parsedDate =
            runCatching {

                formatter.parse(
                    normalizedValue
                )

            }.getOrNull()

        if (
            parsedDate != null
        ) {

            calendar.time =
                parsedDate
        }

        return calendar
    }
    /**
     * Kullanıcı tarafından seçilen çalışma tarihinin
     * bugünden sonra olup olmadığını kontrol eder.
     *
     * Saatleri sıfırlıyoruz.
     *
     * Çünkü burada sadece "gün" karşılaştırıyoruz.
     */
    private fun isFutureDisplayDate(
        value: String
    ): Boolean {

        val formatter =
            SimpleDateFormat(
                DISPLAY_DATE_PATTERN,
                Locale.getDefault()
            ).apply {

                isLenient =
                    false
            }

        val selectedDate =
            runCatching {

                formatter.parse(
                    value.trim()
                )

            }.getOrNull()
                ?: return false

        val selectedCalendar =
            Calendar.getInstance()
                .apply {

                    time =
                        selectedDate

                    clearTimePart()
                }

        val today =
            Calendar.getInstance()
                .apply {

                    clearTimePart()
                }

        return selectedCalendar.after(
            today
        )
    }
    /**
     * Tarih karşılaştırmalarında saatin sonucu etkilemesini engeller.
     *
     * Örneğin:
     *
     * bugün 18:30
     *
     * ile
     *
     * bugün 00:00
     *
     * aynı gün kabul edilir.
     */
    private fun Calendar.clearTimePart() {

        set(
            Calendar.HOUR_OF_DAY,
            0
        )

        set(
            Calendar.MINUTE,
            0
        )

        set(
            Calendar.SECOND,
            0
        )

        set(
            Calendar.MILLISECOND,
            0
        )
    }
    /**
     * DatePicker'ın bugünü seçilebilir bırakması için
     * bugünün son milisaniyesini verir.
     */
    private fun getTodayEndForDatePicker():
            Long {

        return Calendar
            .getInstance()
            .apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    23
                )

                set(
                    Calendar.MINUTE,
                    59
                )

                set(
                    Calendar.SECOND,
                    59
                )

                set(
                    Calendar.MILLISECOND,
                    999
                )
            }
            .timeInMillis
    }
    companion object {

        const val ARG_TASK_ID =
            "taskId"

        private const val INVALID_TASK_ID =
            -1
        private const val EDIT_TASK_DIALOG_TAG =
            "EditTaskDialog"
        private const val DISABLED_VIEW_ALPHA =
            0.45f
        private const val DISPLAY_DATE_PATTERN =
            "dd.MM.yyyy"
    }
}