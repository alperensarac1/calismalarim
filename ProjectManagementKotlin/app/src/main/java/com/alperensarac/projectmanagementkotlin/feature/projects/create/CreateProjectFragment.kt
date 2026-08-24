package com.alperensarac.projectmanagementkotlin.feature.projects.create

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.FragmentCreateProjectBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.alperensarac.projectmanagementkotlin.feature.projects.create.owner.ProjectOwnerSelectionDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Yeni proje oluşturma ekranıdır.
 *
 * Owner seçimi artık ID yazarak yapılmaz.
 *
 * Admin:
 *
 * - kullanıcı seçim alanına tıklar
 * - kullanıcıyı arar
 * - listeden seçer
 * - User.id arka planda OwnerId olarak gönderilir
 *
 * ProjectManager:
 *
 * - owner seçimi yapamaz
 * - backend otomatik olarak kendisini owner yapar
 */
@AndroidEntryPoint
class CreateProjectFragment :
    Fragment() {

    // =========================================================================
    // BINDING
    // =========================================================================

    private var _binding:
            FragmentCreateProjectBinding? =
        null

    private val binding:
            FragmentCreateProjectBinding
        get() =
            checkNotNull(
                _binding
            )

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel:
            CreateProjectViewModel
            by viewModels()

    // =========================================================================
    // STATUS
    // =========================================================================

    private var selectedStatus:
            ProjectStatus =
        ProjectStatus.PLANNING

    // =========================================================================
    // OWNER
    // =========================================================================

    /**
     * Backend'e gönderilecek gerçek User.id.
     *
     * null:
     *
     * Owner seçilmemiştir.
     *
     * Admin için backend mevcut kullanıcıyı owner yapar.
     */
    private var selectedOwnerId:
            Int? =
        null

    private var selectedOwnerFullName:
            String? =
        null

    private var selectedOwnerEmail:
            String? =
        null

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentCreateProjectBinding.inflate(
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

        configureDatePickers()

        configureOwnerSelection()

        configureListeners()

        configureOwnerResult()

        observeUiState()

        observeEvents()
    }

    // =========================================================================
    // STATUS
    // =========================================================================

    private fun configureStatusDropdown() {

        val statuses =
            ProjectStatus.entries

        val displayNames =
            statuses.map {
                it.displayName
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayNames
            )

        binding.autoCompleteProjectCreateStatus
            .setAdapter(
                adapter
            )

        selectedStatus =
            ProjectStatus.PLANNING

        binding.autoCompleteProjectCreateStatus
            .setText(
                selectedStatus.displayName,
                false
            )

        binding.autoCompleteProjectCreateStatus
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                selectedStatus =
                    statuses[position]
            }
    }

    // =========================================================================
    // DATE PICKERS
    // =========================================================================

    private fun configureDatePickers() {

        binding.editTextProjectStartDate
            .setOnClickListener {

                showDatePicker { formattedDate ->

                    binding.editTextProjectStartDate
                        .setText(
                            formattedDate
                        )

                    binding.textInputLayoutProjectStartDate
                        .error =
                        null
                }
            }

        binding.editTextProjectEndDate
            .setOnClickListener {

                showDatePicker { formattedDate ->

                    binding.editTextProjectEndDate
                        .setText(
                            formattedDate
                        )

                    binding.textInputLayoutProjectEndDate
                        .error =
                        null
                }
            }
    }

    /**
     * Backend generic DateTime kullandığı için timezone eklemiyoruz.
     */
    private fun showDatePicker(
        onDateSelected:
            (String) -> Unit
    ) {

        val calendar =
            Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            {
                    _,
                    year,
                    month,
                    dayOfMonth ->

                val formattedDate =
                    String.format(
                        Locale.US,
                        "%04d-%02d-%02dT00:00:00",
                        year,
                        month + 1,
                        dayOfMonth
                    )

                onDateSelected(
                    formattedDate
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
    // OWNER SELECTION
    // =========================================================================

    private fun configureOwnerSelection() {

        /*
         * AutoCompleteTextView'u gerçek text input olarak kullanmıyoruz.
         *
         * Tıklama kullanıcı seçim dialog'unu açar.
         */
        binding.autoCompleteProjectOwner
            .setOnClickListener {

                openOwnerSelectionDialog()
            }

        binding.textInputLayoutProjectOwner
            .setEndIconOnClickListener {

                openOwnerSelectionDialog()
            }

        binding.buttonClearProjectOwner
            .setOnClickListener {

                clearSelectedOwner()
            }

        renderSelectedOwner()
    }

    private fun openOwnerSelectionDialog() {

        val state =
            viewModel.uiState.value

        /*
         * Yalnız Admin owner seçebilir.
         */
        if (
            !state.canSelectOwner ||
            state.isBusy
        ) {
            return
        }

        /*
         * Hızlı çift tıklamada ikinci dialog açılmasını engeller.
         */
        if (
            childFragmentManager
                .findFragmentByTag(
                    ProjectOwnerSelectionDialogFragment
                        .DIALOG_TAG
                ) != null
        ) {
            return
        }

        ProjectOwnerSelectionDialogFragment()
            .show(
                childFragmentManager,
                ProjectOwnerSelectionDialogFragment
                    .DIALOG_TAG
            )
    }

    // =========================================================================
    // OWNER RESULT
    // =========================================================================

    private fun configureOwnerResult() {

        childFragmentManager
            .setFragmentResultListener(

                ProjectOwnerSelectionDialogFragment
                    .REQUEST_PROJECT_OWNER_SELECTED,

                viewLifecycleOwner

            ) { _, bundle ->

                val ownerId =
                    bundle.getInt(
                        ProjectOwnerSelectionDialogFragment
                            .RESULT_OWNER_ID,
                        INVALID_OWNER_ID
                    )

                if (
                    ownerId <= 0
                ) {
                    return@setFragmentResultListener
                }

                selectedOwnerId =
                    ownerId

                selectedOwnerFullName =
                    bundle.getString(
                        ProjectOwnerSelectionDialogFragment
                            .RESULT_OWNER_FULL_NAME
                    )

                selectedOwnerEmail =
                    bundle.getString(
                        ProjectOwnerSelectionDialogFragment
                            .RESULT_OWNER_EMAIL
                    )

                binding.textInputLayoutProjectOwner
                    .error =
                    null

                renderSelectedOwner()
            }
    }

    // =========================================================================
    // OWNER RENDER
    // =========================================================================

    private fun renderSelectedOwner() {

        val ownerId =
            selectedOwnerId

        if (
            ownerId == null
        ) {

            binding.autoCompleteProjectOwner
                .setText(
                    "Kendim / Varsayılan proje sahibi",
                    false
                )

            binding.buttonClearProjectOwner
                .isVisible =
                false

            return
        }

        val label =
            buildString {

                val fullName =
                    selectedOwnerFullName
                        ?.takeIf {
                            it.isNotBlank()
                        }

                val email =
                    selectedOwnerEmail
                        ?.takeIf {
                            it.isNotBlank()
                        }

                if (
                    fullName != null
                ) {

                    append(
                        fullName
                    )
                }

                if (
                    email != null
                ) {

                    if (
                        isNotEmpty()
                    ) {
                        append(
                            " - "
                        )
                    }

                    append(
                        email
                    )
                }

                /*
                 * İsim/e-posta herhangi bir nedenle dönmediyse kullanıcıya
                 * boş alan göstermiyoruz.
                 */
                if (
                    isEmpty()
                ) {

                    append(
                        "Seçili kullanıcı"
                    )
                }
            }

        binding.autoCompleteProjectOwner
            .setText(
                label,
                false
            )

        binding.buttonClearProjectOwner
            .isVisible =
            true
    }

    private fun clearSelectedOwner() {

        selectedOwnerId =
            null

        selectedOwnerFullName =
            null

        selectedOwnerEmail =
            null

        binding.textInputLayoutProjectOwner
            .error =
            null

        renderSelectedOwner()
    }

    // =========================================================================
    // LISTENERS
    // =========================================================================

    private fun configureListeners() {

        binding.buttonCreateProjectSubmit
            .setOnClickListener {

                clearInputErrors()

                /*
                 * ViewModel'in mevcut ownerIdText API'sini bozmadık.
                 *
                 * Kullanıcı artık ID yazmıyor.
                 *
                 * selectedOwnerId:
                 *
                 * Int?
                 *
                 * olarak Fragment içerisinde tutuluyor ve yalnız request
                 * öncesinde String'e çevriliyor.
                 */
                viewModel.createProject(

                    name =
                    binding.editTextProjectName
                        .text
                        ?.toString()
                        .orEmpty(),

                    description =
                    binding.editTextProjectDescription
                        .text
                        ?.toString()
                        .orEmpty(),

                    startDate =
                    binding.editTextProjectStartDate
                        .text
                        ?.toString()
                        .orEmpty(),

                    endDate =
                    binding.editTextProjectEndDate
                        .text
                        ?.toString()
                        .orEmpty(),

                    status =
                    selectedStatus,

                    ownerIdText =
                    selectedOwnerId
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // STATE
    // =========================================================================

    private fun observeUiState() {

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

    private fun renderState(
        state: CreateProjectUiState
    ) {

        // ---------------------------------------------------------------------
        // PROGRESS
        // ---------------------------------------------------------------------

        binding.progressIndicatorCreateProject
            .isVisible =
            state.isBusy

        // ---------------------------------------------------------------------
        // CREATE
        // ---------------------------------------------------------------------

        binding.buttonCreateProjectSubmit
            .isEnabled =
            !state.isBusy &&
                    state.canCreateProject

        // ---------------------------------------------------------------------
        // FORM
        // ---------------------------------------------------------------------

        binding.editTextProjectName
            .isEnabled =
            !state.isBusy &&
                    state.canCreateProject

        binding.editTextProjectDescription
            .isEnabled =
            !state.isBusy &&
                    state.canCreateProject

        binding.editTextProjectStartDate
            .isEnabled =
            !state.isBusy &&
                    state.canCreateProject

        binding.editTextProjectEndDate
            .isEnabled =
            !state.isBusy &&
                    state.canCreateProject

        binding.autoCompleteProjectCreateStatus
            .isEnabled =
            !state.isBusy &&
                    state.canCreateProject

        // ---------------------------------------------------------------------
        // OWNER
        // ---------------------------------------------------------------------

        binding.autoCompleteProjectOwner
            .isEnabled =
            !state.isBusy &&
                    state.canSelectOwner

        binding.textInputLayoutProjectOwner
            .isEnabled =
            !state.isBusy &&
                    state.canSelectOwner

        binding.buttonClearProjectOwner
            .isEnabled =
            !state.isBusy &&
                    state.canSelectOwner

        /*
         * ProjectManager owner seçemez.
         */
        binding.textInputLayoutProjectOwner
            .helperText =
            if (
                state.canSelectOwner
            ) {

                "Kullanıcı seçmezseniz proje sahibi siz olursunuz."

            } else {

                "ProjectManager olarak proje sahibi otomatik olarak siz olursunuz."
            }

        /*
         * PM ekranında varsayılan owner text'i daha anlamlı olsun.
         */
        if (
            !state.canSelectOwner
        ) {

            selectedOwnerId =
                null

            selectedOwnerFullName =
                null

            selectedOwnerEmail =
                null

            binding.autoCompleteProjectOwner
                .setText(
                    "Oturum açmış kullanıcı",
                    false
                )

            binding.buttonClearProjectOwner
                .isVisible =
                false

        } else {

            renderSelectedOwner()
        }

        // ---------------------------------------------------------------------
        // FIELD ERRORS
        // ---------------------------------------------------------------------

        binding.textInputLayoutProjectName
            .error =
            state.nameError

        binding.textInputLayoutProjectDescription
            .error =
            state.descriptionError

        binding.textInputLayoutProjectStartDate
            .error =
            state.startDateError

        binding.textInputLayoutProjectEndDate
            .error =
            state.endDateError

        /*
         * ViewModel'deki mevcut ownerId validation'ını da
         * kaybetmiyoruz.
         */
        binding.textInputLayoutProjectOwner
            .error =
            state.ownerIdError

        // ---------------------------------------------------------------------
        // GENERAL ERROR
        // ---------------------------------------------------------------------

        binding.textViewCreateProjectError
            .isVisible =
            !state.generalError
                .isNullOrBlank()

        binding.textViewCreateProjectError
            .text =
            state.generalError
                .orEmpty()
    }

    // =========================================================================
    // EVENTS
    // =========================================================================

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

                                    is CreateProjectUiEvent.ProjectCreated -> {

                                        findNavController()
                                            .previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set(
                                                RESULT_PROJECT_CREATED,
                                                true
                                            )

                                        findNavController()
                                            .previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set(
                                                RESULT_CREATED_PROJECT_ID,
                                                event.projectId
                                            )

                                        Snackbar.make(
                                            binding.root,
                                            event.message,
                                            Snackbar.LENGTH_SHORT
                                        ).show()

                                        findNavController()
                                            .navigateUp()
                                    }
                                }
                            }
                    }
            }
    }

    // =========================================================================
    // CLEAR ERRORS
    // =========================================================================

    private fun clearInputErrors() {

        binding.textInputLayoutProjectName
            .error =
            null

        binding.textInputLayoutProjectDescription
            .error =
            null

        binding.textInputLayoutProjectStartDate
            .error =
            null

        binding.textInputLayoutProjectEndDate
            .error =
            null

        binding.textInputLayoutProjectOwner
            .error =
            null

        viewModel.clearGeneralError()
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
    // CONSTANTS
    // =========================================================================

    companion object {

        const val RESULT_PROJECT_CREATED =
            "result_project_created"

        const val RESULT_CREATED_PROJECT_ID =
            "result_created_project_id"

        private const val INVALID_OWNER_ID =
            -1
    }
}