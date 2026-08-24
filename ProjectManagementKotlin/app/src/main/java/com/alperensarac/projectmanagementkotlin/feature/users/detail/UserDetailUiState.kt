package com.alperensarac.projectmanagementkotlin.feature.users.detail

import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole

/**
 * Kullanıcı detay ekranının bütün durumunu temsil eder.
 *
 * Backend'den gelen User aynı zamanda formun orijinal halidir.
 * firstName, lastName vb. alanlar ise kullanıcının form üzerinde
 * yaptığı geçici değişiklikleri temsil eder.
 */
data class UserDetailUiState(

    // =========================================================================
    // OPERATION STATES
    // =========================================================================

    val isLoading: Boolean = false,

    val isSaving: Boolean = false,

    val isChangingStatus: Boolean = false,

    val isResettingPassword: Boolean = false,

    val isDeleting: Boolean = false,

    // =========================================================================
    // USER
    // =========================================================================

    val user: User? = null,

    /**
     * GET /api/Auth/me sonucundan alınır.
     *
     * Özellikle self-delete kontrolü için kullanıyoruz.
     */
    val currentUserId: Int? = null,

    // =========================================================================
    // FORM
    // =========================================================================

    val firstName: String = "",

    val lastName: String = "",

    val email: String = "",

    val selectedRole: UserRole = UserRole.TEAM_MEMBER,

    val department: String = "",

    // =========================================================================
    // VALIDATION
    // =========================================================================

    val firstNameError: String? = null,

    val lastNameError: String? = null,

    val emailError: String? = null,

    val departmentError: String? = null,

    // =========================================================================
    // GENERAL ERROR
    // =========================================================================

    val errorMessage: String? = null
) {

    // =========================================================================
    // BUSY
    // =========================================================================

    val isBusy: Boolean
        get() =
            isLoading ||
                    isSaving ||
                    isChangingStatus ||
                    isResettingPassword ||
                    isDeleting

    // =========================================================================
    // CURRENT USER
    // =========================================================================

    /**
     * Detay ekranında açılan kullanıcı,
     * oturum açmış kullanıcının kendisi mi?
     */
    val isCurrentUser: Boolean
        get() {

            val targetUser =
                user ?: return false

            val loggedInUserId =
                currentUserId ?: return false

            return targetUser.id ==
                    loggedInUserId
        }

    // =========================================================================
    // FORM CHANGED
    // =========================================================================

    val isFormChanged: Boolean
        get() {

            val originalUser =
                user ?: return false

            val originalRole =
                UserRole.fromApiValue(
                    originalUser.role
                )

            return firstName.trim() !=
                    originalUser.firstName.trim() ||

                    lastName.trim() !=
                    originalUser.lastName.trim() ||

                    email.trim() !=
                    originalUser.email.trim() ||

                    selectedRole !=
                    originalRole ||

                    department.trim() !=
                    originalUser.department
                        .orEmpty()
                        .trim()
        }

    // =========================================================================
    // SAVE
    // =========================================================================

    val canSave: Boolean
        get() =
            user != null &&
                    !isBusy &&
                    isFormChanged

    // =========================================================================
    // STATUS
    // =========================================================================

    /**
     * Backend self-deactivate işlemini yasaklamıyor.
     *
     * Bu nedenle kendi hesabımız dahil olmak üzere aktiflik durumu
     * değiştirilebilir.
     */
    val canChangeStatus: Boolean
        get() =
            user != null &&
                    !isBusy

    // =========================================================================
    // RESET PASSWORD
    // =========================================================================

    /**
     * Backend:
     *
     * if (!user.IsActive)
     *     throw BusinessRuleException(...)
     *
     * yaptığı için pasif kullanıcının şifresini sıfırlama butonunu
     * UI tarafında da kapatıyoruz.
     */
    val canResetPassword: Boolean
        get() =
            user != null &&
                    user.isActive &&
                    !isBusy

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * Backend kendi oturum hesabının silinmesini açıkça yasaklıyor.
     */
    val canDelete: Boolean
        get() =
            user != null &&
                    !isBusy &&
                    !isCurrentUser
}