package com.alperensarac.projectmanagementkotlin.domain.model.comments

/**
 * Uygulama içerisindeki yorum domain modelidir.
 *
 * DTO veya Retrofit bağımlılığı içermez.
 */
data class TaskComment(
    val id: Int,
    val taskId: Int,

    val userId: Int,
    val userFullName: String,
    val userEmail: String,

    val content: String,

    val createdAtUtc: String,
    val updatedAtUtc: String?,

    /**
     * Yetki bilgisi backend tarafından hesaplanır.
     */
    val canEdit: Boolean,
    val canDelete: Boolean
)