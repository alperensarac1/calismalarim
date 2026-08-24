package com.alperensarac.projectmanagementkotlin.feature.projects.detail.member

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.DialogAddProjectMemberBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Projeye yeni kullanıcı ekleme ekranıdır.
 *
 * Kullanıcı:
 *
 * 1. Kullanıcı arar.
 * 2. Listeden kullanıcı seçer.
 * 3. ProjectMemberRole seçer.
 * 4. Kaydet'e basar.
 */
@AndroidEntryPoint
class AddProjectMemberDialogFragment :
    DialogFragment() {

    private var _binding:
            DialogAddProjectMemberBinding? =
        null

    private val binding:
            DialogAddProjectMemberBinding
        get() =
            checkNotNull(_binding)

    private val viewModel:
            AddProjectMemberViewModel
            by viewModels()

    private lateinit var userAdapter:
            UserSelectionPagingAdapter

    private var latestUiState =
        AddProjectMemberUiState()

    private val projectId: Int
        get() =
            requireArguments().getInt(
                ARG_PROJECT_ID,
                INVALID_PROJECT_ID
            )

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        _binding =
            DialogAddProjectMemberBinding.inflate(
                LayoutInflater.from(
                    requireContext()
                )
            )

        configureUserList()

        configureSearch()

        configureRoleDropdown()

        configureButtons()

        observeUiState()

        observeUsers()

        observeLoadStates()

        observeEvents()

        return AlertDialog.Builder(
            requireContext()
        )
            .setView(
                binding.root
            )
            .create()
    }

    // -------------------------------------------------------------------------
    // USER LIST
    // -------------------------------------------------------------------------

    private fun configureUserList() {

        userAdapter =
            UserSelectionPagingAdapter(

                selectedUserId = {
                    latestUiState
                        .selectedUser
                        ?.id
                },

                onUserClicked = { user ->

                    viewModel.selectUser(
                        user
                    )
                }
            )

        binding.recyclerViewUsers.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                userAdapter
        }
    }

    // -------------------------------------------------------------------------
    // SEARCH
    // -------------------------------------------------------------------------

    private fun configureSearch() {

        binding.editTextUserSearch
            .doAfterTextChanged { editable ->

                viewModel.onSearchChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // -------------------------------------------------------------------------
    // ROLE
    // -------------------------------------------------------------------------

    private fun configureRoleDropdown() {

        val roleLabels =
            listOf(
                getString(
                    R.string.project_member_role_member
                ),
                getString(
                    R.string.project_member_role_contributor
                ),
                getString(
                    R.string.project_member_role_viewer
                )
            )

        val adapter =
            android.widget.ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                roleLabels
            )

        binding.autoCompleteMemberRole
            .setAdapter(
                adapter
            )

        binding.autoCompleteMemberRole
            .setText(
                roleLabels.first(),
                false
            )

        binding.autoCompleteMemberRole
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val role =
                    when (position) {

                        0 ->
                            ProjectMemberRole.MEMBER

                        1 ->
                            ProjectMemberRole.CONTRIBUTOR

                        else ->
                            ProjectMemberRole.VIEWER
                    }

                viewModel.selectRole(
                    role
                )
            }
    }

    // -------------------------------------------------------------------------
    // BUTTONS
    // -------------------------------------------------------------------------

    private fun configureButtons() {

        binding.buttonCancelAddMember
            .setOnClickListener {

                dismiss()
            }

        binding.buttonSaveAddMember
            .setOnClickListener {

                viewModel.addMember(
                    projectId = projectId
                )
            }
    }

    // -------------------------------------------------------------------------
    // STATE
    // -------------------------------------------------------------------------

    private fun observeUiState() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.uiState.collect { state ->

                    latestUiState =
                        state

                    binding.buttonSaveAddMember.isEnabled =
                        state.canSave

                    binding.progressAddMember.isVisible =
                        state.isSaving

                    binding.textViewSelectedUser.isVisible =
                        state.selectedUser != null

                    binding.textViewSelectedUser.text =
                        state.selectedUser
                            ?.let { user ->
                                getString(
                                    R.string.add_member_selected_user_format,
                                    user.fullName
                                )
                            }
                            .orEmpty()

                    userAdapter.refreshSelection()
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // PAGING
    // -------------------------------------------------------------------------

    private fun observeUsers() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.users.collectLatest { pagingData ->

                    userAdapter.submitData(
                        pagingData
                    )
                }
            }
        }
    }

    private fun observeLoadStates() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                userAdapter.loadStateFlow
                    .collectLatest { states ->

                        val refresh =
                            states.refresh

                        binding.progressUsers.isVisible =
                            refresh is
                                    LoadState.Loading

                        binding.textViewUsersEmpty.isVisible =
                            refresh is
                                    LoadState.NotLoading &&
                                    userAdapter.itemCount == 0

                        binding.textViewUsersError.isVisible =
                            refresh is
                                    LoadState.Error

                        if (
                            refresh is
                                    LoadState.Error
                        ) {

                            binding.textViewUsersError.text =
                                refresh.error.message
                                    ?: getString(
                                        R.string.add_member_users_error
                                    )
                        }
                    }
            }
        }
    }

    // -------------------------------------------------------------------------
    // EVENTS
    // -------------------------------------------------------------------------

    private fun observeEvents() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.events.collect { event ->

                    when (event) {

                        is AddProjectMemberUiEvent.ShowMessage -> {

                            Snackbar.make(
                                binding.root,
                                event.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }

                        is AddProjectMemberUiEvent.MemberAdded -> {

                            /*
                             * Parent ProjectDetailFragment'e sonuç
                             * gönderiyoruz.
                             */
                            parentFragmentManager
                                .setFragmentResult(
                                    REQUEST_MEMBER_ADDED,
                                    Bundle.EMPTY
                                )

                            dismiss()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {

        if (
            ::userAdapter.isInitialized
        ) {
            binding.recyclerViewUsers.adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }

    companion object {

        const val REQUEST_MEMBER_ADDED =
            "project_member_added"

        private const val ARG_PROJECT_ID =
            "projectId"

        private const val INVALID_PROJECT_ID =
            -1

        fun newInstance(
            projectId: Int
        ): AddProjectMemberDialogFragment {

            return AddProjectMemberDialogFragment()
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
    }
}