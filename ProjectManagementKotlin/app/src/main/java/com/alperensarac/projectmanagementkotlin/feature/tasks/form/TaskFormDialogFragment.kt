package com.alperensarac.projectmanagementkotlin.feature.tasks.form

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.ApiDateFormatter
import com.alperensarac.projectmanagementkotlin.databinding.DialogTaskFormBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Görev oluşturma / düzenleme dialog'udur.
 *
 * Bu ekran:
 *
 * - görev başlığı girer,
 * - açıklama girer,
 * - proje üyelerinden görev ataması yapar,
 * - görev durumunu seçer,
 * - öncelik seçer,
 * - DatePicker üzerinden teslim tarihi seçer,
 * - tahmini çalışma süresi girer.
 *
 * Görev atamasında Viewer rolündeki kullanıcılar gösterilmez.
 */
@AndroidEntryPoint
class TaskFormDialogFragment :
    DialogFragment() {

    // =========================================================================
    // BINDING
    // =========================================================================

    private var _binding:
            DialogTaskFormBinding? =
        null

    private val binding:
            DialogTaskFormBinding
        get() =
            checkNotNull(_binding) {
                "DialogTaskFormBinding view lifecycle dışında kullanılamaz."
            }

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel:
            TaskFormViewModel
            by viewModels()

    // =========================================================================
    // FORMATTER
    // =========================================================================

    @Inject
    lateinit var apiDateFormatter:
            ApiDateFormatter

    // =========================================================================
    // EDIT DATA
    // =========================================================================

    /**
     * null:
     * Create modu.
     *
     * Task:
     * Edit modu.
     */
    private var initialTask:
            Task? =
        null

    // =========================================================================
    // ARGUMENTS
    // =========================================================================

    private val projectId: Int
        get() =
            requireArguments()
                .getInt(
                    ARG_PROJECT_ID,
                    INVALID_PROJECT_ID
                )

    // =========================================================================
    // DIALOG
    // =========================================================================

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        _binding =
            DialogTaskFormBinding.inflate(
                LayoutInflater.from(
                    requireContext()
                )
            )

        // ---------------------------------------------------------------------
        // FORM CONFIGURATION
        // ---------------------------------------------------------------------

        configureStatusDropdown()

        configurePriorityDropdown()

        configureDueDatePicker()

        // ---------------------------------------------------------------------
        // OBSERVERS
        // ---------------------------------------------------------------------

        observeState()

        observeEvents()

        /*
         * Görev atanabilecek kullanıcıları
         *
         * GET /api/projects/{projectId}/members
         *
         * üzerinden alıyoruz.
         *
         * /api/Users endpoint'ine burada ihtiyacımız yok.
         */
        /*
         * DueDate minimum sınırını belirleyebilmek için
         * proje detayını da yüklüyoruz.
         */
        viewModel.loadProject(
            projectId = projectId
        )

        viewModel.loadMembers(
            projectId = projectId
        )

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(
                    binding.root
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
         * AlertDialog positive button ancak dialog gösterildikten sonra
         * oluşturulur.
         *
         * Bu nedenle listener'ı burada bağlıyoruz.
         */
        dialog.setOnShowListener {

            dialog
                .getButton(
                    AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {

                    saveTask()
                }

            configureInitialValues()

            /*
             * Dialog ilk açıldığında mevcut ViewModel state'ini
             * bir kere doğrudan render ediyoruz.
             */
            renderState(
                viewModel.uiState.value
            )
        }

        return dialog
    }

    // =========================================================================
    // STATUS
    // =========================================================================

    private fun configureStatusDropdown() {

        val statuses =
            TaskStatus.entries

        val labels =
            statuses.map { status ->

                getStatusDisplayName(
                    status
                )
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteTaskFormStatus
            .setAdapter(
                adapter
            )

        binding.autoCompleteTaskFormStatus
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val selectedStatus =
                    statuses[position]

                viewModel.selectStatus(
                    selectedStatus
                )
            }
    }

    /**
     * API enum değerini kullanıcıya Türkçe gösterir.
     */
    private fun getStatusDisplayName(
        status: TaskStatus
    ): String {

        return when (
            status
        ) {

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

    // =========================================================================
    // PRIORITY
    // =========================================================================

    private fun configurePriorityDropdown() {

        val priorities =
            TaskPriority.entries

        val labels =
            priorities.map { priority ->

                getPriorityDisplayName(
                    priority
                )
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteTaskFormPriority
            .setAdapter(
                adapter
            )

        binding.autoCompleteTaskFormPriority
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val selectedPriority =
                    priorities[position]

                viewModel.selectPriority(
                    selectedPriority
                )
            }
    }

    private fun getPriorityDisplayName(
        priority: TaskPriority
    ): String {

        return when (
            priority
        ) {

            TaskPriority.LOW ->
                getString(
                    R.string.tasks_priority_low
                )

            TaskPriority.MEDIUM ->
                getString(
                    R.string.tasks_priority_medium
                )

            TaskPriority.HIGH ->
                getString(
                    R.string.tasks_priority_high
                )

            TaskPriority.CRITICAL ->
                getString(
                    R.string.tasks_priority_critical
                )
        }
    }

    // =========================================================================
    // DUE DATE
    // =========================================================================

    /**
     * Teslim tarihi alanını manuel yazılabilir bir input yerine
     * DatePicker olarak kullanıyoruz.
     *
     * UI formatı:
     *
     * dd.MM.yyyy
     *
     * Örnek:
     *
     * 16.08.2026
     */
    private fun configureDueDatePicker() {

        binding.editTextTaskFormDueDate
            .setOnClickListener {

                /*
                 * Form herhangi bir işlem yaparken DatePicker açılmasın.
                 */
                if (
                    viewModel.uiState.value.isBusy
                ) {
                    return@setOnClickListener
                }

                showDueDatePicker()
            }
    }

    /**
     * DatePicker'ı açar.
     *
     * Input içerisinde daha önce seçilmiş bir tarih varsa
     * picker o tarihten başlar.
     *
     * Tarih yoksa bugünün tarihi kullanılır.
     */
    private fun showDueDatePicker() {

        val calendar =
            createCalendarFromDueDateInput()

        val dialog =
            DatePickerDialog(
                requireContext(),

                { _, year, month, dayOfMonth ->

                    /*
                     * DatePicker month değerini 0 tabanlı verir.
                     *
                     * Calendar da 0 tabanlı ay kullandığı için
                     * burada +1 yapmıyoruz.
                     */
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

                    binding.editTextTaskFormDueDate
                        .setText(
                            selectedDate
                        )

                    /*
                     * Kullanıcı geçerli bir tarih seçti.
                     * Eski validation hatasını temizliyoruz.
                     */
                    binding.textInputLayoutTaskFormDueDate
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
         * Backend:
         *
         * DueDate >= Project.StartDate
         *
         * Eğer proje başlangıç tarihi başarılı şekilde yüklendiyse
         * DatePicker'da önceki tarihleri tamamen kapatıyoruz.
         */
        getProjectStartDateMillis()
            ?.let { projectStartMillis ->

                dialog.datePicker.minDate =
                    projectStartMillis
            }

        dialog.show()
    }

    /**
     * DueDate input'unda bir tarih varsa DatePicker'ın
     * başlangıç değerini o tarihe getirir.
     *
     * Örnek:
     *
     * Input:
     * 21.08.2026
     *
     * DatePicker:
     * 21 Ağustos 2026 tarihinde açılır.
     *
     * Parse başarısız olursa bugünün tarihine döner.
     */
    private fun createCalendarFromDueDateInput():
            Calendar {

        val calendar =
            Calendar.getInstance()

        val currentValue =
            binding.editTextTaskFormDueDate
                .text
                ?.toString()
                .orEmpty()
                .trim()

        if (
            currentValue.isBlank()
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
                    currentValue
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

    // =========================================================================
    // PROJECT START DATE
    // =========================================================================

    /**
     * Backend'den gelen Project.startDateUtc değerini DatePicker'ın
     * kullanabileceği gün başlangıcı millis değerine dönüştürür.
     *
     * Saat bilgisi özellikle dikkate alınmaz. Buradaki kural gün bazlıdır:
     *
     * DueDate >= Project.StartDate
     */
    private fun getProjectStartDateMillis(): Long? {

        val projectStartDateUtc =
            viewModel.uiState.value
                .projectStartDateUtc
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return null

        /*
         * Backend değeri DateTime olsa bile ilk 10 karakter tarih kısmıdır:
         *
         * 2026-08-10T14:30:00 -> 2026-08-10
         *
         * Böylece timezone dönüşümünün günü kaydırması engellenir.
         */
        val datePart =
            projectStartDateUtc
                .take(10)

        val formatter =
            SimpleDateFormat(
                API_DATE_ONLY_PATTERN,
                Locale.US
            ).apply {
                isLenient = false
            }

        val parsedDate =
            runCatching {
                formatter.parse(
                    datePart
                )
            }.getOrNull()
                ?: return null

        return Calendar
            .getInstance()
            .apply {
                time = parsedDate

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
            .timeInMillis
    }

    /**
     * Kullanıcının seçtiği teslim tarihinin proje başlangıç tarihinden
     * önce olup olmadığını gün bazında kontrol eder.
     *
     * DatePicker minDate ilk korumadır.
     * Bu method ise save öncesindeki ikinci korumadır.
     */
    private fun isDueDateBeforeProjectStart(
        dueDateDisplay: String
    ): Boolean {

        val projectStartMillis =
            getProjectStartDateMillis()
                ?: return false

        val formatter =
            SimpleDateFormat(
                DISPLAY_DATE_PATTERN,
                Locale.getDefault()
            ).apply {
                isLenient = false
            }

        val dueDate =
            runCatching {
                formatter.parse(
                    dueDateDisplay.trim()
                )
            }.getOrNull()
                ?: return false

        val dueDateMillis =
            Calendar
                .getInstance()
                .apply {
                    time = dueDate

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
                .timeInMillis

        return dueDateMillis <
                projectStartMillis
    }

    // =========================================================================
    // MEMBERS
    // =========================================================================

    /**
     * Görev atanabilecek kullanıcı dropdown'unu hazırlar.
     *
     * Buraya gelen liste TaskFormUiState.assignableMembers listesidir.
     *
     * Dolayısıyla:
     *
     * - kullanıcı aktiftir,
     * - proje üyeliği aktiftir,
     * - Viewer değildir.
     */
    private fun configureMemberDropdown(
        members: List<ProjectMember>,
        selectedAssignedUserId: Int?
    ) {

        val labels =
            buildList {

                /*
                 * İlk seçenek her zaman:
                 *
                 * AssignedToUserId = null
                 */
                add(
                    getString(
                        R.string.tasks_unassigned
                    )
                )

                members.forEach { member ->

                    add(
                        createMemberLabel(
                            member
                        )
                    )
                }
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteTaskFormAssignedUser
            .setAdapter(
                adapter
            )

        binding.autoCompleteTaskFormAssignedUser
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val selectedUserId =
                    if (
                        position == 0
                    ) {

                        /*
                         * Atamasız görev.
                         */
                        null

                    } else {

                        members[
                            position - 1
                        ].userId
                    }

                viewModel.selectAssignedUser(
                    selectedUserId
                )
            }

        renderSelectedAssignedUser(
            members = members,
            selectedAssignedUserId =
            selectedAssignedUserId
        )
    }

    // =========================================================================
    // SELECTED ASSIGNEE
    // =========================================================================

    private fun renderSelectedAssignedUser(
        members: List<ProjectMember>,
        selectedAssignedUserId: Int?
    ) {

        // ---------------------------------------------------------------------
        // UNASSIGNED
        // ---------------------------------------------------------------------

        if (
            selectedAssignedUserId == null
        ) {

            binding.autoCompleteTaskFormAssignedUser
                .setText(
                    getString(
                        R.string.tasks_unassigned
                    ),
                    false
                )

            return
        }

        // ---------------------------------------------------------------------
        // VALID MEMBER
        // ---------------------------------------------------------------------

        val selectedMember =
            members.firstOrNull { member ->

                member.userId ==
                        selectedAssignedUserId
            }

        if (
            selectedMember != null
        ) {

            binding.autoCompleteTaskFormAssignedUser
                .setText(
                    createMemberLabel(
                        selectedMember
                    ),
                    false
                )

            return
        }

        // ---------------------------------------------------------------------
        // OLD / INVALID ASSIGNMENT
        // ---------------------------------------------------------------------

        /*
         * Edit edilen görev daha önce bir kullanıcıya atanmış olabilir.
         *
         * Kullanıcı sonradan:
         *
         * - projeden çıkarılmış,
         * - pasif yapılmış,
         * - Viewer yapılmış
         *
         * olabilir.
         *
         * Eski seçimi sessizce null'a çevirmiyoruz.
         */
        val previousAssigneeName =
            initialTask
                ?.assignedToUserFullName
                ?.takeIf {
                    it.isNotBlank()
                }

        val text =
            previousAssigneeName
                ?.let { name ->

                    "$name (artık atanamaz)"
                }
                ?: "Mevcut kullanıcı artık atanamaz"

        binding.autoCompleteTaskFormAssignedUser
            .setText(
                text,
                false
            )
    }

    // =========================================================================
    // MEMBER LABEL
    // =========================================================================

    private fun createMemberLabel(
        member: ProjectMember
    ): String {

        val role =
            ProjectMemberRole.fromApiValue(
                member.projectRole
            )

        val roleText =
            when (
                role
            ) {

                ProjectMemberRole.MEMBER ->
                    getString(
                        R.string.project_member_role_member
                    )

                ProjectMemberRole.CONTRIBUTOR ->
                    getString(
                        R.string.project_member_role_contributor
                    )

                ProjectMemberRole.VIEWER ->
                    getString(
                        R.string.project_member_role_viewer
                    )

                null ->
                    member.projectRole
            }

        return getString(
            R.string.task_action_member_format,
            member.fullName,
            roleText
        )
    }

    // =========================================================================
    // INITIAL TASK
    // =========================================================================

    /**
     * Edit modunda mevcut görevi dialog'a verir.
     */
    fun setInitialTask(
        task: Task
    ) {

        initialTask =
            task
    }

    // =========================================================================
    // INITIAL VALUES
    // =========================================================================

    private fun configureInitialValues() {

        val task =
            initialTask

        // ---------------------------------------------------------------------
        // TITLE
        // ---------------------------------------------------------------------

        binding.textViewTaskFormTitle
            .text =
            if (
                task == null
            ) {

                getString(
                    R.string.task_form_create_title
                )

            } else {

                getString(
                    R.string.task_form_edit_title
                )
            }

        // ---------------------------------------------------------------------
        // CREATE MODE
        // ---------------------------------------------------------------------

        if (
            task == null
        ) {

            /*
             * ViewModel default:
             *
             * Todo
             * Medium
             *
             * Fakat kullanıcı dialog açılır açılmaz da
             * doğru text'i görsün.
             */
            binding.autoCompleteTaskFormStatus
                .setText(
                    getString(
                        R.string.tasks_status_todo
                    ),
                    false
                )

            binding.autoCompleteTaskFormPriority
                .setText(
                    getString(
                        R.string.tasks_priority_medium
                    ),
                    false
                )

            binding.autoCompleteTaskFormAssignedUser
                .setText(
                    getString(
                        R.string.tasks_unassigned
                    ),
                    false
                )

            return
        }

        // ---------------------------------------------------------------------
        // EDIT MODE
        // ---------------------------------------------------------------------

        /*
         * Task'ın status / priority / assignee bilgilerini
         * ViewModel state'ine taşıyoruz.
         */
        viewModel.initializeForEdit(
            task
        )

        binding.editTextTaskFormTitle
            .setText(
                task.title
            )

        binding.editTextTaskFormDescription
            .setText(
                task.description
                    .orEmpty()
            )

        binding.editTextTaskFormEstimatedHours
            .setText(
                task.estimatedHours
                    ?.toString()
                    .orEmpty()
            )

        binding.editTextTaskFormDueDate
            .setText(
                task.dueDateUtc
                    ?.let { date ->

                        apiDateFormatter
                            .apiDateToDisplayDate(
                                date
                            )
                    }
                    .orEmpty()
            )
    }

    // =========================================================================
    // SAVE
    // =========================================================================

    private fun saveTask() {

        val state =
            viewModel.uiState.value

        if (
            state.isBusy
        ) {
            return
        }

        // ---------------------------------------------------------------------
        // TITLE
        // ---------------------------------------------------------------------

        binding.textInputLayoutTaskFormTitle
            .error =
            null

        val title =
            binding.editTextTaskFormTitle
                .text
                ?.toString()
                .orEmpty()
                .trim()

        if (
            title.isBlank()
        ) {

            binding.textInputLayoutTaskFormTitle
                .error =
                getString(
                    R.string.task_form_title_required
                )

            return
        }

        // ---------------------------------------------------------------------
        // DESCRIPTION
        // ---------------------------------------------------------------------

        val description =
            binding.editTextTaskFormDescription
                .text
                ?.toString()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        // ---------------------------------------------------------------------
        // ESTIMATED HOURS
        // ---------------------------------------------------------------------

        binding.textInputLayoutTaskFormEstimatedHours
            .error =
            null

        val estimatedText =
            binding.editTextTaskFormEstimatedHours
                .text
                ?.toString()
                .orEmpty()
                .trim()

        val estimatedHours =
            if (
                estimatedText.isBlank()
            ) {

                null

            } else {

                estimatedText
                    .replace(
                        ",",
                        "."
                    )
                    .toDoubleOrNull()
            }

        if (
            estimatedText.isNotBlank() &&
            (
                    estimatedHours == null ||
                            estimatedHours <= 0.0
                    )
        ) {

            binding.textInputLayoutTaskFormEstimatedHours
                .error =
                getString(
                    R.string.task_form_invalid_hours
                )

            return
        }

        // ---------------------------------------------------------------------
        // DUE DATE
        // ---------------------------------------------------------------------

        binding.textInputLayoutTaskFormDueDate
            .error =
            null

        val dueDateText =
            binding.editTextTaskFormDueDate
                .text
                ?.toString()
                .orEmpty()
                .trim()

        val dueDate =
            if (
                dueDateText.isBlank()
            ) {

                null

            } else {

                apiDateFormatter
                    .displayDateToApiUtc(
                        dueDateText
                    )
            }

        if (
            dueDateText.isNotBlank() &&
            dueDate == null
        ) {

            binding.textInputLayoutTaskFormDueDate
                .error =
                getString(
                    R.string.task_form_invalid_date
                )

            return
        }

        /*
         * DatePicker minimum tarih uygulasa da save öncesinde tekrar kontrol
         * ediyoruz. Böylece alan programatik olarak değiştirilirse de backend'e
         * geçersiz request göndermeyiz.
         */
        if (
            dueDateText.isNotBlank() &&
            isDueDateBeforeProjectStart(
                dueDateText
            )
        ) {

            binding.textInputLayoutTaskFormDueDate
                .error =
                "Görev teslim tarihi proje başlangıç tarihinden önce olamaz."

            return
        }

        // ---------------------------------------------------------------------
        // CREATE / UPDATE
        // ---------------------------------------------------------------------

        val task =
            initialTask

        if (
            task == null
        ) {

            viewModel.createTask(

                projectId =
                projectId,

                title =
                title,

                description =
                description,

                dueDate =
                dueDate,

                estimatedHours =
                estimatedHours
            )

        } else {

            viewModel.updateTask(

                taskId =
                task.id,

                title =
                title,

                description =
                description,

                dueDate =
                dueDate,

                estimatedHours =
                estimatedHours
            )
        }
    }

    // =========================================================================
    // OBSERVE STATE
    // =========================================================================

    private fun observeState() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.uiState
                    .collect { state ->

                        renderState(
                            state
                        )
                    }
            }
        }
    }

    // =========================================================================
    // RENDER STATE
    // =========================================================================

    private fun renderState(
        state: TaskFormUiState
    ) {

        // ---------------------------------------------------------------------
        // PROGRESS
        // ---------------------------------------------------------------------

        binding.progressIndicatorTaskForm
            .isVisible =
            state.isBusy

        // ---------------------------------------------------------------------
        // ASSIGNEE
        // ---------------------------------------------------------------------

        configureMemberDropdown(

            members =
            state.assignableMembers,

            selectedAssignedUserId =
            state.selectedAssignedUserId
        )

        binding.autoCompleteTaskFormAssignedUser
            .isEnabled =
            !state.isBusy

        // ---------------------------------------------------------------------
        // STATUS
        // ---------------------------------------------------------------------

        binding.autoCompleteTaskFormStatus
            .isEnabled =
            !state.isBusy

        binding.autoCompleteTaskFormStatus
            .setText(
                getStatusDisplayName(
                    state.selectedStatus
                ),
                false
            )

        // ---------------------------------------------------------------------
        // PRIORITY
        // ---------------------------------------------------------------------

        binding.autoCompleteTaskFormPriority
            .isEnabled =
            !state.isBusy

        binding.autoCompleteTaskFormPriority
            .setText(
                getPriorityDisplayName(
                    state.selectedPriority
                ),
                false
            )

        // ---------------------------------------------------------------------
        // INPUTS
        // ---------------------------------------------------------------------

        binding.editTextTaskFormTitle
            .isEnabled =
            !state.isBusy

        binding.editTextTaskFormDescription
            .isEnabled =
            !state.isBusy

        binding.editTextTaskFormDueDate
            .isEnabled =
            !state.isBusy

        binding.editTextTaskFormEstimatedHours
            .isEnabled =
            !state.isBusy

        // ---------------------------------------------------------------------
        // GENERAL ERROR
        // ---------------------------------------------------------------------

        binding.textViewTaskFormError
            .isVisible =
            !state.errorMessage
                .isNullOrBlank()

        binding.textViewTaskFormError
            .text =
            state.errorMessage
                .orEmpty()

        // ---------------------------------------------------------------------
        // SAVE BUTTON
        // ---------------------------------------------------------------------

        (dialog as? AlertDialog)
            ?.getButton(
                AlertDialog.BUTTON_POSITIVE
            )
            ?.isEnabled =
            !state.isBusy
    }

    // =========================================================================
    // EVENTS
    // =========================================================================

    private fun observeEvents() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.events
                    .collect { event ->

                        when (
                            event
                        ) {

                            // =================================================
                            // SAVED
                            // =================================================

                            is TaskFormUiEvent.TaskSaved -> {

                                parentFragmentManager
                                    .setFragmentResult(

                                        REQUEST_TASK_SAVED,

                                        Bundle().apply {

                                            putInt(
                                                RESULT_TASK_ID,
                                                event.task.id
                                            )
                                        }
                                    )

                                dismiss()
                            }

                            // =================================================
                            // MESSAGE
                            // =================================================

                            is TaskFormUiEvent.ShowMessage -> {

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
    // DESTROY
    // =========================================================================

    override fun onDestroyView() {

        _binding =
            null

        super.onDestroyView()
    }

    // =========================================================================
    // FACTORY
    // =========================================================================

    companion object {

        const val REQUEST_TASK_SAVED =
            "task_saved"

        const val RESULT_TASK_ID =
            "task_id"

        private const val ARG_PROJECT_ID =
            "projectId"

        private const val ARG_TASK_ID =
            "taskId"

        private const val INVALID_PROJECT_ID =
            -1

        private const val NO_TASK_ID =
            -1

        private const val DISPLAY_DATE_PATTERN =
            "dd.MM.yyyy"

        private const val API_DATE_ONLY_PATTERN =
            "yyyy-MM-dd"

        // =====================================================================
        // CREATE
        // =====================================================================

        fun newCreateInstance(
            projectId: Int
        ): TaskFormDialogFragment {

            return TaskFormDialogFragment()
                .apply {

                    arguments =
                        Bundle().apply {

                            putInt(
                                ARG_PROJECT_ID,
                                projectId
                            )
                        }
                }
        }



        // =====================================================================
        // EDIT
        // =====================================================================

        fun newEditInstance(
            task: Task
        ): TaskFormDialogFragment {

            return TaskFormDialogFragment()
                .apply {

                    arguments =
                        Bundle().apply {

                            putInt(
                                ARG_PROJECT_ID,
                                task.projectId
                            )

                            putInt(
                                ARG_TASK_ID,
                                task.id
                            )
                        }

                    setInitialTask(
                        task
                    )
                }
        }
    }
}
