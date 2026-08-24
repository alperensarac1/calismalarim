package com.alperensarac.projectmanagementkotlin.data.remote.api

import com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse
import com.alperensarac.projectmanagementkotlin.data.remote.dto.comments.CommentResponseDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.comments.CreateCommentRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.comments.UpdateCommentRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.common.EmptyObjectDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Görev yorum endpointleri.
 *
 * Backend route:
 *
 * api/tasks/{taskId}/comments
 */
interface CommentsApi {

    /**
     * Görev yorumlarını getirir.
     */
    @GET("api/tasks/{taskId}/comments")
    suspend fun getComments(
        @Path("taskId")
        taskId: Int
    ): ApiResponse<List<CommentResponseDto>>

    /**
     * Yeni yorum ekler.
     */
    @POST("api/tasks/{taskId}/comments")
    suspend fun createComment(
        @Path("taskId")
        taskId: Int,

        @Body
        request: CreateCommentRequestDto
    ): ApiResponse<CommentResponseDto>

    /**
     * Mevcut yorumu günceller.
     *
     * PUT /api/tasks/{taskId}/comments/{commentId}
     */
    @PUT("api/tasks/{taskId}/comments/{commentId}")
    suspend fun updateComment(
        @Path("taskId")
        taskId: Int,

        @Path("commentId")
        commentId: Int,

        @Body
        request: UpdateCommentRequestDto
    ): ApiResponse<CommentResponseDto>

    /**
     * Yorumu siler.
     */
    @DELETE("api/tasks/{taskId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("taskId")
        taskId: Int,

        @Path("commentId")
        commentId: Int
    ): ApiResponse<EmptyObjectDto>
}