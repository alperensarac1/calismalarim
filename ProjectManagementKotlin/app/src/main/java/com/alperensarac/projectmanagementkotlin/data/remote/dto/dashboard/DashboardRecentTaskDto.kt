package com.alperensarac.projectmanagementkotlin.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

/**
 * GET /api/Dashboard/recent-tasks endpointinin liste elemanıdır.
 *
 * Backend response örneğinde yalnızca Dashboard için gerekli alanlar
 * bulunmaktadır.
 */
@Serializable
data class DashboardRecentTaskDto(
    val id: Int,
    val title: String,
    val projectId: Int,
    val projectName: String,
    val status: String,
    val priority: String,

    /*
     * Görev henüz bir kullanıcıya atanmamış olabilir.
     */
    val assignedToUserId: Int? = null,
    val assignedToUserFullName: String? = null,

    /*
     * Görevin son tarihi olmayabilir.
     */
    val dueDate: String? = null,

    val isOverdue: Boolean,
    val createdAt: String,

    /*
     * Görev oluşturulduktan sonra hiç güncellenmemiş olabilir.
     */
    val updatedAt: String? = null
)