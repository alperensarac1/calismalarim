package com.alperensarac.projectmanagementkotlin.feature.users.create

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.CreateUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Admin yeni kullanıcı formunun ViewModel'idir.
 */
@HiltViewModel
class CreateUserViewModel @Inject constructor(
    private val createUserUseCase:
    CreateUserUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            CreateUserUiState()
        )

    val uiState:
            StateFlow<CreateUserUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<CreateUserUiEvent>(
            capacity = Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // CREATE
    // =========================================================================

    fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole,
        department: String,
        isActive: Boolean
    ) {

        if (
            mutableUiState.value.isSubmitting
        ) {
            return
        }

        val normalizedFirstName =
            firstName.trim()

        val normalizedLastName =
            lastName.trim()

        val normalizedEmail =
            email.trim()

        val normalizedDepartment =
            department.trim()

        // ---------------------------------------------------------------------
        // FIRST NAME
        // ---------------------------------------------------------------------

        val firstNameError =
            when {

                normalizedFirstName.isBlank() ->
                    "Kullanıcı adı zorunludur."

                normalizedFirstName.length > 50 ->
                    "Kullanıcı adı en fazla 50 karakter olabilir."

                else ->
                    null
            }

        // ---------------------------------------------------------------------
        // LAST NAME
        // ---------------------------------------------------------------------

        val lastNameError =
            when {

                normalizedLastName.isBlank() ->
                    "Kullanıcı soyadı zorunludur."

                normalizedLastName.length > 50 ->
                    "Kullanıcı soyadı en fazla 50 karakter olabilir."

                else ->
                    null
            }

        // ---------------------------------------------------------------------
        // EMAIL
        // ---------------------------------------------------------------------

        val emailError =
            when {

                normalizedEmail.isBlank() ->
                    "E-posta adresi zorunludur."

                normalizedEmail.length > 200 ->
                    "E-posta adresi en fazla 200 karakter olabilir."

                !Patterns.EMAIL_ADDRESS
                    .matcher(
                        normalizedEmail
                    )
                    .matches() ->
                    "Geçerli bir e-posta adresi girilmelidir."

                else ->
                    null
            }

        // ---------------------------------------------------------------------
        // PASSWORD
        // ---------------------------------------------------------------------

        val passwordError =
            when {

                password.isBlank() ->
                    "Şifre zorunludur."

                password.length < 8 ->
                    "Şifre en az 8 karakter olmalıdır."

                password.length > 100 ->
                    "Şifre en fazla 100 karakter olabilir."

                !password.any {
                    it.isUpperCase()
                } ->
                    "Şifre en az bir büyük harf içermelidir."

                !password.any {
                    it.isLowerCase()
                } ->
                    "Şifre en az bir küçük harf içermelidir."

                !password.any {
                    it.isDigit()
                } ->
                    "Şifre en az bir rakam içermelidir."

                else ->
                    null
            }

        // ---------------------------------------------------------------------
        // DEPARTMENT
        // ---------------------------------------------------------------------

        val departmentError =
            if (
                normalizedDepartment.length > 100
            ) {

                "Departman en fazla 100 karakter olabilir."

            } else {

                null
            }

        // ---------------------------------------------------------------------
        // CHECK
        // ---------------------------------------------------------------------

        val hasError =
            firstNameError != null ||
                    lastNameError != null ||
                    emailError != null ||
                    passwordError != null ||
                    departmentError != null

        if (
            hasError
        ) {

            mutableUiState.value =
                mutableUiState.value.copy(
                    firstNameError =
                    firstNameError,

                    lastNameError =
                    lastNameError,

                    emailError =
                    emailError,

                    passwordError =
                    passwordError,

                    departmentError =
                    departmentError,

                    generalError =
                    null
                )

            return
        }

        // ---------------------------------------------------------------------
        // REQUEST
        // ---------------------------------------------------------------------

        viewModelScope.launch {

            mutableUiState.value =
                mutableUiState.value.copy(
                    isSubmitting = true,
                    firstNameError = null,
                    lastNameError = null,
                    emailError = null,
                    passwordError = null,
                    departmentError = null,
                    generalError = null
                )

            when (
                val result =
                    createUserUseCase(
                        firstName =
                        normalizedFirstName,

                        lastName =
                        normalizedLastName,

                        email =
                        normalizedEmail,

                        password =
                        password,

                        role =
                        role,

                        department =
                        normalizedDepartment
                            .takeIf {
                                it.isNotBlank()
                            },

                        isActive =
                        isActive
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isSubmitting = false
                        )

                    eventChannel.send(
                        CreateUserUiEvent.UserCreated(
                            userId =
                            result.data.id,

                            message =
                            result.message ?: ""
                        )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isSubmitting = false,

                            generalError =
                            result.error
                                .toUserMessage()
                        )
                }
            }
        }
    }
}