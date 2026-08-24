package com.alperensarac.projectmanagementkotlin.data.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.comments.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.CommentsApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.comments.CreateCommentRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.comments.UpdateCommentRequestDto
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskCommentsRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TaskCommentsRepository Retrofit implementasyonudur.
 */
@Singleton
class TaskCommentsRepositoryImpl @Inject constructor(
    private val commentsApi: CommentsApi,
    private val networkErrorMapper: NetworkErrorMapper
) : TaskCommentsRepository {

    override suspend fun getComments(
        taskId: Int
    ): AppResult<List<TaskComment>> {

        return try {

            val response =
                commentsApi.getComments(
                    taskId = taskId
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Görev yorumları alınamadı."
                    )
                )
            }

            AppResult.Success(
                data =
                data.map { dto ->
                    dto.toDomain()
                },
                message =
                response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    override suspend fun createComment(
        taskId: Int,
        content: String
    ): AppResult<TaskComment> {

        return try {

            val response =
                commentsApi.createComment(
                    taskId = taskId,
                    request =
                    CreateCommentRequestDto(
                        content = content
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Yorum eklenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    override suspend fun updateComment(
        taskId: Int,
        commentId: Int,
        content: String
    ): AppResult<TaskComment> {

        return try {

            val response =
                commentsApi.updateComment(
                    taskId = taskId,
                    commentId = commentId,
                    request =
                    UpdateCommentRequestDto(
                        content = content
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Yorum güncellenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    override suspend fun deleteComment(
        taskId: Int,
        commentId: Int
    ): AppResult<Unit> {

        return try {

            val response =
                commentsApi.deleteComment(
                    taskId = taskId,
                    commentId = commentId
                )

            if (!response.success) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Yorum silinemedi."
                    )
                )
            }

            AppResult.Success(
                data = Unit,
                message = response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    private fun createBusinessError(
        message: String,
        errors: Map<String, List<String>>?,
        fallbackMessage: String
    ): NetworkError {

        if (!errors.isNullOrEmpty()) {

            val combinedMessage =
                errors.values
                    .flatten()
                    .joinToString("\n")
                    .ifBlank {
                        message.ifBlank {
                            fallbackMessage
                        }
                    }

            return NetworkError.Validation(
                message = combinedMessage,
                fieldErrors = errors
            )
        }

        return NetworkError.Unknown(
            message =
            message.ifBlank {
                fallbackMessage
            }
        )
    }
}