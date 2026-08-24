package com.alperensarac.projectmanagementkotlin.feature.tasks.detail

import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment
import com.alperensarac.projectmanagementkotlin.domain.model.history.TaskHistory
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLogSummary

/**
 * Görev detay ekranının bütün kalıcı state'idir.
 *
 * Bu state:
 *
 * - Task
 * - Permission context
 * - Comments
 * - Time Logs
 * - Histories
 *
 * verilerini tek merkezden yönetir.
 *
 * ÖNEMLİ:
 *
 * Buradaki permission değerleri UI/UX içindir.
 * Gerçek authorization her zaman backend tarafından yapılır.
 */
data class TaskDetailUiState(

    // =========================================================================
    // TASK
    // =========================================================================

    val isLoading: Boolean = false,

    val isRefreshing: Boolean = false,

    val task: Task? = null,

    // =========================================================================
    // PERMISSION CONTEXT
    // =========================================================================

    /**
     * Oturum açmış kullanıcı.
     */
    val currentUser: AuthUser? = null,

    /**
     * Görevin bağlı olduğu proje.
     *
     * Özellikle:
     *
     * - ownerId
     * - isArchived
     *
     * bilgileri permission hesabında kullanılır.
     */
    val project: Project? = null,

    /**
     * Oturum açmış kullanıcının bu projedeki aktif üyeliği.
     *
     * Admin veya proje owner'ı için null olması normal olabilir.
     */
    val currentProjectMember: ProjectMember? = null,

    val isPermissionContextLoading: Boolean = false,

    // =========================================================================
    // COMMENTS
    // =========================================================================

    val isCommentsLoading: Boolean = false,

    val comments: List<TaskComment> =
        emptyList(),

    val isCommentSending: Boolean = false,

    val updatingCommentId: Int? = null,

    val deletingCommentId: Int? = null,

    val commentText: String = "",

    val commentsErrorMessage: String? = null,

    // =========================================================================
    // TIME LOGS
    // =========================================================================

    val isTimeLogsLoading: Boolean = false,

    val timeLogs: List<TaskTimeLog> =
        emptyList(),

    val timeLogSummary: TaskTimeLogSummary? = null,

    val isTimeLogCreating: Boolean = false,

    val processingTimeLogId: Int? = null,

    val timeLogsErrorMessage: String? = null,

    // =========================================================================
    // HISTORY
    // =========================================================================

    val isHistoriesLoading: Boolean = false,

    val histories: List<TaskHistory> =
        emptyList(),

    val historiesErrorMessage: String? = null,

    // =========================================================================
    // MAIN ERROR
    // =========================================================================

    val errorMessage: String? = null

) {

    // =========================================================================
    // GENERAL CONTENT
    // =========================================================================

    val hasContent: Boolean
        get() =
            task != null

    val areCommentsEmpty: Boolean
        get() =
            !isCommentsLoading &&
                    comments.isEmpty()

    val areTimeLogsEmpty: Boolean
        get() =
            !isTimeLogsLoading &&
                    timeLogs.isEmpty()

    val areHistoriesEmpty: Boolean
        get() =
            !isHistoriesLoading &&
                    histories.isEmpty()

    // =========================================================================
    // PERMISSION HELPERS
    // =========================================================================

    /**
     * Kullanıcı Admin mi?
     */
    private val isAdmin: Boolean
        get() =
            currentUser?.isAdmin == true

    /**
     * Kullanıcı, bu projenin sahibi olan ProjectManager mı?
     */
    private val isOwnerProjectManager: Boolean
        get() {

            val user =
                currentUser ?: return false

            val loadedProject =
                project ?: return false

            return user.isProjectManager &&
                    loadedProject.ownerId ==
                    user.id
        }

    /**
     * Proje üye rolünü enum'a çeviriyoruz.
     */
    private val currentProjectRole:
            ProjectMemberRole?
        get() =
            currentProjectMember
                ?.projectRole
                ?.let(
                    ProjectMemberRole::fromApiValue
                )

    private val hasActiveProjectMembership: Boolean
        get() =
            currentProjectMember?.isActive == true

    private val isContributor: Boolean
        get() =
            hasActiveProjectMembership &&
                    currentProjectRole ==
                    ProjectMemberRole.CONTRIBUTOR

    private val isMember: Boolean
        get() =
            hasActiveProjectMembership &&
                    currentProjectRole ==
                    ProjectMemberRole.MEMBER

    private val isViewer: Boolean
        get() =
            hasActiveProjectMembership &&
                    currentProjectRole ==
                    ProjectMemberRole.VIEWER

    /**
     * Arşivlenmiş proje üzerinde mutation yapılamaz.
     */
    private val isProjectWritable: Boolean
        get() =
            project?.isArchived == false

    // =========================================================================
    // COMMON WRITE PERMISSION
    // =========================================================================

    /**
     * CommentService.EnsureCanWriteCommentAsync()
     * ve
     * TaskTimeLogService.EnsureCanWriteTimeLogAsync()
     *
     * aynı temel authorization modelini kullanıyor.
     *
     * Yazabilenler:
     *
     * - Admin
     * - proje sahibi
     * - aktif Member
     * - aktif Contributor
     *
     * Yazamayan:
     *
     * - Viewer
     * - aktif üyeliği olmayan kullanıcı
     *
     * Ayrıca proje arşivlenmiş olmamalı.
     */
    private val canWriteProjectTaskContent: Boolean
        get() {

            if (
                !isProjectWritable
            ) {
                return false
            }

            if (
                isAdmin ||
                isOwnerProjectManager
            ) {
                return true
            }

            if (
                !hasActiveProjectMembership
            ) {
                return false
            }

            /*
             * Viewer yazamaz.
             *
             * Member ve Contributor yazabilir.
             */
            return !isViewer
        }

    // =========================================================================
    // COMMENT PERMISSIONS
    // =========================================================================

    /**
     * Kullanıcı yeni yorum oluşturabilir mi?
     */
    val canCreateComment: Boolean
        get() =
            canWriteProjectTaskContent &&
                    !isCommentSending

    /**
     * Send butonunun nihai durumu.
     *
     * Permission +
     * boş olmayan text +
     * request çalışmıyor olmalı.
     */
    val canSendComment: Boolean
        get() =
            canCreateComment &&
                    commentText.isNotBlank()

    // =========================================================================
    // TIME LOG PERMISSIONS
    // =========================================================================

    /**
     * Kullanıcı yeni bir zaman kaydı oluşturabilir mi?
     *
     * Backend kuralı yorum oluşturmayla aynı.
     */
    val canCreateTimeLog: Boolean
        get() =
            canWriteProjectTaskContent &&
                    !isTimeLogCreating

    // =========================================================================
    // TASK EDIT
    // =========================================================================

    /**
     * Görev düzenleme:
     *
     * - Admin
     * - Owner ProjectManager
     * - Contributor
     */
    val canEditTask: Boolean
        get() {

            if (
                !isProjectWritable
            ) {
                return false
            }

            return isAdmin ||
                    isOwnerProjectManager ||
                    isContributor
        }

    // =========================================================================
    // TASK ASSIGNMENT
    // =========================================================================

    /**
     * Görev ataması değiştirme:
     *
     * - Admin
     * - Owner ProjectManager
     * - Contributor
     */
    val canChangeTaskAssignment: Boolean
        get() {

            if (
                !isProjectWritable
            ) {
                return false
            }

            return isAdmin ||
                    isOwnerProjectManager ||
                    isContributor
        }

    // =========================================================================
    // TASK STATUS
    // =========================================================================

    /**
     * Status:
     *
     * Admin
     * Owner PM
     * Contributor
     *
     * Member:
     * yalnızca görev kendisine atanmışsa.
     */
    val canChangeTaskStatus: Boolean
        get() {

            if (
                !isProjectWritable
            ) {
                return false
            }

            if (
                isAdmin ||
                isOwnerProjectManager ||
                isContributor
            ) {
                return true
            }

            if (
                isViewer ||
                !isMember
            ) {
                return false
            }

            val userId =
                currentUser?.id
                    ?: return false

            val assignedUserId =
                task?.assignedToUserId
                    ?: return false

            return assignedUserId ==
                    userId
        }

    // =========================================================================
    // TASK DELETE
    // =========================================================================

    /**
     * Delete:
     *
     * sadece:
     *
     * - Admin
     * - Owner ProjectManager
     */
    val canDeleteTask: Boolean
        get() {

            if (
                !isProjectWritable
            ) {
                return false
            }

            return isAdmin ||
                    isOwnerProjectManager
        }

    // =========================================================================
    // TASK ACTION VISIBILITY
    // =========================================================================

    val hasAnyTaskAction: Boolean
        get() =
            canChangeTaskStatus ||
                    canChangeTaskAssignment ||
                    canEditTask ||
                    canDeleteTask
}