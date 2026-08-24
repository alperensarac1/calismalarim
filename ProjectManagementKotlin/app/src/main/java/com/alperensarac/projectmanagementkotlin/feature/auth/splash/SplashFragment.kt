package com.alperensarac.projectmanagementkotlin.feature.auth.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.FragmentSplashBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Uygulamanın açılış ve oturum kontrol ekranıdır.
 *
 * Fragment yalnızca UI gösterimi ve navigation işlemlerini yönetir.
 * Oturum kontrolü SplashViewModel içerisinde gerçekleştirilir.
 */
@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null

    private val binding: FragmentSplashBinding
        get() = checkNotNull(_binding) {
            "FragmentSplashBinding yalnızca view yaşam döngüsü içerisinde kullanılabilir."
        }

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(
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

        observeUiState()
        observeUiEvents()
    }

    /**
     * Splash ekranının kalıcı durumunu lifecycle güvenli biçimde izler.
     */
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

    /**
     * Navigation gibi tek seferlik olayları izler.
     */
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
        state: SplashUiState
    ) {
        binding.progressIndicatorSplash.isVisible =
            state.isCheckingSession

        binding.textViewSplashStatus.text =
            state.statusMessage
    }

    private fun handleUiEvent(
        event: SplashUiEvent
    ) {
        when (event) {
            SplashUiEvent.NavigateToHome -> {
                navigateToHome()
            }

            is SplashUiEvent.NavigateToLogin -> {
                /*
                 * Önce Login ekranına geçilir.
                 *
                 * Snackbar'ın Splash view üzerinde gösterilip navigation
                 * sırasında kaybolmaması için mesaj MainActivity root view
                 * üzerinde gösterilir.
                 */
                navigateToLogin()

                event.message
                    ?.takeIf { it.isNotBlank() }
                    ?.let { message ->
                        Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            message,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }

    private fun navigateToHome() {
        val navController = findNavController()

        if (navController.currentDestination?.id != R.id.splashFragment) {
            return
        }

        navController.navigate(
            R.id.action_splashFragment_to_homeFragment
        )
    }

    private fun navigateToLogin() {
        val navController = findNavController()

        if (navController.currentDestination?.id != R.id.splashFragment) {
            return
        }

        navController.navigate(
            R.id.action_splashFragment_to_loginFragment
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}