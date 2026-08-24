package com.alperensarac.projectmanagementkotlin.feature.projects.edit

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.FragmentEditProjectBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Proje düzenleme ekranıdır.
 *
 * Formun gerçek sahibi ViewModel'dir.
 * Fragment yalnızca state'i render eder ve UI event'lerini ViewModel'e yollar.
 */
@AndroidEntryPoint
class EditProjectFragment :
    Fragment() {

    // =========================================================================
    // BINDING
    // =========================================================================

    private var _binding:
            FragmentEditProjectBinding? =
        null

    private val binding:
            FragmentEditProjectBinding
        get() =
            checkNotNull(_binding)

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel:
            EditProjectViewModel
            by viewModels()

    // =========================================================================
    // ARGUMENT
    // =========================================================================

    private val projectId: Int
        get() =
            requireArguments()
                .getInt(
                    ARG_PROJECT_ID,
                    INVALID_PROJECT_ID
                )

    /**
     * Kullanıcı confirmation sonrası gerçekten geri çıkmak istediğinde
     * dirty-form callback'inin tekrar devreye girmesini önler.
     */
    private var allowBackNavigation =
        false

    // =========================================================================
    // VIEW
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentEditProjectBinding.inflate(
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

        configureStatusDropdown()

        configureFormListeners()

        configureDatePickers()

        configureActions()

        configureBackNavigation()

        observeState()

        observeEvents()

        if (
            savedInstanceState == null &&
            viewModel.uiState
                .value
                .project == null
        ) {

            viewModel.loadProject(
                projectId = projectId
            )
        }
    }

    // =========================================================================
    // STATUS
    // =========================================================================

    private fun configureStatusDropdown() {

        val statuses =
            ProjectStatus.entries

        val labels =
            statuses.map {
                it.displayName
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteEditProjectStatus
            .setAdapter(
                adapter
            )

        binding.autoCompleteEditProjectStatus
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                viewModel.onStatusChanged(
                    statuses[position]
                )
            }
    }

    // =========================================================================
    // FORM
    // =========================================================================

    private fun configureFormListeners() {

        binding.editTextEditProjectName
            .doAfterTextChanged {

                viewModel.onNameChanged(
                    it?.toString()
                        .orEmpty()
                )
            }

        binding.editTextEditProjectDescription
            .doAfterTextChanged {

                viewModel.onDescriptionChanged(
                    it?.toString()
                        .orEmpty()
                )
            }

        binding.editTextEditProjectOwnerId
            .doAfterTextChanged {

                viewModel.onOwnerIdChanged(
                    it?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // DATE PICKERS
    // =========================================================================

    private fun configureDatePickers() {

        binding.editTextEditProjectStartDate
            .setOnClickListener {

                showDatePicker(
                    currentValue =
                    viewModel.uiState
                        .value
                        .startDate
                ) { value ->

                    viewModel.onStartDateChanged(
                        value
                    )
                }
            }

        binding.editTextEditProjectEndDate
            .setOnClickListener {

                showDatePicker(
                    currentValue =
                    viewModel.uiState
                        .value
                        .endDate
                ) { value ->

                    viewModel.onEndDateChanged(
                        value
                    )
                }
            }
    }

    private fun showDatePicker(
        currentValue: String,
        onDateSelected: (String) -> Unit
    ) {

        val calendar =
            Calendar.getInstance()

        /*
         * Mevcut tarih yyyy-MM-dd... şeklindeyse DatePicker'ı
         * o güne açıyoruz.
         */
        val currentDate =
            currentValue
                .takeIf {
                    it.length >= 10
                }
                ?.take(10)
                ?.split("-")

        if (
            currentDate?.size == 3
        ) {

            val year =
                currentDate[0]
                    .toIntOrNull()

            val month =
                currentDate[1]
                    .toIntOrNull()

            val day =
                currentDate[2]
                    .toIntOrNull()

            if (
                year != null &&
                month != null &&
                day != null
            ) {

                calendar.set(
                    year,
                    month - 1,
                    day
                )
            }
        }

        DatePickerDialog(
            requireContext(),
            {
                    _,
                    year,
                    month,
                    dayOfMonth ->

                val value =
                    String.format(
                        Locale.US,
                        "%04d-%02d-%02dT00:00:00",
                        year,
                        month + 1,
                        dayOfMonth
                    )

                onDateSelected(
                    value
                )
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
        ).show()
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================

    private fun configureActions() {

        binding.buttonEditProjectBack
            .setOnClickListener {

                handleBackNavigation()
            }

        binding.buttonEditProjectSave
            .setOnClickListener {

                viewModel.updateProject(
                    projectId = projectId
                )
            }
    }

    // =========================================================================
    // BACK / DIRTY FORM
    // =========================================================================

    private fun configureBackNavigation() {

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,

                object :
                    OnBackPressedCallback(
                        true
                    ) {

                    override fun handleOnBackPressed() {

                        handleBackNavigation()
                    }
                }
            )
    }

    private fun handleBackNavigation() {

        if (
            allowBackNavigation ||
            !viewModel.uiState
                .value
                .isFormChanged
        ) {

            allowBackNavigation =
                true

            findNavController()
                .navigateUp()

            return
        }

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                R.string.edit_project_unsaved_title
            )
            .setMessage(
                R.string.edit_project_unsaved_message
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.edit_project_unsaved_leave
            ) { _, _ ->

                allowBackNavigation =
                    true

                findNavController()
                    .navigateUp()
            }
            .show()
    }

    // =========================================================================
    // OBSERVE
    // =========================================================================

    private fun observeState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
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

    private fun observeEvents() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.events
                            .collect { event ->

                                when (
                                    event
                                ) {

                                    is EditProjectUiEvent.ProjectUpdated -> {

                                        /*
                                         * ProjectDetailFragment'a:
                                         *
                                         * "Proje güncellendi, GET ile tekrar getir."
                                         */
                                        findNavController()
                                            .previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set(
                                                RESULT_PROJECT_UPDATED,
                                                true
                                            )

                                        /*
                                         * ProjectsFragment seviyesinde de daha sonra
                                         * liste refresh'i yapılabilsin diye detay ekranı
                                         * sonucu üst seviyeye aktaracak.
                                         */

                                        Snackbar.make(
                                            binding.root,
                                            event.message,
                                            Snackbar.LENGTH_SHORT
                                        ).show()

                                        allowBackNavigation =
                                            true

                                        findNavController()
                                            .navigateUp()
                                    }
                                }
                            }
                    }
            }
    }

    // =========================================================================
    // RENDER
    // =========================================================================

    private fun renderState(
        state: EditProjectUiState
    ) {

        binding.progressIndicatorEditProject
            .isVisible =
            state.isBusy

        binding.layoutEditProjectContent
            .isVisible =
            state.hasContent

        binding.textViewEditProjectError
            .isVisible =
            !state.generalError
                .isNullOrBlank()

        binding.textViewEditProjectError.text =
            state.generalError
                .orEmpty()

        // ---------------------------------------------------------------------
        // ERRORS
        // ---------------------------------------------------------------------

        binding.textInputLayoutEditProjectName.error =
            state.nameError

        binding.textInputLayoutEditProjectDescription.error =
            state.descriptionError

        binding.textInputLayoutEditProjectStartDate.error =
            state.startDateError

        binding.textInputLayoutEditProjectEndDate.error =
            state.endDateError

        binding.textInputLayoutEditProjectOwnerId.error =
            state.ownerIdError

        binding.textInputLayoutEditProjectStatus.error =
            state.statusError

        // ---------------------------------------------------------------------
        // VALUES
        // ---------------------------------------------------------------------

        setTextIfDifferent(
            binding.editTextEditProjectName,
            state.name
        )

        setTextIfDifferent(
            binding.editTextEditProjectDescription,
            state.description
        )

        setTextIfDifferent(
            binding.editTextEditProjectStartDate,
            state.startDate
        )

        setTextIfDifferent(
            binding.editTextEditProjectEndDate,
            state.endDate
        )

        setTextIfDifferent(
            binding.editTextEditProjectOwnerId,
            state.ownerIdText
        )

        if (
            binding.autoCompleteEditProjectStatus
                .text
                ?.toString() !=
            state.selectedStatus.displayName
        ) {

            binding.autoCompleteEditProjectStatus
                .setText(
                    state.selectedStatus.displayName,
                    false
                )
        }

        // ---------------------------------------------------------------------
        // ENABLED
        // ---------------------------------------------------------------------

        val formEnabled =
            state.hasContent &&
                    state.canEditProject &&
                    !state.isBusy

        binding.editTextEditProjectName.isEnabled =
            formEnabled

        binding.editTextEditProjectDescription.isEnabled =
            formEnabled

        binding.editTextEditProjectStartDate.isEnabled =
            formEnabled

        binding.editTextEditProjectEndDate.isEnabled =
            formEnabled

        binding.autoCompleteEditProjectStatus.isEnabled =
            formEnabled

        /*
         * Sadece Admin owner değiştirebilir.
         */
        binding.editTextEditProjectOwnerId.isEnabled =
            formEnabled &&
                    state.canChangeOwner

        binding.textInputLayoutEditProjectOwnerId.helperText =
            if (
                state.canChangeOwner
            ) {

                getString(
                    R.string.edit_project_owner_helper
                )

            } else {

                getString(
                    R.string.edit_project_owner_manager_helper
                )
            }

        binding.buttonEditProjectSave.isEnabled =
            state.canSave
    }

    private fun setTextIfDifferent(
        editText:
        android.widget.EditText,
        value: String
    ) {

        if (
            editText.text
                ?.toString() !=
            value
        ) {

            editText.setText(
                value
            )
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

    companion object {

        const val ARG_PROJECT_ID =
            "projectId"

        const val RESULT_PROJECT_UPDATED =
            "result_project_updated"

        private const val INVALID_PROJECT_ID =
            -1
    }
}