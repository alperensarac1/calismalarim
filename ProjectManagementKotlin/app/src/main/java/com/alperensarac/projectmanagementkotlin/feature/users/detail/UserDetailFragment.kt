package com.alperensarac.projectmanagementkotlin.feature.users.detail

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
import com.alperensarac.projectmanagementkotlin.databinding.DialogResetUserPasswordBinding
import com.alperensarac.projectmanagementkotlin.databinding.FragmentUserDetailBinding
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Admin kullanıcı detay/yönetim ekranıdır.
 *
 * Bu Fragment'ın görevi:
 *
 * - UI event'lerini ViewModel'e iletmek
 * - ViewModel state'ini ekrana çizmek
 * - Navigation ve dialog gibi Android UI işlemlerini yapmak
 *
 * Form verisinin asıl sahibi artık Fragment DEĞİL,
 * UserDetailViewModel'dir.
 */
@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    // =========================================================================
    // VIEW BINDING
    // =========================================================================

    private var _binding: FragmentUserDetailBinding? = null

    private val binding: FragmentUserDetailBinding
        get() = checkNotNull(_binding)

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel: UserDetailViewModel by viewModels()

    // =========================================================================
    // ARGUMENT
    // =========================================================================

    private var userId: Int = INVALID_USER_ID

    /**
     * State içerisinden switch'in değerini programatik olarak değiştirirken
     * setOnCheckedChangeListener tetiklenmesini kontrol etmek için kullanılır.
     */
    private var isRenderingStatus = false

    /**
     * Kullanıcının gerçekten geri çıkmak istediği durumda
     * callback'in kendisini tekrar yakalamasını engellemek için kullanılır.
     */
    private var allowNavigationBack =
        false

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        userId =
            requireArguments().getInt(
                ARG_USER_ID,
                INVALID_USER_ID
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentUserDetailBinding.inflate(
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

        configureFormListeners()

        configureActionListeners()

        configureBackNavigation()

        configureToolbar()

        observeState()

        observeEvents()

        /*
         * Rotation gibi normal configuration change durumlarında ViewModel
         * yaşamaya devam edeceği için kullanıcıyı tekrar yüklemiyoruz.
         */
        if (savedInstanceState == null) {

            viewModel.loadUser(
                userId = userId
            )
        }
    }

    // =========================================================================
    // ROLE DROPDOWN
    // =========================================================================

    private fun configureRoleDropdown() {

        val roles =
            UserRole.entries

        val labels =
            roles.map { role ->
                role.displayName
            }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteUserDetailRole
            .setAdapter(
                adapter
            )

        binding.autoCompleteUserDetailRole
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                /*
                 * Artık selectedRole değişkenini Fragment'ta tutmuyoruz.
                 *
                 * Rol seçimini doğrudan ViewModel state'ine gönderiyoruz.
                 */
                viewModel.onRoleChanged(
                    roles[position]
                )
            }
    }

    // =========================================================================
    // FORM LISTENERS
    // =========================================================================

    /**
     * Formdaki her değişikliği ViewModel'e aktarır.
     *
     * Böylece ViewModel:
     *
     * - form değişti mi?
     * - Kaydet aktif olmalı mı?
     * - ilgili validation hatası temizlenmeli mi?
     *
     * kararlarını kendi verebilir.
     */
    private fun configureFormListeners() {

        // ---------------------------------------------------------------------
        // FIRST NAME
        // ---------------------------------------------------------------------

        binding.editTextUserDetailFirstName
            .doAfterTextChanged { editable ->

                viewModel.onFirstNameChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }

        // ---------------------------------------------------------------------
        // LAST NAME
        // ---------------------------------------------------------------------

        binding.editTextUserDetailLastName
            .doAfterTextChanged { editable ->

                viewModel.onLastNameChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }

        // ---------------------------------------------------------------------
        // EMAIL
        // ---------------------------------------------------------------------

        binding.editTextUserDetailEmail
            .doAfterTextChanged { editable ->

                viewModel.onEmailChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }

        // ---------------------------------------------------------------------
        // DEPARTMENT
        // ---------------------------------------------------------------------

        binding.editTextUserDetailDepartment
            .doAfterTextChanged { editable ->

                viewModel.onDepartmentChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // ACTION LISTENERS
    // =========================================================================

    private fun configureActionListeners() {

        // ---------------------------------------------------------------------
        // SAVE
        // ---------------------------------------------------------------------

        binding.buttonSaveUser
            .setOnClickListener {

                /*
                 * Form alanlarını burada tekrar okumuyoruz.
                 *
                 * ViewModel kendi UserDetailUiState içerisindeki form
                 * değerlerini kullanıyor.
                 */
                viewModel.updateUser(
                    userId = userId
                )
            }

        // ---------------------------------------------------------------------
        // STATUS
        // ---------------------------------------------------------------------

        binding.switchUserDetailActive
            .setOnCheckedChangeListener {
                    _,
                    isChecked ->

                /*
                 * State render sırasında switch değeri değiştirildiyse
                 * API isteği gönderme.
                 */
                if (isRenderingStatus) {
                    return@setOnCheckedChangeListener
                }

                val currentUser =
                    viewModel.uiState
                        .value
                        .user
                        ?: return@setOnCheckedChangeListener

                /*
                 * Backend'deki durum ile zaten aynıysa hiçbir şey yapmayız.
                 */
                if (
                    currentUser.isActive ==
                    isChecked
                ) {
                    return@setOnCheckedChangeListener
                }

                showStatusConfirmation(
                    requestedStatus = isChecked,
                    currentStatus = currentUser.isActive
                )
            }

        // ---------------------------------------------------------------------
        // RESET PASSWORD
        // ---------------------------------------------------------------------

        binding.buttonResetUserPassword
            .setOnClickListener {

                showResetPasswordDialog()
            }

        // ---------------------------------------------------------------------
        // DELETE
        // ---------------------------------------------------------------------

        binding.buttonDeleteUser
            .setOnClickListener {

                showDeleteConfirmation()
            }
    }

    // =========================================================================
    // STATUS CONFIRMATION
    // =========================================================================

    private fun showStatusConfirmation(
        requestedStatus: Boolean,
        currentStatus: Boolean
    ) {

        val message =
            if (requestedStatus) {

                getString(
                    R.string.user_detail_activate_confirmation
                )

            } else {

                getString(
                    R.string.user_detail_deactivate_confirmation
                )
            }

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                R.string.user_detail_status_confirmation_title
            )
            .setMessage(
                message
            )
            .setNegativeButton(
                android.R.string.cancel
            ) { _, _ ->

                /*
                 * Kullanıcı işlemi iptal ederse switch'i backend'deki
                 * mevcut haline geri döndürüyoruz.
                 */
                setSwitchWithoutListener(
                    checked = currentStatus
                )
            }
            .setPositiveButton(
                android.R.string.ok
            ) { _, _ ->

                viewModel.updateStatus(
                    userId = userId,
                    isActive = requestedStatus
                )
            }
            .setOnCancelListener {

                setSwitchWithoutListener(
                    checked = currentStatus
                )
            }
            .show()
    }

    // =========================================================================
    // RESET PASSWORD
    // =========================================================================

    private fun showResetPasswordDialog() {

        val dialogBinding =
            DialogResetUserPasswordBinding.inflate(
                layoutInflater
            )

        val dialog =
            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle(
                    R.string.user_detail_reset_password
                )
                .setView(
                    dialogBinding.root
                )
                .setNegativeButton(
                    android.R.string.cancel,
                    null
                )
                /*
                 * Positive listener'ı create sırasında vermiyoruz.
                 *
                 * Çünkü verirsek Android butona basıldığı anda dialog'u
                 * otomatik kapatır.
                 *
                 * Validation hatasında dialog açık kalmalı.
                 */
                .setPositiveButton(
                    R.string.user_detail_reset_password_confirm,
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog
                .getButton(
                    androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE
                )
                .setOnClickListener {

                    val password =
                        dialogBinding
                            .editTextNewPassword
                            .text
                            ?.toString()
                            .orEmpty()

                    /*
                     * Aynı password validation logic ViewModel içerisinde
                     * kullanılır.
                     */
                    val error =
                        viewModel.validatePassword(
                            password
                        )

                    if (error != null) {

                        dialogBinding
                            .textInputLayoutNewPassword
                            .error =
                            error

                        return@setOnClickListener
                    }

                    dialogBinding
                        .textInputLayoutNewPassword
                        .error =
                        null

                    viewModel.resetPassword(
                        userId = userId,
                        newPassword = password
                    )

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    private fun showDeleteConfirmation() {

        val user =
            viewModel.uiState
                .value
                .user
                ?: return

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                R.string.user_detail_delete_confirmation_title
            )
            .setMessage(
                getString(
                    R.string.user_detail_delete_confirmation_message,
                    user.fullName
                )
            )
            .setNegativeButton(
                android.R.string.cancel,
                null
            )
            .setPositiveButton(
                R.string.user_detail_delete
            ) { _, _ ->

                viewModel.deleteUser(
                    userId = userId
                )
            }
            .show()
    }

    // =========================================================================
    // OBSERVE STATE
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

    // =========================================================================
    // RENDER STATE
    // =========================================================================

    private fun renderState(
        state: UserDetailUiState
    ) {

        // ---------------------------------------------------------------------
        // PROGRESS
        // ---------------------------------------------------------------------

        binding.progressIndicatorUserDetail
            .isVisible =
            state.isBusy

        // ---------------------------------------------------------------------
        // CONTENT
        // ---------------------------------------------------------------------

        binding.layoutUserDetailContent
            .isVisible =
            !state.isLoading &&
                    state.user != null

        // ---------------------------------------------------------------------
        // GENERAL ERROR
        // ---------------------------------------------------------------------

        binding.textViewUserDetailError
            .isVisible =
            !state.errorMessage
                .isNullOrBlank()

        binding.textViewUserDetailError
            .text =
            state.errorMessage
                .orEmpty()

        // ---------------------------------------------------------------------
        // FIELD ERRORS
        // ---------------------------------------------------------------------

        binding.textInputLayoutUserDetailFirstName
            .error =
            state.firstNameError

        binding.textInputLayoutUserDetailLastName
            .error =
            state.lastNameError

        binding.textInputLayoutUserDetailEmail
            .error =
            state.emailError

        binding.textInputLayoutUserDetailDepartment
            .error =
            state.departmentError

        // ---------------------------------------------------------------------
        // FORM VALUES
        // ---------------------------------------------------------------------

        renderFormState(
            state = state
        )

        // ---------------------------------------------------------------------
        // USER-SPECIFIC VALUES
        // ---------------------------------------------------------------------

        state.user?.let { user ->

            renderUserMetadata(
                user = user
            )
        }

        // ---------------------------------------------------------------------
        // ENABLE / DISABLE
        // ---------------------------------------------------------------------

        renderEnabledState(
            state = state
        )
    }

    // =========================================================================
    // RENDER FORM
    // =========================================================================

    /**
     * Formu artık User nesnesinden değil,
     * UserDetailUiState içerisindeki form state'inden çiziyoruz.
     */
    private fun renderFormState(
        state: UserDetailUiState
    ) {

        // ---------------------------------------------------------------------
        // FIRST NAME
        // ---------------------------------------------------------------------

        if (
            binding.editTextUserDetailFirstName
                .text
                ?.toString() !=
            state.firstName
        ) {

            binding.editTextUserDetailFirstName
                .setText(
                    state.firstName
                )
        }

        // ---------------------------------------------------------------------
        // LAST NAME
        // ---------------------------------------------------------------------

        if (
            binding.editTextUserDetailLastName
                .text
                ?.toString() !=
            state.lastName
        ) {

            binding.editTextUserDetailLastName
                .setText(
                    state.lastName
                )
        }

        // ---------------------------------------------------------------------
        // EMAIL
        // ---------------------------------------------------------------------

        if (
            binding.editTextUserDetailEmail
                .text
                ?.toString() !=
            state.email
        ) {

            binding.editTextUserDetailEmail
                .setText(
                    state.email
                )
        }

        // ---------------------------------------------------------------------
        // DEPARTMENT
        // ---------------------------------------------------------------------

        if (
            binding.editTextUserDetailDepartment
                .text
                ?.toString() !=
            state.department
        ) {

            binding.editTextUserDetailDepartment
                .setText(
                    state.department
                )
        }

        // ---------------------------------------------------------------------
        // ROLE
        // ---------------------------------------------------------------------

        if (
            binding.autoCompleteUserDetailRole
                .text
                ?.toString() !=
            state.selectedRole.displayName
        ) {

            binding.autoCompleteUserDetailRole
                .setText(
                    state.selectedRole.displayName,
                    false
                )
        }
    }

    // =========================================================================
    // RENDER USER METADATA
    // =========================================================================

    /**
     * Form verisi olmayan kullanıcı özelliklerini çizer.
     *
     * Örneğin:
     *
     * - mevcut backend e-posta bilgisi
     * - aktif/pasif durumu
     */
    private fun renderUserMetadata(
        user: User
    ) {

        binding.textViewUserDetailEmailInfo
            .text =
            user.email

        setSwitchWithoutListener(
            checked = user.isActive
        )
    }

    // =========================================================================
    // SWITCH
    // =========================================================================

    private fun setSwitchWithoutListener(
        checked: Boolean
    ) {

        /*
         * Programatik değişiklik sırasında listener'ın PATCH request
         * başlatmasını engelliyoruz.
         */
        isRenderingStatus =
            true

        binding.switchUserDetailActive
            .isChecked =
            checked

        isRenderingStatus =
            false
    }

    // =========================================================================
    // ENABLED STATE
    // =========================================================================

    private fun renderEnabledState(
        state: UserDetailUiState
    ) {

        val formEnabled =
            !state.isBusy &&
                    state.user != null

        // -------------------------------------------------------------------------
        // BASIC FORM
        // -------------------------------------------------------------------------

        binding.editTextUserDetailFirstName
            .isEnabled =
            formEnabled

        binding.editTextUserDetailLastName
            .isEnabled =
            formEnabled

        binding.editTextUserDetailEmail
            .isEnabled =
            formEnabled

        /*
         * Backend UpdateAsync kendi rolünü değiştirmeyi yasaklamıyor.
         *
         * Bu nedenle kendi hesabımızda da role dropdown açık kalıyor.
         */
        binding.autoCompleteUserDetailRole
            .isEnabled =
            formEnabled

        binding.editTextUserDetailDepartment
            .isEnabled =
            formEnabled

        // -------------------------------------------------------------------------
        // STATUS
        // -------------------------------------------------------------------------

        /*
         * Backend self deactivate'i yasaklamadığı için kendi hesabımızda
         * switch de aktif olabilir.
         */
        binding.switchUserDetailActive
            .isEnabled =
            state.canChangeStatus

        // -------------------------------------------------------------------------
        // UPDATE
        // -------------------------------------------------------------------------

        binding.buttonSaveUser
            .isEnabled =
            state.canSave

        // -------------------------------------------------------------------------
        // RESET PASSWORD
        // -------------------------------------------------------------------------

        /*
         * Kullanıcı pasifse backend password reset'i reddediyor.
         */
        binding.buttonResetUserPassword
            .isEnabled =
            state.canResetPassword

        // -------------------------------------------------------------------------
        // DELETE
        // -------------------------------------------------------------------------

        /*
         * Sadece kendi hesabımızı silerken disabled.
         */
        binding.buttonDeleteUser
            .isEnabled =
            state.canDelete
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

                                when (event) {

                                    // =================================================
                                    // GENERIC SUCCESS MESSAGE
                                    // =================================================

                                    is UserDetailUiEvent.ShowMessage -> {

                                        /*
                                         * Update / status gibi işlemlerden sonra
                                         * liste ekranına döndüğümüzde yeni backend
                                         * değerlerinin görünmesi için sonucu bırakırız.
                                         */
                                        sendUserChangedResult()

                                        Snackbar.make(
                                            binding.root,
                                            event.message,
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }

                                    // =================================================
                                    // DELETE
                                    // =================================================

                                    is UserDetailUiEvent.UserDeleted -> {

                                        sendUserChangedResult()

                                        Snackbar.make(
                                            binding.root,
                                            event.message,
                                            Snackbar.LENGTH_SHORT
                                        ).show()

                                        /*
                                         * Kullanıcı zaten silindi.
                                         *
                                         * Form dirty olsa bile çıkış confirmation göstermemeliyiz.
                                         */
                                        allowNavigationBack =
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
    // TOOLBAR
    // =========================================================================

    /**
     * Kullanıcı detay ekranının toolbar geri butonunu yönetir.
     *
     * Bu ekran UsersFragment üzerinden açıldığı için navigateUp()
     * mevcut back stack üzerinde Kullanıcı Yönetimi ekranına döner.
     */
    private fun configureToolbar() {

        binding.toolbarUserDetail
            .setNavigationOnClickListener {

                findNavController()
                    .navigateUp()
            }
    }

    // =========================================================================
    // RESULT
    // =========================================================================

    private fun sendUserChangedResult() {

        findNavController()
            .previousBackStackEntry
            ?.savedStateHandle
            ?.set(
                RESULT_USER_CHANGED,
                true
            )
    }

    // =========================================================================
// BACK NAVIGATION
// =========================================================================

    /**
     * Kullanıcının formda kaydedilmemiş değişikliği varsa
     * geri çıkmadan önce onay ister.
     *
     * Bu callback yalnızca Fragment view lifecycle'ı boyunca aktiftir.
     */
    private fun configureBackNavigation() {

        val callback =
            object : OnBackPressedCallback(
                true
            ) {

                override fun handleOnBackPressed() {

                    /*
                     * Biz programatik olarak geri dönüyorsak
                     * tekrar confirmation göstermemeliyiz.
                     */
                    if (
                        allowNavigationBack
                    ) {

                        isEnabled =
                            false

                        requireActivity()
                            .onBackPressedDispatcher
                            .onBackPressed()

                        return
                    }

                    val state =
                        viewModel.uiState.value

                    /*
                     * Form değişmemişse direkt geri çık.
                     */
                    if (
                        !state.isFormChanged
                    ) {

                        isEnabled =
                            false

                        requireActivity()
                            .onBackPressedDispatcher
                            .onBackPressed()

                        return
                    }

                    /*
                     * Form değişmişse kullanıcıdan onay al.
                     */
                    showUnsavedChangesDialog()
                }
            }

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                callback
            )
    }
    /**
     * Kullanıcı kaydedilmemiş değişikliklerle çıkmaya çalıştığında
     * gösterilen confirmation dialog.
     */
    private fun showUnsavedChangesDialog() {

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                R.string.user_detail_unsaved_changes_title
            )
            .setMessage(
                R.string.user_detail_unsaved_changes_message
            )
            .setNegativeButton(
                R.string.user_detail_unsaved_changes_stay,
                null
            )
            .setPositiveButton(
                R.string.user_detail_unsaved_changes_leave
            ) { _, _ ->

                /*
                 * Artık bir sonraki back işlemini engellemiyoruz.
                 */
                allowNavigationBack =
                    true

                requireActivity()
                    .onBackPressedDispatcher
                    .onBackPressed()
            }
            .show()
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

        const val ARG_USER_ID =
            "userId"

        const val RESULT_USER_CHANGED =
            "result_user_changed"

        private const val INVALID_USER_ID =
            -1
    }
}