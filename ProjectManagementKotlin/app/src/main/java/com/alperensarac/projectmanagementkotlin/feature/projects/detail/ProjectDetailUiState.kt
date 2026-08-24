package com.alperensarac.projectmanagementkotlin.feature.projects.detail

import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember

/**
 * Proje detay ekranının bütün UI state'ini temsil eder.
 *
 * Önemli:
 *
 * Buradaki permission değerleri kullanıcı deneyimi içindir.
 * Gerçek authorization kontrolü her zaman backend tarafından yapılır.
 */
data class ProjectDetailUiState(

    // =========================================================================
    // LOADING / OPERATIONS
    // =========================================================================

    val isLoading: Boolean = false,

    val isRefreshing: Boolean = false,

    val isProjectOperationInProgress: Boolean = false,

    val isMemberOperationInProgress: Boolean = false,

    val processingMemberUserId: Int? = null,

    // =========================================================================
    // DATA
    // =========================================================================

    val project: Project? = null,

    val members: List<ProjectMember> = emptyList(),

    val currentUser: AuthUser? = null,

    val errorMessage: String? = null
) {

    // =========================================================================
    // CONTENT
    // =========================================================================

    val hasContent: Boolean
        get() =
            project != null

    val isMembersEmpty: Boolean
        get() =
            members.isEmpty()

    // =========================================================================
    // PROJECT MANAGEMENT
    // =========================================================================

    /**
     * Projeyi kim yönetebilir?
     *
     * Admin:
     * - bütün projeleri yönetebilir.
     *
     * ProjectManager:
     * - yalnızca sahibi olduğu projeyi yönetebilir.
     *
     * TeamMember:
     * - yönetemez.
     */
    val canManageProject: Boolean
        get() {

            val loadedProject =
                project ?: return false

            val loggedInUser =
                currentUser ?: return false

            if (
                loggedInUser.isAdmin
            ) {
                return true
            }

            return loggedInUser.isProjectManager &&
                    loadedProject.ownerId ==
                    loggedInUser.id
        }

    /**
     * Projenin OwnerId alanını yalnızca Admin değiştirebilir.
     */
    val canChangeOwner: Boolean
        get() =
            currentUser?.isAdmin == true &&
                    canManageProject

    val canEditProject: Boolean
        get() =
            canManageProject &&
                    !isProjectOperationInProgress

    val canArchiveProject: Boolean
        get() =
            canManageProject &&
                    !isProjectOperationInProgress

    val canDeleteProject: Boolean
        get() =
            canManageProject &&
                    !isProjectOperationInProgress

    // =========================================================================
    // TASK MANAGEMENT
    // =========================================================================

    /**
     * Android uygulamasındaki görev oluşturma kuralımız:
     *
     * Admin:
     * - görev oluşturabilir.
     *
     * ProjectManager:
     * - yalnızca sahibi olduğu projede görev oluşturabilir.
     *
     * TeamMember / Contributor / Viewer:
     * - bu ekrandan görev oluşturamaz.
     *
     * NOT:
     * Backend Contributor'a da görev oluşturma yetkisi veriyor.
     * Ancak mobil UI'daki ürün kuralımızı daha kısıtlı tutuyoruz.
     */
    val canCreateTask: Boolean
        get() {

            val loadedProject =
                project ?: return false

            val loggedInUser =
                currentUser ?: return false

            /*
             * Backend arşivlenmiş projelerde hiçbir görev değişikliğine
             * izin vermiyor.
             */
            if (
                loadedProject.isArchived
            ) {
                return false
            }

            if (
                isProjectOperationInProgress
            ) {
                return false
            }

            /*
             * Admin bütün projelerde görev oluşturabilir.
             */
            if (
                loggedInUser.isAdmin
            ) {
                return true
            }

            /*
             * ProjectManager yalnızca sahibi olduğu projede.
             */
            return loggedInUser.isProjectManager &&
                    loadedProject.ownerId ==
                    loggedInUser.id
        }

    // =========================================================================
    // MEMBER MANAGEMENT
    // =========================================================================

    /**
     * Üye yönetimi:
     *
     * Admin:
     * - bütün projelerde.
     *
     * ProjectManager:
     * - yalnızca sahibi olduğu projede.
     */
    val canManageMembers: Boolean
        get() {

            val loadedProject =
                project ?: return false

            val loggedInUser =
                currentUser ?: return false

            if (
                loggedInUser.isAdmin
            ) {
                return true
            }

            return loggedInUser.isProjectManager &&
                    loadedProject.ownerId ==
                    loggedInUser.id
        }

    /**
     * Arşivlenmiş projelerde:
     *
     * - rol değiştirme
     * - üyeyi çıkarma
     *
     * işlemlerini kapatıyoruz.
     */
    val canMutateMembers: Boolean
        get() =
            canManageMembers &&
                    project?.isArchived == false &&
                    !isMemberOperationInProgress

    /**
     * Üye ekleme senin uygulama kuralına göre sadece Admin.
     *
     * Backend ProjectManager owner'a da izin verse bile
     * Android tarafında bunu açmıyoruz.
     */
    val canAddMembers: Boolean
        get() =
            currentUser?.isAdmin == true &&
                    project?.isArchived == false &&
                    !isMemberOperationInProgress
}