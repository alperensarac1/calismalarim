package com.alperensarac.projectmanagementkotlin.data.remote.dto.history

import kotlinx.serialization.Serializable

/**
 * Backend:
 *
 * ProjectManagement.Application.DTOs.TaskHistories.TaskHistoryResponseDto
 *
 * modelinin Android network karşılığıdır.
 */
@Serializable
data class TaskHistoryResponseDto(

    /**
     * History kaydının kendi id değeri.
     */
    val id: Int,

    /**
     * Değişikliğin ait olduğu görev.
     */
    val taskId: Int,

    /**
     * İşlemi yapan kullanıcı.
     */
    val changedByUserId: Int,

    val changedByUserFullName: String,

    val changedByUserEmail: String,

    /**
     * Backend tarafından gönderilen değişiklik tipi.
     *
     * Örneğin:
     *
     * AssignedUserChanged
     *
     * Android bu değerin tüm olası enum seçeneklerini tahmin etmiyor.
     */
    val changeType: String,

    /**
     * Bazı history türlerinde eski/yeni değer olmayabilir.
     */
    val oldValue: String? = null,

    val newValue: String? = null,

    /**
     * Backend'in insan tarafından okunabilir açıklaması.
     *
     * Örneğin:
     *
     * Görev Ayşe Demir kullanıcısına atandı.
     */
    val description: String? = null,

    val createdAt: String
)