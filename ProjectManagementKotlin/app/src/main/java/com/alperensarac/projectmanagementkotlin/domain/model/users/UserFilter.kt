package com.alperensarac.projectmanagementkotlin.domain.model.users

/**
 * Kullanıcı listesi için kullanılan domain filtre modelidir.
 *
 * Backend UserListQueryDto ile eşleşen alanlar:
 *
 * - Search
 * - Role
 * - IsActive
 *
 * Page ve PageSize PagingSource tarafından yönetilir.
 */
data class UserFilter(
    val search: String = "",
    val role: String? = null,
    val isActive: Boolean? = null
)