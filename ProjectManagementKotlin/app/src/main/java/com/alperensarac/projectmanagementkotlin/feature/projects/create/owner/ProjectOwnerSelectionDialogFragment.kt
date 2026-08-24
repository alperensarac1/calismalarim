package com.alperensarac.projectmanagementkotlin.feature.projects.create.owner

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
import com.alperensarac.projectmanagementkotlin.databinding.DialogSelectProjectOwnerBinding
import com.alperensarac.projectmanagementkotlin.feature.projects.detail.member.UserSelectionPagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Proje sahibi seçim dialog'udur.
 *
 * Kullanıcı:
 *
 * 1. Kullanıcı arar.
 * 2. Paging listesinden kullanıcı seçer.
 * 3. "Seç" butonuna basar.
 * 4. Seçim CreateProjectFragment'e FragmentResult ile döner.
 */
@AndroidEntryPoint
class ProjectOwnerSelectionDialogFragment :
    DialogFragment() {

    // =========================================================================
    // BINDING
    // =========================================================================

    private var _binding:
            DialogSelectProjectOwnerBinding? =
        null

    private val binding:
            DialogSelectProjectOwnerBinding
        get() =
            checkNotNull(
                _binding
            )

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel:
            ProjectOwnerSelectionViewModel
            by viewModels()

    // =========================================================================
    // ADAPTER
    // =========================================================================

    private lateinit var userAdapter:
            UserSelectionPagingAdapter

    private var latestState =
        ProjectOwnerSelectionUiState()

    // =========================================================================
    // DIALOG
    // =========================================================================

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {

        _binding =
            DialogSelectProjectOwnerBinding.inflate(
                LayoutInflater.from(
                    requireContext()
                )
            )

        configureUserList()

        configureSearch()

        configureButtons()

        observeState()

        observeUsers()

        observeLoadStates()

        return AlertDialog.Builder(
            requireContext()
        )
            .setView(
                binding.root
            )
            .create()
    }

    // =========================================================================
    // USER LIST
    // =========================================================================

    private fun configureUserList() {

        userAdapter =
            UserSelectionPagingAdapter(

                selectedUserId = {

                    latestState
                        .selectedUser
                        ?.id
                },

                onUserClicked = { user ->

                    viewModel.selectUser(
                        user
                    )
                }
            )

        binding.recyclerViewProjectOwnerUsers
            .apply {

                layoutManager =
                    LinearLayoutManager(
                        requireContext()
                    )

                adapter =
                    userAdapter
            }
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    private fun configureSearch() {

        binding.editTextProjectOwnerSearch
            .doAfterTextChanged { editable ->

                viewModel.onSearchChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // BUTTONS
    // =========================================================================

    private fun configureButtons() {

        binding.buttonCancelProjectOwner
            .setOnClickListener {

                dismiss()
            }

        binding.buttonSelectProjectOwner
            .setOnClickListener {

                val user =
                    latestState.selectedUser
                        ?: return@setOnClickListener

                /*
                 * Burada User objesinin tamamını Bundle'a koymuyoruz.
                 *
                 * Parcelable/Serializable bağımlılığı oluşturmamak için
                 * ihtiyacımız olan primitive/string alanları gönderiyoruz.
                 */
                parentFragmentManager
                    .setFragmentResult(

                        REQUEST_PROJECT_OWNER_SELECTED,

                        Bundle().apply {

                            putInt(
                                RESULT_OWNER_ID,
                                user.id
                            )

                            putString(
                                RESULT_OWNER_FULL_NAME,
                                user.fullName
                            )

                            putString(
                                RESULT_OWNER_EMAIL,
                                user.email
                            )
                        }
                    )

                dismiss()
            }
    }

    // =========================================================================
    // STATE
    // =========================================================================

    private fun observeState() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.uiState
                    .collect { state ->

                        latestState =
                            state

                        binding.buttonSelectProjectOwner
                            .isEnabled =
                            state.canSelect

                        binding.textViewSelectedProjectOwner
                            .isVisible =
                            state.selectedUser != null

                        binding.textViewSelectedProjectOwner
                            .text =
                            state.selectedUser
                                ?.let { user ->

                                    buildString {

                                        append(
                                            user.fullName
                                        )

                                        if (
                                            user.email.isNotBlank()
                                        ) {

                                            append(
                                                "\n"
                                            )

                                            append(
                                                user.email
                                            )
                                        }
                                    }
                                }
                                .orEmpty()

                        /*
                         * Adapter satırlarındaki selected görünümünü yeniler.
                         */
                        if (
                            ::userAdapter.isInitialized
                        ) {

                            userAdapter.refreshSelection()
                        }
                    }
            }
        }
    }

    // =========================================================================
    // USERS
    // =========================================================================

    private fun observeUsers() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.users
                    .collectLatest { pagingData ->

                        userAdapter.submitData(
                            pagingData
                        )
                    }
            }
        }
    }

    // =========================================================================
    // LOAD STATES
    // =========================================================================

    private fun observeLoadStates() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                userAdapter
                    .loadStateFlow
                    .collectLatest { states ->

                        val refresh =
                            states.refresh

                        binding.progressProjectOwnerUsers
                            .isVisible =
                            refresh is LoadState.Loading

                        binding.textViewProjectOwnerEmpty
                            .isVisible =
                            refresh is LoadState.NotLoading &&
                                    userAdapter.itemCount == 0

                        binding.textViewProjectOwnerError
                            .isVisible =
                            refresh is LoadState.Error

                        if (
                            refresh is LoadState.Error
                        ) {

                            binding.textViewProjectOwnerError
                                .text =
                                refresh.error.message
                                    ?: "Kullanıcılar yüklenemedi."
                        }
                    }
            }
        }
    }

    // =========================================================================
    // DESTROY
    // =========================================================================

    override fun onDestroyView() {

        if (
            ::userAdapter.isInitialized
        ) {

            binding.recyclerViewProjectOwnerUsers
                .adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }

    // =========================================================================
    // CONSTANTS
    // =========================================================================

    companion object {

        const val REQUEST_PROJECT_OWNER_SELECTED =
            "project_owner_selected"

        const val RESULT_OWNER_ID =
            "project_owner_id"

        const val RESULT_OWNER_FULL_NAME =
            "project_owner_full_name"

        const val RESULT_OWNER_EMAIL =
            "project_owner_email"

        const val DIALOG_TAG =
            "ProjectOwnerSelectionDialog"
    }
}