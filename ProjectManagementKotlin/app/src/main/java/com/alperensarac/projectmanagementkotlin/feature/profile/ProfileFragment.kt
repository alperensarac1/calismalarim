package com.alperensarac.projectmanagementkotlin.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.theme.AppThemeMode
import com.alperensarac.projectmanagementkotlin.databinding.FragmentProfileBinding
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.feature.theme.ThemeModeDialogFragment
import com.alperensarac.projectmanagementkotlin.feature.theme.ThemeViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Kullanıcının:
 *
 * - profil bilgilerini
 * - uygulama görünüm tercihini
 * - oturum işlemlerini
 *
 * yönettiği ekrandır.
 */
@AndroidEntryPoint
class ProfileFragment : Fragment() {

    // =========================================================================
    // VIEW BINDING
    // =========================================================================

    private var _binding:
            FragmentProfileBinding? =
        null

    private val binding:
            FragmentProfileBinding
        get() =
            checkNotNull(_binding) {
                "FragmentProfileBinding yalnızca view yaşam döngüsü içerisinde kullanılabilir."
            }

    // =========================================================================
    // VIEW MODELS
    // =========================================================================

    /**
     * Profil işlemleri Fragment'a aittir.
     */
    private val viewModel:
            ProfileViewModel
            by viewModels()

    /**
     * Tema uygulamanın tamamına ait bir state olduğu için
     * Activity scope kullanıyoruz.
     *
     * MainActivity de aynı ThemeViewModel instance'ını kullanır.
     */
    private val themeViewModel:
            ThemeViewModel
            by activityViewModels()

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentProfileBinding.inflate(
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

        configureClickListeners()

        observeUiState()

        observeUiEvents()

        observeTheme()
    }

    // =========================================================================
    // CLICK LISTENERS
    // =========================================================================

    private fun configureClickListeners() {

        // ---------------------------------------------------------------------
        // RETRY
        // ---------------------------------------------------------------------

        binding.buttonRetryProfile
            .setOnClickListener {

                viewModel.loadProfile()
            }

        // ---------------------------------------------------------------------
        // THEME
        // ---------------------------------------------------------------------

        binding.cardThemeSetting
            .setOnClickListener {

                showThemeDialog()
            }

        // ---------------------------------------------------------------------
        // LOGOUT
        // ---------------------------------------------------------------------

        binding.buttonLogout
            .setOnClickListener {

                showLogoutConfirmationDialog()
            }
        binding.cardUserManagement
            .setOnClickListener {

                if (
                    findNavController()
                        .currentDestination
                        ?.id !=
                    R.id.profileFragment
                ) {
                    return@setOnClickListener
                }

                findNavController()
                    .navigate(
                        R.id.action_profileFragment_to_usersFragment
                    )
            }
    }

    // =========================================================================
    // THEME
    // =========================================================================

    /**
     * Aydınlık / Karanlık tema dialog'unu açar.
     */
    private fun showThemeDialog() {

        /*
         * Aynı dialog hızlı tıklama nedeniyle iki kere açılmasın.
         */
        if (
            childFragmentManager
                .findFragmentByTag(
                    ThemeModeDialogFragment.TAG
                ) != null
        ) {
            return
        }

        ThemeModeDialogFragment()
            .show(
                childFragmentManager,
                ThemeModeDialogFragment.TAG
            )
    }

    /**
     * Mevcut tema tercihini Profil ekranında gösterir.
     *
     * Örneğin:
     *
     * Tema
     * Uygulamanın görünümünü değiştir       Karanlık
     */
    private fun observeTheme() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        themeViewModel
                            .themeMode
                            .collect { mode ->

                                renderTheme(
                                    mode
                                )
                            }
                    }
            }
    }

    private fun renderTheme(
        mode: AppThemeMode
    ) {

        binding.textViewThemeCurrent.text =
            when (mode) {

                AppThemeMode.LIGHT ->
                    getString(
                        R.string.theme_light
                    )

                AppThemeMode.DARK ->
                    getString(
                        R.string.theme_dark
                    )
            }

        /*
         * Basit ama anlaşılır görsel gösterge.
         */
        binding.textViewThemeIcon.text =
            when (mode) {

                AppThemeMode.LIGHT ->
                    "☀"

                AppThemeMode.DARK ->
                    "☾"
            }
    }

    // =========================================================================
    // LOGOUT
    // =========================================================================

    /**
     * Kullanıcının yanlışlıkla çıkış yapmasını önlemek için
     * onay diyaloğu gösterilir.
     */
    private fun showLogoutConfirmationDialog() {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                R.string.logout_dialog_title
            )
            .setMessage(
                R.string.logout_dialog_message
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.action_logout
            ) { _, _ ->

                viewModel.logout()
            }
            .show()
    }

    // =========================================================================
    // PROFILE STATE
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

                                renderUiState(
                                    state
                                )
                            }
                    }
            }
    }

    // =========================================================================
    // EVENTS
    // =========================================================================

    private fun observeUiEvents() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.events
                            .collect { event ->

                                handleUiEvent(
                                    event
                                )
                            }
                    }
            }
    }

    // =========================================================================
    // RENDER PROFILE
    // =========================================================================

    private fun renderUiState(
        state: ProfileUiState
    ) {

        // ---------------------------------------------------------------------
        // INITIAL LOADING
        // ---------------------------------------------------------------------

        binding.progressIndicatorProfile
            .isVisible =
            state.isLoading &&
                    state.user == null

        // ---------------------------------------------------------------------
        // PROFILE
        // ---------------------------------------------------------------------

        binding.cardProfileInformation
            .isVisible =
            state.user != null

        /*
         * Tema kullanıcının profile API'sinden bağımsız bir uygulama
         * ayarıdır.
         *
         * Kullanıcı bilgileri başarıyla geldiyse profil kartıyla
         * birlikte gösteriyoruz.
         */
        binding.cardThemeSetting
            .isVisible =
            state.user != null

        // ---------------------------------------------------------------------
        // ERROR
        // ---------------------------------------------------------------------

        binding.layoutProfileError
            .isVisible =
            !state.errorMessage
                .isNullOrBlank() &&
                    state.user == null

        binding.textViewProfileError.text =
            state.errorMessage
                .orEmpty()

        // ---------------------------------------------------------------------
        // USER
        // ---------------------------------------------------------------------

        state.user
            ?.let(
                ::renderUser
            )

        // ---------------------------------------------------------------------
        // LOGOUT
        // ---------------------------------------------------------------------

        binding.progressIndicatorLogout
            .isVisible =
            state.isLoggingOut

        binding.buttonLogout
            .isEnabled =
            !state.isLoggingOut

        binding.cardThemeSetting
            .isEnabled =
            !state.isLoggingOut

        binding.buttonRetryProfile
            .isEnabled =
            !state.isLoading

        binding.buttonLogout.text =
            if (
                state.isLoggingOut
            ) {

                getString(
                    R.string.logout_loading
                )

            } else {

                getString(
                    R.string.action_logout
                )
            }
    }

    // =========================================================================
    // USER
    // =========================================================================

    private fun renderUser(
        user: AuthUser
    ) {

        binding.textViewProfileFullName.text =
            user.fullName

        binding.textViewProfileEmailValue.text =
            user.email

        binding.textViewProfileRoleValue.text =
            user.role

        binding.textViewProfileDepartmentValue.text =
            user.department
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: getString(
                    R.string.profile_department_not_defined
                )

        binding.textViewProfileStatusValue.text =
            if (
                user.isActive
            ) {

                getString(
                    R.string.profile_status_active
                )

            } else {

                getString(
                    R.string.profile_status_passive
                )
            }

        /*
         * İsim boşsa e-posta adresinin ilk karakterini kullan.
         */
        binding.textViewProfileAvatar.text =
            user.fullName
                .trim()
                .firstOrNull()
                ?.uppercaseChar()
                ?.toString()
                ?: user.email
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                        ?: "?"
        /*
 * Kullanıcı yönetimi yalnızca Admin'e gösterilir.
 *
 * Backend UsersController da zaten Admin rolü gerektirir.
 */
        binding.cardUserManagement.isVisible =
            user.role.equals(
                "Admin",
                ignoreCase = true
            )
    }

    // =========================================================================
    // EVENT
    // =========================================================================

    private fun handleUiEvent(
        event: ProfileUiEvent
    ) {

        when (event) {

            is ProfileUiEvent.ShowMessage -> {

                Snackbar.make(
                    binding.root,
                    event.message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    // =========================================================================
    // DESTROY VIEW
    // =========================================================================

    override fun onDestroyView() {

        _binding =
            null

        super.onDestroyView()
    }
}