package com.alperensarac.projectmanagementkotlin.data.remote.dto.comments

import kotlinx.serialization.Serializable

/**
 * Backend:
 *
 * ProjectManagement.Application.DTOs.Comments.CommentResponseDto
 *
 * modelinin Android karşılığıdır.
 */
@Serializable
data class CommentResponseDto(

    /**
     * Yorumun kendi id değeri.
     */
    val id: Int,

    /**
     * Yorumun bağlı olduğu görev.
     */
    val taskId: Int,

    /**
     * Yorumu yazan kullanıcının id'si.
     */
    val userId: Int,

    val userFullName: String,

    val userEmail: String,

    val content: String,

    val createdAt: String,

    val updatedAt: String? = null,

    /**
     * Bu değerleri Android hesaplamıyor.
     *
     * Backend mevcut kullanıcı ve izinlere göre belirliyor.
     */
    val canEdit: Boolean,

    val canDelete: Boolean
)