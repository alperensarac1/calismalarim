package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.core.network.model.PagedResult
import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxRecipientUserDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Mailbox recipient picker'ın kullandığı mevcut UsersController.
 *
 * Backend:
 *
 * GET /api/Users
 *
 * ÖNEMLİ:
 *
 * Şu anki backend'de UsersController:
 *
 * [Authorize(Roles = "Admin")]
 *
 * olduğundan bu API Admin dışında 403 döndürür.
 */
interface MailboxRecipientApi {

    @GET("api/Users")
    suspend fun getUsers(
        @Query("Page")
        page: Int = 1,

        @Query("PageSize")
        pageSize: Int = 50,

        @Query("Search")
        search: String? = null,

        @Query("IsActive")
        isActive: Boolean = true
    ): ApiResponse<PagedResult<MailboxRecipientUserDto>>
}