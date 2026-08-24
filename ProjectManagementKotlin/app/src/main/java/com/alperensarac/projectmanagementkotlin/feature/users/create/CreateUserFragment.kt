package com.alperensarac.projectmanagementkotlin.feature.users.create

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
import com.alperensarac.projectmanagementkotlin.databinding.FragmentCreateUserBinding
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Admin tarafından yeni kullanıcı oluşturulan ekrandır.
 */
@AndroidEntryPoint
class CreateUserFragment :
    Fragment() {

    private var _binding:
            FragmentCreateUserBinding? =
        null

    private val binding:
            FragmentCreateUserBinding
        get() =
            checkNotNull(_binding)

    private val viewModel:
            CreateUserViewModel
            by viewModels()

    private var selectedRole:
            UserRole =
        UserRole.TEAM_MEMBER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentCreateUserBinding.inflate(
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

        configureRoleDropdown()

        configureListeners()

        observeState()

        observeEvents()
    }

    // =========================================================================
    // ROLE
    // =========================================================================

    private fun configureRoleDropdown() {

        val roles =
            UserRole.entries

        val labels =
            roles.map {
                it.displayName
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteRole
            .setAdapter(
                adapter
            )

        selectedRole =
            UserRole.TEAM_MEMBER

        binding.autoCompleteRole
            .setText(
                selectedRole.displayName,
                false
            )

        binding.autoCompleteRole
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                selectedRole =
                    roles[position]
            }
    }

    // =========================================================================
    // LISTENERS
    // =========================================================================

    private fun configureListeners() {

        binding.buttonCreateUser
            .setOnClickListener {

                viewModel.createUser(
                    firstName =
                    binding.editTextFirstName
                        .text
                        ?.toString()
                        .orEmpty(),

                    lastName =
                    binding.editTextLastName
                        .text
                        ?.toString()
                        .orEmpty(),

                    email =
                    binding.editTextEmail
                        .text
                        ?.toString()
                        .orEmpty(),

                    password =
                    binding.editTextPassword
                        .text
                        ?.toString()
                        .orEmpty(),

                    role =
                    selectedRole,

                    department =
                    binding.editTextDepartment
                        .text
                        ?.toString()
                        .orEmpty(),

                    isActive =
                    binding.switchUserActive
                        .isChecked
                )
            }
    }

    // =========================================================================
    // STATE
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
                            .collect(
                                ::renderState
                            )
                    }
            }
    }

    private fun renderState(
        state: CreateUserUiState
    ) {

        binding.progressIndicatorCreateUser
            .isVisible =
            state.isSubmitting

        binding.buttonCreateUser
            .isEnabled =
            !state.isSubmitting

        binding.textInputLayoutFirstName
            .error =
            state.firstNameError

        binding.textInputLayoutLastName
            .error =
            state.lastNameError

        binding.textInputLayoutEmail
            .error =
            state.emailError

        binding.textInputLayoutPassword
            .error =
            state.passwordError

        binding.textInputLayoutDepartment
            .error =
            state.departmentError

        binding.textViewCreateUserError
            .isVisible =
            !state.generalError
                .isNullOrBlank()

        binding.textViewCreateUserError
            .text =
            state.generalError
                .orEmpty()

        val enabled =
            !state.isSubmitting

        binding.editTextFirstName.isEnabled =
            enabled

        binding.editTextLastName.isEnabled =
            enabled

        binding.editTextEmail.isEnabled =
            enabled

        binding.editTextPassword.isEnabled =
            enabled

        binding.autoCompleteRole.isEnabled =
            enabled

        binding.editTextDepartment.isEnabled =
            enabled

        binding.switchUserActive.isEnabled =
            enabled
    }

    // =========================================================================
    // EVENT
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

                                when (event) {

                                    is CreateUserUiEvent.UserCreated -> {

                                        findNavController()
                                            .previousBackStackEntry
                                            ?.savedStateHandle
                                            ?.set(
                                                RESULT_USER_CREATED,
                                                true
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
    companion object{
        const val RESULT_USER_CREATED =
            "result_user_created"
    }

    override fun onDestroyView() {

        _binding =
            null

        super.onDestroyView()
    }
}