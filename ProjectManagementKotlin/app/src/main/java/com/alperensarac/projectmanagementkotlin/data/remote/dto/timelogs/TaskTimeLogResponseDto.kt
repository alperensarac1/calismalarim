package com.alperensarac.projectmanagementkotlin.data.remote.dto.timelogs

import kotlinx.serialization.Serializable

/**
 * Backend:
 *
 * ProjectManagement.Application.DTOs.TaskTimeLogs.TaskTimeLogResponseDto
 *
 * modelinin Android network karşılığıdır.
 */
@Serializable
data class TaskTimeLogResponseDto(
    val id: Int,

    val taskId: Int,

    val userId: Int,

    val userFullName: String,

    val userEmail: String,

    /**
     * Backend decimal kullanıyor.
     *
     * UI hesaplamaları için Android tarafında Double kullanıyoruz.
     */
    val hours: Double,

    val description: String? = null,

    /**
     * Çalışmanın yapıldığı tarih.
     */
    val workDate: String,

    /**
     * Zaman kaydının oluşturulma tarihi.
     */
    val createdAt: String,

    /**
     * Bu izinleri backend hesaplıyor.
     */
    val canEdit: Boolean,

    val canDelete: Boolean
)