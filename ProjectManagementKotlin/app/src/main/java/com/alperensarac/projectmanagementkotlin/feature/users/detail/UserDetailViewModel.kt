package com.alperensarac.projectmanagementkotlin.feature.users.detail

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.GetCurrentUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.DeleteUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.GetUserByIdUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.ResetUserPasswordUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.UpdateUserStatusUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.UpdateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Admin kullanıcı detay ekranının ViewModel'idir.
 *
 * Bu ViewModel artık sadece network işlemlerini değil,
 * form state'ini de yönetmektedir.
 *
 * Böylece:
 *
 * - Fragment business logic içermez.
 * - Form değişmiş mi merkezi olarak anlaşılır.
 * - Kaydet butonu yalnızca gerekli olduğunda aktif olur.
 * - Backend validation hataları doğru input alanına aktarılır.
 */
@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val resetUserPasswordUseCase: ResetUserPasswordUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {

    // =========================================================================
    // STATE
    // =========================================================================

    private val mutableUiState =
        MutableStateFlow(
            UserDetailUiState()
        )

    val uiState: StateFlow<UserDetailUiState> =
        mutableUiState.asStateFlow()

    // =========================================================================
    // EVENTS
    // =========================================================================

    /**
     * Snackbar, navigation gibi tek seferlik olaylar için Channel.
     */
    private val eventChannel =
        Channel<UserDetailUiEvent>(
            capacity = Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // LOAD USER
    // =========================================================================

    fun loadUser(
        userId: Int
    ) {

        if (
            userId <= 0 ||
            mutableUiState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )

            /*
             * Önce mevcut oturum kullanıcısını alıyoruz.
             *
             * Bunun amacı:
             * Detayını açtığımız hesap kendi hesabımız mı?
             */
            val currentUserResult =
                getCurrentUserUseCase()

            val currentUserId =
                when (
                    currentUserResult
                ) {

                    is AppResult.Success ->
                        currentUserResult.data.id

                    is AppResult.Error ->
                        null
                }

            /*
             * Daha sonra görüntülenecek kullanıcıyı getiriyoruz.
             */
            when (
                val result =
                    getUserByIdUseCase(
                        userId = userId
                    )
            ) {

                is AppResult.Success -> {

                    val user =
                        result.data

                    val role =
                        UserRole.fromApiValue(
                            user.role
                        ) ?: UserRole.TEAM_MEMBER

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoading = false,

                            user = user,

                            currentUserId =
                            currentUserId,

                            firstName =
                            user.firstName,

                            lastName =
                            user.lastName,

                            email =
                            user.email,

                            selectedRole =
                            role,

                            department =
                            user.department.orEmpty(),

                            firstNameError = null,
                            lastNameError = null,
                            emailError = null,
                            departmentError = null,

                            errorMessage = null
                        )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isLoading = false,

                            currentUserId =
                            currentUserId,

                            errorMessage =
                            result.error
                                .toUserMessage()
                        )
                }
            }
        }
    }
    /**
     * Backend'den gelen User modelini hem:
     *
     * - original user
     * - form değerleri
     *
     * olarak state'e yerleştirir.
     *
     * Bu çok önemli.
     *
     * Çünkü form ilk açıldığında:
     *
     * original == form
     *
     * olacağı için:
     *
     * isFormChanged = false
     * canSave = false
     *
     * olacaktır.
     */
    private fun setLoadedUser(
        user: User
    ) {

        val role =
            UserRole.fromApiValue(
                user.role
            ) ?: UserRole.TEAM_MEMBER

        mutableUiState.value =
            mutableUiState.value.copy(

                isLoading = false,

                user = user,

                // -------------------------------------------------------------
                // FORM
                // -------------------------------------------------------------

                firstName =
                user.firstName,

                lastName =
                user.lastName,

                email =
                user.email,

                selectedRole =
                role,

                department =
                user.department
                    .orEmpty(),

                // -------------------------------------------------------------
                // ERRORS
                // -------------------------------------------------------------

                firstNameError = null,

                lastNameError = null,

                emailError = null,

                departmentError = null,

                errorMessage = null
            )
    }

    // =========================================================================
    // FORM EVENTS
    // =========================================================================

    /**
     * Ad alanı değişti.
     */
    fun onFirstNameChanged(
        value: String
    ) {

        /*
         * Fragment render sırasında aynı değeri tekrar set ederse
         * gereksiz StateFlow emission oluşturmayalım.
         */
        if (
            mutableUiState.value.firstName ==
            value
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                firstName = value,

                /*
                 * Kullanıcı alanı yeniden düzenlemeye başladığında
                 * eski validation hatasını temizliyoruz.
                 */
                firstNameError = null
            )
    }

    /**
     * Soyad alanı değişti.
     */
    fun onLastNameChanged(
        value: String
    ) {

        if (
            mutableUiState.value.lastName ==
            value
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                lastName = value,
                lastNameError = null
            )
    }

    /**
     * E-posta değişti.
     */
    fun onEmailChanged(
        value: String
    ) {

        if (
            mutableUiState.value.email ==
            value
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                email = value,
                emailError = null
            )
    }

    /**
     * Rol değişti.
     */
    fun onRoleChanged(
        role: UserRole
    ) {

        if (
            mutableUiState.value.selectedRole ==
            role
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                selectedRole = role
            )
    }

    /**
     * Departman değişti.
     */
    fun onDepartmentChanged(
        value: String
    ) {

        if (
            mutableUiState.value.department ==
            value
        ) {
            return
        }

        mutableUiState.value =
            mutableUiState.value.copy(
                department = value,
                departmentError = null
            )
    }

    // =========================================================================
    // UPDATE USER
    // =========================================================================

    /**
     * Artık Fragment'tan form alanlarını parametre olarak almıyoruz.
     *
     * ViewModel kendi state'indeki form verilerini kullanır.
     */
    fun updateUser(
        userId: Int
    ) {

        val currentState =
            mutableUiState.value

        // ---------------------------------------------------------------------
        // GUARD
        // ---------------------------------------------------------------------

        if (
            currentState.isBusy
        ) {
            return
        }

        /*
         * Form değişmediyse API'ye gereksiz PUT göndermiyoruz.
         */
        if (
            !currentState.isFormChanged
        ) {
            return
        }

        // ---------------------------------------------------------------------
        // NORMALIZE
        // ---------------------------------------------------------------------

        val normalizedFirstName =
            currentState.firstName.trim()

        val normalizedLastName =
            currentState.lastName.trim()

        val normalizedEmail =
            currentState.email.trim()

        val normalizedDepartment =
            currentState.department.trim()

        // ---------------------------------------------------------------------
        // CLIENT VALIDATION
        // ---------------------------------------------------------------------

        val firstNameError =
            validateFirstName(
                normalizedFirstName
            )

        val lastNameError =
            validateLastName(
                normalizedLastName
            )

        val emailError =
            validateEmail(
                normalizedEmail
            )

        val departmentError =
            validateDepartment(
                normalizedDepartment
            )

        val hasValidationError =
            firstNameError != null ||
                    lastNameError != null ||
                    emailError != null ||
                    departmentError != null

        if (
            hasValidationError
        ) {

            mutableUiState.value =
                currentState.copy(
                    firstNameError = firstNameError,
                    lastNameError = lastNameError,
                    emailError = emailError,
                    departmentError = departmentError,
                    errorMessage = null
                )

            return
        }

        // ---------------------------------------------------------------------
        // REQUEST
        // ---------------------------------------------------------------------

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isSaving = true,

                    firstNameError = null,
                    lastNameError = null,
                    emailError = null,
                    departmentError = null,

                    errorMessage = null
                )

            when (
                val result =
                    updateUserUseCase(
                        userId = userId,

                        firstName =
                        normalizedFirstName,

                        lastName =
                        normalizedLastName,

                        email =
                        normalizedEmail,

                        role =
                        currentState.selectedRole,

                        department =
                        normalizedDepartment
                            .takeIf {
                                it.isNotBlank()
                            }
                    )
            ) {

                // =============================================================
                // SUCCESS
                // =============================================================

                is AppResult.Success -> {

                    /*
                     * Backend'in döndürdüğü kullanıcı artık yeni
                     * "orijinal" kullanıcıdır.
                     *
                     * setLoadedUser() form değerlerini de bununla eşitlediği
                     * için isFormChanged otomatik false olur.
                     */
                    setLoadedUser(
                        user = result.data
                    )

                    eventChannel.send(
                        UserDetailUiEvent.ShowMessage(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Kullanıcı başarıyla güncellendi."
                        )
                    )
                }

                // =============================================================
                // ERROR
                // =============================================================

                is AppResult.Error -> {

                    handleUpdateError(
                        error = result.error
                    )
                }
            }
        }
    }

    // =========================================================================
    // UPDATE ERROR
    // =========================================================================

    /**
     * Backend validation hatalarını ilgili form alanlarına dağıtır.
     *
     * Örneğin backend:
     *
     * {
     *   "errors": {
     *      "Email": [
     *          "Geçerli bir e-posta adresi girilmelidir."
     *      ]
     *   }
     * }
     *
     * döndürürse hata genel TextView yerine e-posta TextInputLayout üzerinde
     * gösterilir.
     */
    private fun handleUpdateError(
        error: NetworkError
    ) {

        if (
            error is NetworkError.Validation
        ) {

            val fieldErrors =
                error.fieldErrors

            /*
             * ASP.NET / FluentValidation alan adları genellikle DTO property
             * isimleriyle gelir:
             *
             * FirstName
             * LastName
             * Email
             * Role
             * Department
             *
             * Case-insensitive okuyarak daha dayanıklı hale getiriyoruz.
             */

            val firstNameError =
                findFieldError(
                    fieldErrors = fieldErrors,
                    fieldName = "FirstName"
                )

            val lastNameError =
                findFieldError(
                    fieldErrors = fieldErrors,
                    fieldName = "LastName"
                )

            val emailError =
                findFieldError(
                    fieldErrors = fieldErrors,
                    fieldName = "Email"
                )

            val departmentError =
                findFieldError(
                    fieldErrors = fieldErrors,
                    fieldName = "Department"
                )

            /*
             * Role için şu anda ayrı TextInputLayout error state'i
             * UiState'te tanımlamadık.
             *
             * Backend'den Role hatası gelirse genel hata olarak gösteriyoruz.
             */
            val roleError =
                findFieldError(
                    fieldErrors = fieldErrors,
                    fieldName = "Role"
                )

            val hasMappedFieldError =
                firstNameError != null ||
                        lastNameError != null ||
                        emailError != null ||
                        departmentError != null

            mutableUiState.value =
                mutableUiState.value.copy(
                    isSaving = false,

                    firstNameError =
                    firstNameError,

                    lastNameError =
                    lastNameError,

                    emailError =
                    emailError,

                    departmentError =
                    departmentError,

                    errorMessage =
                    when {

                        roleError != null ->
                            roleError

                        hasMappedFieldError ->
                            null

                        else ->
                            error.toUserMessage()
                    }
                )

            return
        }

        /*
         * Validation dışındaki:
         *
         * - timeout
         * - connection
         * - unauthorized
         * - unknown
         *
         * hatalar genel mesaj olarak gösterilir.
         */
        mutableUiState.value =
            mutableUiState.value.copy(
                isSaving = false,

                errorMessage =
                error.toUserMessage()
            )
    }

    /**
     * Backend alan adını case-insensitive bulur.
     */
    private fun findFieldError(
        fieldErrors: Map<String, List<String>>,
        fieldName: String
    ): String? {

        return fieldErrors
            .entries
            .firstOrNull { entry ->

                entry.key.equals(
                    fieldName,
                    ignoreCase = true
                )
            }
            ?.value
            ?.firstOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    // =========================================================================
    // STATUS
    // =========================================================================

    fun updateStatus(
        userId: Int,
        isActive: Boolean
    ) {

        val state =
            mutableUiState.value

        if (
            state.isBusy
        ) {
            return
        }


        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isChangingStatus = true,
                    errorMessage = null
                )

            when (
                val result =
                    updateUserStatusUseCase(
                        userId = userId,
                        isActive = isActive
                    )
            ) {

                // =============================================================
                // SUCCESS
                // =============================================================

                is AppResult.Success -> {

                    /*
                     * Status endpoint'i UserResponseDto dönüyor.
                     *
                     * Fakat burada form alanlarını setLoadedUser() ile tamamen
                     * yeniden yüklemek istemiyoruz.
                     *
                     * Çünkü kullanıcı formda henüz kaydetmediği bir değişiklik
                     * yaptıysa status değiştirdiğinde o değişikliği kaybetmemeli.
                     *
                     * Bu nedenle yalnızca original user nesnesini güncelliyoruz.
                     */
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isChangingStatus = false,
                            user = result.data
                        )

                    eventChannel.send(
                        UserDetailUiEvent.ShowMessage(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: if (
                                    isActive
                                ) {
                                    "Kullanıcı hesabı aktif hâle getirildi."
                                } else {
                                    "Kullanıcı hesabı pasif hâle getirildi."
                                }
                        )
                    )
                }

                // =============================================================
                // ERROR
                // =============================================================

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isChangingStatus = false,

                            errorMessage =
                            result.error
                                .toUserMessage()
                        )
                }
            }
        }
    }

    // =========================================================================
    // RESET PASSWORD
    // =========================================================================

    fun resetPassword(
        userId: Int,
        newPassword: String
    ) {

        if (
            mutableUiState.value.isBusy
        ) {
            return
        }

        val passwordError =
            validatePassword(
                newPassword
            )

        if (
            passwordError != null
        ) {

            viewModelScope.launch {

                eventChannel.send(
                    UserDetailUiEvent.ShowMessage(
                        message =
                        passwordError
                    )
                )
            }

            return
        }

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isResettingPassword = true,
                    errorMessage = null
                )

            when (
                val result =
                    resetUserPasswordUseCase(
                        userId = userId,
                        newPassword = newPassword
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isResettingPassword = false
                        )

                    eventChannel.send(
                        UserDetailUiEvent.ShowMessage(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Kullanıcının şifresi başarıyla sıfırlandı."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isResettingPassword = false,

                            errorMessage =
                            result.error
                                .toUserMessage()
                        )
                }
            }
        }
    }

    // =========================================================================
    // PASSWORD VALIDATION
    // =========================================================================

    /**
     * Backend ResetUserPasswordRequestValidator ile aynı kurallar:
     *
     * - zorunlu
     * - min 8
     * - max 100
     * - büyük harf
     * - küçük harf
     * - rakam
     */
    fun validatePassword(
        password: String
    ): String? {

        return when {

            password.isBlank() ->
                "Yeni şifre zorunludur."

            password.length < 8 ->
                "Yeni şifre en az 8 karakter olmalıdır."

            password.length > 100 ->
                "Yeni şifre en fazla 100 karakter olabilir."

            !password.any {
                it.isUpperCase()
            } ->
                "Yeni şifre en az bir büyük harf içermelidir."

            !password.any {
                it.isLowerCase()
            } ->
                "Yeni şifre en az bir küçük harf içermelidir."

            !password.any {
                it.isDigit()
            } ->
                "Yeni şifre en az bir rakam içermelidir."

            else ->
                null
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    fun deleteUser(
        userId: Int
    ) {

        val state =
            mutableUiState.value

        if (
            state.isBusy
        ) {
            return
        }

        /*
         * Kendi hesabımızın silinmesini UI tarafında da engelliyoruz.
         *
         * Backend'in kendi iş kuralları yine son otoritedir.
         */
        if (
            state.isCurrentUser
        ) {

            viewModelScope.launch {

                eventChannel.send(
                    UserDetailUiEvent.ShowMessage(
                        "Kendi kullanıcı hesabınızı silemezsiniz."
                    )
                )
            }

            return
        }


        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isDeleting = true,
                    errorMessage = null
                )

            when (
                val result =
                    deleteUserUseCase(
                        userId = userId
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isDeleting = false
                        )

                    eventChannel.send(
                        UserDetailUiEvent.UserDeleted(
                            message =
                            result.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "Kullanıcı başarıyla silindi."
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isDeleting = false,

                            errorMessage =
                            result.error
                                .toUserMessage()
                        )
                }
            }
        }
    }

    // =========================================================================
    // CLIENT VALIDATION
    // =========================================================================

    private fun validateFirstName(
        value: String
    ): String? {

        return when {

            value.isBlank() ->
                "Kullanıcı adı zorunludur."

            value.length > 50 ->
                "Kullanıcı adı en fazla 50 karakter olabilir."

            else ->
                null
        }
    }

    private fun validateLastName(
        value: String
    ): String? {

        return when {

            value.isBlank() ->
                "Kullanıcı soyadı zorunludur."

            value.length > 50 ->
                "Kullanıcı soyadı en fazla 50 karakter olabilir."

            else ->
                null
        }
    }

    private fun validateEmail(
        value: String
    ): String? {

        return when {

            value.isBlank() ->
                "E-posta adresi zorunludur."

            value.length > 200 ->
                "E-posta adresi en fazla 200 karakter olabilir."

            !Patterns.EMAIL_ADDRESS
                .matcher(
                    value
                )
                .matches() ->
                "Geçerli bir e-posta adresi girilmelidir."

            else ->
                null
        }
    }

    private fun validateDepartment(
        value: String
    ): String? {

        return if (
            value.length > 100
        ) {
            "Departman en fazla 100 karakter olabilir."
        } else {
            null
        }
    }

    // =========================================================================
    // ERROR
    // =========================================================================

    fun clearError() {

        if (
            mutableUiState.value.errorMessage != null
        ) {

            mutableUiState.value =
                mutableUiState.value.copy(
                    errorMessage = null
                )
        }
    }
}