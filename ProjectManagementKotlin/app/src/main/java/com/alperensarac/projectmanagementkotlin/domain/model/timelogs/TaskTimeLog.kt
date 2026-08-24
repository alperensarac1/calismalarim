package com.alperensarac.projectmanagementkotlin.domain.model.timelogs

/**
 * Uygulama içerisinde kullanılan zaman kaydı domain modelidir.
 */
data class TaskTimeLog(
    val id: Int,

    val taskId: Int,

    val userId: Int,

    val userFullName: String,

    val userEmail: String,

    val hours: Double,

    val description: String?,

    val workDateUtc: String,

    val createdAtUtc: String,

    /**
     * Backend tarafından hesaplanan izinler.
     */
    val canEdit: Boolean,

    val canDelete: Boolean
)