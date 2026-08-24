package com.alperensarac.projectmanagementkotlin.feature.tasks.form

import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus

/**
 * Görev oluşturma / düzenleme formunun UI state'idir.
 *
 * Edit modunda Task nesnesini state içerisinde tutmamızın temel nedeni
 * process-death senaryosudur.
 *
 * Dialog'u açan Fragment ilk anda Task nesnesini memory üzerinden verebilir.
 * Ancak Android process'i öldürüp ekranı Bundle üzerinden yeniden oluşturursa
 * bu memory referansı kaybolur. ARG_TASK_ID korunacağı için ViewModel görevi
 * backend'den yeniden yükler ve editingTask alanına koyar.
 */
data class TaskFormUiState(

    // =========================================================================
    // OPERATION
    // =========================================================================

    val isSaving: Boolean = false,

    val isMembersLoading: Boolean = false,

    val isProjectLoading: Boolean = false,

    /**
     * Edit edilen Task backend'den yeniden yüklenirken true olur.
     */
    val isTaskLoading: Boolean = false,

    // =========================================================================
    // EDIT TASK
    // =========================================================================

    /**
     * Edit modundaki güncel görev.
     *
     * Create modunda null kalır.
     */
    val editingTask: Task? = null,

    // =========================================================================
    // PROJECT
    // =========================================================================

    /**
     * DueDate >= Project.StartDate validation'ı için kullanılır.
     */
    val projectStartDateUtc: String? = null,

    // =========================================================================
    // MEMBERS
    // =========================================================================

    val members: List<ProjectMember> =
        emptyList(),

    // =========================================================================
    // SELECTED VALUES
    // =========================================================================

    val selectedAssignedUserId: Int? =
        null,

    val selectedStatus: TaskStatus =
        TaskStatus.TODO,

    val selectedPriority: TaskPriority =
        TaskPriority.MEDIUM,

    // =========================================================================
    // ERROR
    // =========================================================================

    val errorMessage: String? =
        null

) {

    // =========================================================================
    // ASSIGNABLE MEMBERS
    // =========================================================================

    val assignableMembers: List<ProjectMember>
        get() =
            members.filter { member ->

                val memberRole =
                    ProjectMemberRole.fromApiValue(
                        member.projectRole
                    )

                member.isActive &&
                        memberRole != ProjectMemberRole.VIEWER
            }

    // =========================================================================
    // BUSY
    // =========================================================================

    val isBusy: Boolean
        get() =
            isSaving ||
                    isMembersLoading ||
                    isProjectLoading ||
                    isTaskLoading

    // =========================================================================
    // ASSIGNEE VALIDATION
    // =========================================================================

    val isSelectedAssigneeValid: Boolean
        get() {

            val selectedUserId =
                selectedAssignedUserId
                    ?: return true

            return assignableMembers.any { member ->

                member.userId ==
                        selectedUserId
            }
        }
}
