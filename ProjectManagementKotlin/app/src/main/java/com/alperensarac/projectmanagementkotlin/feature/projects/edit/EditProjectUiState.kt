package com.alperensarac.projectmanagementkotlin.feature.projects.edit

import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus

/**
 * Proje düzenleme ekranının bütün state'ini temsil eder.
 *
 * Burada iki ayrı veri grubu vardır:
 *
 * 1. project
 *    Backend'den gelen orijinal proje.
 *
 * 2. name, description, startDate...
 *    Kullanıcının form üzerinde değiştirdiği geçici değerler.
 *
 * Böylece formun değişip değişmediğini Fragment içerisinde değil,
 * merkezi olarak state üzerinden hesaplayabiliyoruz.
 */
data class EditProjectUiState(

    // =========================================================================
    // OPERATION
    // =========================================================================

    val isLoading: Boolean = false,

    val isSaving: Boolean = false,

    // =========================================================================
    // ORIGINAL DATA
    // =========================================================================

    val project: Project? = null,

    val currentUser: AuthUser? = null,

    // =========================================================================
    // FORM
    // =========================================================================

    val name: String = "",

    val description: String = "",

    /**
     * Backend'e gönderilecek format:
     *
     * yyyy-MM-ddT00:00:00
     */
    val startDate: String = "",

    val endDate: String = "",

    val selectedStatus: ProjectStatus =
        ProjectStatus.PLANNING,

    /**
     * Admin proje sahibini değiştirebilir.
     *
     * ProjectManager için mevcut owner ID burada yine tutulur ancak
     * alan UI'da disabled olacaktır.
     */
    val ownerIdText: String = "",

    // =========================================================================
    // FIELD ERRORS
    // =========================================================================

    val nameError: String? = null,

    val descriptionError: String? = null,

    val startDateError: String? = null,

    val endDateError: String? = null,

    val ownerIdError: String? = null,

    val statusError: String? = null,

    // =========================================================================
    // GENERAL ERROR
    // =========================================================================

    val generalError: String? = null
) {

    val hasContent: Boolean
        get() =
            project != null

    val isBusy: Boolean
        get() =
            isLoading ||
                    isSaving

    // =========================================================================
    // PERMISSION
    // =========================================================================

    /**
     * Backend ProjectService.EnsureCanManageProject:
     *
     * Admin -> bütün projeler
     *
     * ProjectManager ->
     * yalnızca project.ownerId == currentUser.id
     */
    val canEditProject: Boolean
        get() {

            val loadedProject =
                project ?: return false

            val user =
                currentUser ?: return false

            if (
                user.isAdmin
            ) {
                return true
            }

            return user.isProjectManager &&
                    loadedProject.ownerId ==
                    user.id
        }

    /**
     * Backend yalnızca Admin için owner değişikliğini uyguluyor.
     */
    val canChangeOwner: Boolean
        get() =
            currentUser?.isAdmin == true &&
                    canEditProject

    // =========================================================================
    // DIRTY FORM
    // =========================================================================

    val isFormChanged: Boolean
        get() {

            val original =
                project ?: return false

            val originalStatus =
                ProjectStatus.fromApiValue(
                    original.status
                )

            val normalizedOriginalStartDate =
                normalizeBackendDate(
                    original.startDateUtc
                )

            val normalizedOriginalEndDate =
                original.endDateUtc
                    ?.let(
                        ::normalizeBackendDate
                    )
                    .orEmpty()

            val ownerChanged =
                if (
                    canChangeOwner
                ) {

                    ownerIdText.trim() !=
                            original.ownerId.toString()

                } else {

                    false
                }

            return name.trim() !=
                    original.name.trim() ||

                    description.trim() !=
                    original.description
                        .orEmpty()
                        .trim() ||

                    startDate.trim() !=
                    normalizedOriginalStartDate ||

                    endDate.trim() !=
                    normalizedOriginalEndDate ||

                    selectedStatus !=
                    originalStatus ||

                    ownerChanged
        }

    val canSave: Boolean
        get() =
            hasContent &&
                    canEditProject &&
                    !isBusy &&
                    isFormChanged

    companion object {

        /**
         * Backend DateTime örnekleri:
         *
         * 2026-08-14T00:00:00
         * 2026-08-14T00:00:00.0000000
         * 2026-08-14T00:00:00Z
         *
         * Edit ekranımızda yalnız tarih seçildiği için ilk 10 karakteri
         * kullanıp tekrar bizim standart request biçimimize dönüştürüyoruz.
         *
         * java.time kullanmadığımız için minSdk 24 ile uyumludur.
         */
        fun normalizeBackendDate(
            value: String
        ): String {

            if (
                value.length < 10
            ) {
                return value
            }

            val datePart =
                value.take(10)

            return "${datePart}T00:00:00"
        }
    }
}