package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.core.network.model.PagedResult
import com.alperensarac.projectmanagementkotlin.data.remote.dto.common.EmptyObjectDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.CreateUserRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.ResetUserPasswordRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.UpdateUserRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.UpdateUserStatusRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Admin kullanıcı yönetimi API servisidir.
 *
 * Backend UsersController controller seviyesinde:
 *
 * [Authorize(Roles = "Admin")]
 *
 * kullandığı için bu endpointlerin tamamı Admin içindir.
 */
interface UsersApi {

    // =========================================================================
    // LIST
    // =========================================================================

    /**
     * Kullanıcıları sayfalı getirir.
     *
     * GET /api/Users
     */
    @GET("api/Users")
    suspend fun getUsers(
        @Query("Page")
        page: Int,

        @Query("PageSize")
        pageSize: Int,

        @Query("Search")
        search: String? = null,

        @Query("Role")
        role: String? = null,

        @Query("IsActive")
        isActive: Boolean? = null
    ): ApiResponse<PagedResult<UserResponseDto>>

    // =========================================================================
    // DETAIL
    // =========================================================================

    /**
     * Tek kullanıcı bilgilerini getirir.
     *
     * GET /api/Users/{id}
     */
    @GET("api/Users/{id}")
    suspend fun getUserById(
        @Path("id")
        userId: Int
    ): ApiResponse<UserResponseDto>

    // =========================================================================
    // CREATE
    // =========================================================================

    /**
     * Yeni kullanıcı oluşturur.
     *
     * POST /api/Users
     *
     * Başarılı HTTP status:
     * 201 Created
     */
    @POST("api/Users")
    suspend fun createUser(
        @Body
        request: CreateUserRequestDto
    ): ApiResponse<UserResponseDto>

    // =========================================================================
    // UPDATE
    // =========================================================================

    /**
     * Kullanıcının temel bilgilerini günceller.
     *
     * PUT /api/Users/{id}
     */
    @PUT("api/Users/{id}")
    suspend fun updateUser(
        @Path("id")
        userId: Int,

        @Body
        request: UpdateUserRequestDto
    ): ApiResponse<UserResponseDto>

    // =========================================================================
    // STATUS
    // =========================================================================

    /**
     * Kullanıcı hesabını aktif veya pasif yapar.
     *
     * PATCH /api/Users/{id}/status
     */
    @PATCH("api/Users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id")
        userId: Int,

        @Body
        request: UpdateUserStatusRequestDto
    ): ApiResponse<UserResponseDto>

    // =========================================================================
    // RESET PASSWORD
    // =========================================================================

    /**
     * Kullanıcının şifresini Admin tarafından sıfırlar.
     *
     * PATCH /api/Users/{id}/reset-password
     */
    @PATCH("api/Users/{id}/reset-password")
    suspend fun resetUserPassword(
        @Path("id")
        userId: Int,

        @Body
        request: ResetUserPasswordRequestDto
    ): ApiResponse<EmptyObjectDto>

    // =========================================================================
    // DELETE
    // =========================================================================

    /**
     * Kullanıcıyı siler.
     *
     * DELETE /api/Users/{id}
     */
    @DELETE("api/Users/{id}")
    suspend fun deleteUser(
        @Path("id")
        userId: Int
    ): ApiResponse<EmptyObjectDto>
}