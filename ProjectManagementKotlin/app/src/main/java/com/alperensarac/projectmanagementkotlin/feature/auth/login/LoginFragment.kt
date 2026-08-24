package com.alperensarac.projectmanagementkotlin.feature.auth.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Kullanıcı giriş ekranıdır.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding: FragmentLoginBinding
        get() = checkNotNull(_binding) {
            "FragmentLoginBinding yalnızca view yaşam döngüsünde kullanılabilir."
        }

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(
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
        super.onViewCreated(view, savedInstanceState)

        configureInputListeners()
        configureClickListeners()
        observeUiState()
        observeUiEvents()
    }

    private fun configureInputListeners() {
        binding.editTextEmail.doAfterTextChanged { editable ->
            viewModel.onEmailChanged(
                editable?.toString().orEmpty()
            )
        }

        binding.editTextPassword.doAfterTextChanged { editable ->
            viewModel.onPasswordChanged(
                editable?.toString().orEmpty()
            )
        }

        binding.editTextPassword.setOnEditorActionListener { _, _, _ ->
            viewModel.login()
            true
        }

        /*
         * XML içerisinde geliştirme test hesabı yazılıysa başlangıç
         * değerlerini ViewModel'e aktarır.
         */
        viewModel.onEmailChanged(
            binding.editTextEmail.text?.toString().orEmpty()
        )

        viewModel.onPasswordChanged(
            binding.editTextPassword.text?.toString().orEmpty()
        )
    }

    private fun configureClickListeners() {
        binding.buttonLogin.setOnClickListener {
            viewModel.login()
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.uiState.collect { state ->
                    renderUiState(state)
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
                    handleUiEvent(event)
                }
            }
        }
    }

    private fun renderUiState(
        state: LoginUiState
    ) {
        binding.textInputLayoutEmail.error =
            state.emailError

        binding.textInputLayoutPassword.error =
            state.passwordError

        binding.textViewGeneralError.text =
            state.generalError

        binding.textViewGeneralError.isVisible =
            !state.generalError.isNullOrBlank()

        binding.progressIndicatorLogin.isVisible =
            state.isLoading

        binding.buttonLogin.isEnabled =
            !state.isLoading

        binding.editTextEmail.isEnabled =
            !state.isLoading

        binding.editTextPassword.isEnabled =
            !state.isLoading

        binding.buttonLogin.text = if (state.isLoading) {
            getString(R.string.login_loading)
        } else {
            getString(R.string.login_button)
        }
    }

    private fun handleUiEvent(
        event: LoginUiEvent
    ) {
        when (event) {
            LoginUiEvent.NavigateToHome -> {
                val currentDestination =
                    findNavController().currentDestination?.id

                if (currentDestination == R.id.loginFragment) {
                    findNavController().navigate(
                        R.id.action_loginFragment_to_homeFragment
                    )
                }
            }

            is LoginUiEvent.ShowMessage -> {
                Snackbar.make(
                    binding.root,
                    event.message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}