package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.core.network.model.PagedResult
import com.alperensarac.projectmanagementkotlin.data.remote.dto.common.EmptyObjectDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxMessageDetailDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.mailbox.MailboxMessageListItemDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * ASP.NET Core MailboxController Retrofit karşılığı.
 */
interface MailboxApi {

    // =========================================================================
    // SEND
    // =========================================================================

    /**
     * POST /api/Mailbox/messages
     *
     * Backend:
     *
     * [Consumes("multipart/form-data")]
     *
     * SendMailboxMessageRequest:
     *
     * RecipientUserIds
     * Subject
     * Body
     * Attachments
     *
     * Multipart parçalarını manuel oluşturuyoruz.
     *
     * Bunun iki önemli nedeni vardır:
     *
     * 1. RecipientUserIds aynı field adıyla birden fazla kez gönderilebilir.
     *
     * 2. Attachment RequestBody'lerini streaming olarak oluşturabiliyoruz.
     */
    @Multipart
    @POST("api/Mailbox/messages")
    suspend fun sendMessage(
        @Part
        parts: List<MultipartBody.Part>
    ): ApiResponse<MailboxMessageDetailDto>

    // =========================================================================
    // INBOX
    // =========================================================================

    @GET("api/Mailbox/inbox")
    suspend fun getInbox(

        @Query("Page")
        page: Int,

        @Query("PageSize")
        pageSize: Int,

        @Query("Search")
        search: String? = null,

        @Query("IsRead")
        isRead: Boolean? = null,

        @Query("HasAttachment")
        hasAttachment: Boolean? = null
    ): ApiResponse<PagedResult<MailboxMessageListItemDto>>

    // =========================================================================
    // SENT
    // =========================================================================

    @GET("api/Mailbox/sent")
    suspend fun getSent(

        @Query("Page")
        page: Int,

        @Query("PageSize")
        pageSize: Int,

        @Query("Search")
        search: String? = null,

        @Query("IsRead")
        isRead: Boolean? = null,

        @Query("HasAttachment")
        hasAttachment: Boolean? = null
    ): ApiResponse<PagedResult<MailboxMessageListItemDto>>

    // =========================================================================
    // DETAIL
    // =========================================================================

    @GET("api/Mailbox/messages/{messageId}")
    suspend fun getMessageById(

        @Path("messageId")
        messageId: Int,

        @Query("markAsRead")
        markAsRead: Boolean =
            true
    ): ApiResponse<MailboxMessageDetailDto>

    // =========================================================================
    // READ
    // =========================================================================

    @PATCH("api/Mailbox/messages/{messageId}/read")
    suspend fun markAsRead(

        @Path("messageId")
        messageId: Int
    ): ApiResponse<EmptyObjectDto>

    // =========================================================================
    // UNREAD
    // =========================================================================

    @PATCH("api/Mailbox/messages/{messageId}/unread")
    suspend fun markAsUnread(

        @Path("messageId")
        messageId: Int
    ): ApiResponse<EmptyObjectDto>

    // =========================================================================
    // DELETE
    // =========================================================================

    @DELETE("api/Mailbox/messages/{messageId}")
    suspend fun deleteMessage(

        @Path("messageId")
        messageId: Int
    ): ApiResponse<EmptyObjectDto>

    // =========================================================================
    // ATTACHMENT DOWNLOAD
    // =========================================================================

    /**
     * Büyük attachment'ları belleğe almamak için @Streaming zorunludur.
     */
    @Streaming
    @GET(
        "api/Mailbox/messages/{messageId}/attachments/{attachmentId}/download"
    )
    suspend fun downloadAttachment(

        @Path("messageId")
        messageId: Int,

        @Path("attachmentId")
        attachmentId: Int
    ): Response<ResponseBody>
}