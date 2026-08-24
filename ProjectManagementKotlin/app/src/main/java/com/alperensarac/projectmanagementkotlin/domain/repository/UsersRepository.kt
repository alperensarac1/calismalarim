package com.alperensarac.projectmanagementkotlin.domain.repository

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserFilter
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * Admin kullanıcı yönetimi domain repository sözleşmesidir.
 */
interface UsersRepository {

    // =========================================================================
    // LIST
    // =========================================================================

    fun getUsers(
        filter: UserFilter
    ): Flow<PagingData<User>>

    // =========================================================================
    // DETAIL
    // =========================================================================

    suspend fun getUserById(
        userId: Int
    ): AppResult<User>

    // =========================================================================
    // CREATE
    // =========================================================================

    suspend fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole,
        department: String?,
        isActive: Boolean
    ): AppResult<User>

    // =========================================================================
    // UPDATE
    // =========================================================================

    suspend fun updateUser(
        userId: Int,
        firstName: String,
        lastName: String,
        email: String,
        role: UserRole,
        department: String?
    ): AppResult<User>

    // =========================================================================
    // STATUS
    // =========================================================================

    suspend fun updateUserStatus(
        userId: Int,
        isActive: Boolean
    ): AppResult<User>

    // =========================================================================
    // PASSWORD
    // =========================================================================

    suspend fun resetUserPassword(
        userId: Int,
        newPassword: String
    ): AppResult<Unit>

    // =========================================================================
    // DELETE
    // =========================================================================

    suspend fun deleteUser(
        userId: Int
    ): AppResult<Unit>
}