package com.alperensarac.projectmanagementkotlin.feature.projects.create

import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser

/**
 * Proje oluşturma ekranının UI state'i.
 *
 * Artık yalnız validation değil,
 * oturumdaki kullanıcı bilgisi de burada tutuluyor.
 *
 * Bunun sebebi:
 *
 * Admin:
 * - proje oluşturabilir
 * - OwnerId seçebilir
 *
 * ProjectManager:
 * - proje oluşturabilir
 * - OwnerId seçemez
 * - backend otomatik olarak kendisini owner yapar
 */
data class CreateProjectUiState(

    // =========================================================================
    // SESSION
    // =========================================================================

    val currentUser: AuthUser? = null,

    val isLoadingPermission: Boolean = false,

    // =========================================================================
    // SUBMIT
    // =========================================================================

    val isSubmitting: Boolean = false,

    // =========================================================================
    // FIELD ERRORS
    // =========================================================================

    val nameError: String? = null,

    val descriptionError: String? = null,

    val startDateError: String? = null,

    val endDateError: String? = null,

    val ownerIdError: String? = null,

    // =========================================================================
    // GENERAL ERROR
    // =========================================================================

    val generalError: String? = null
) {

    /**
     * Backend:
     *
     * [Authorize(Roles = "Admin,ProjectManager")]
     */
    val canCreateProject: Boolean
        get() =
            currentUser?.isAdmin == true ||
                    currentUser?.isProjectManager == true

    /**
     * Owner değiştirme/seçme yetkisi yalnızca Admin'de.
     *
     * ProjectManager create sırasında OwnerId gönderse bile
     * backend kendi userId'sini owner yapıyor.
     */
    val canSelectOwner: Boolean
        get() =
            currentUser?.isAdmin == true

    val isBusy: Boolean
        get() =
            isLoadingPermission ||
                    isSubmitting
}