package com.alperensarac.projectmanagementkotlin.data.repository

import android.content.ContentResolver
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.mailbox.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.MailboxApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.common.EmptyObjectDto
import com.alperensarac.projectmanagementkotlin.data.remote.paging.MailboxPagingSource
import com.alperensarac.projectmanagementkotlin.data.upload.MailboxMultipartBuilder
import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFilter
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessage
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessageDetail
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxUploadProgress
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class MailboxRepositoryImpl @Inject constructor(
    private val mailboxApi: MailboxApi,
    private val mailboxMultipartBuilder:
    MailboxMultipartBuilder,
    private val networkErrorMapper:
    NetworkErrorMapper
) : MailboxRepository {

    // =========================================================================
    // LIST
    // =========================================================================

    override fun getMessages(
        folder: MailboxFolder,
        filter: MailboxFilter
    ): Flow<PagingData<MailboxMessage>> {

        return Pager(
            config =
            PagingConfig(
                pageSize =
                PAGE_SIZE,

                initialLoadSize =
                PAGE_SIZE,

                prefetchDistance =
                PREFETCH_DISTANCE,

                enablePlaceholders =
                false
            ),

            pagingSourceFactory = {

                MailboxPagingSource(
                    mailboxApi =
                    mailboxApi,

                    folder =
                    folder,

                    filter =
                    filter
                )
            }
        ).flow
    }

    // =========================================================================
    // DETAIL
    // =========================================================================

    override suspend fun getMessageById(
        messageId: Int,
        markAsRead: Boolean
    ): AppResult<MailboxMessageDetail> {

        return try {

            val response =
                mailboxApi.getMessageById(
                    messageId =
                    messageId,

                    markAsRead =
                    markAsRead
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message =
                        response.message,

                        errors =
                        response.errors,

                        fallbackMessage =
                        "Mesaj ayrıntıları getirilemedi."
                    )
                )
            }

            AppResult.Success(
                data =
                data.toDomain(),

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

    // =========================================================================
    // SEND
    // =========================================================================

    override suspend fun sendMessage(
        contentResolver: ContentResolver,
        recipientUserIds: List<Int>,
        subject: String,
        body: String,
        attachments: List<MailboxSelectedFile>,
        onUploadProgress: (MailboxUploadProgress) -> Unit
    ): AppResult<MailboxMessageDetail> {

        return try {

            val parts =
                mailboxMultipartBuilder
                    .build(
                        contentResolver =
                        contentResolver,

                        recipientUserIds =
                        recipientUserIds,

                        subject =
                        subject,

                        body =
                        body,

                        attachments =
                        attachments,

                        onProgress =
                        onUploadProgress
                    )

            val response =
                mailboxApi.sendMessage(
                    parts =
                    parts
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message =
                        response.message,

                        errors =
                        response.errors,

                        fallbackMessage =
                        "Mesaj gönderilemedi."
                    )
                )
            }

            AppResult.Success(
                data =
                data.toDomain(),

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

    // =========================================================================
    // READ
    // =========================================================================

    override suspend fun markAsRead(
        messageId: Int
    ): AppResult<Unit> {

        return executeUnitRequest(
            fallbackMessage =
            "Mesaj okundu olarak işaretlenemedi."
        ) {

            mailboxApi.markAsRead(
                messageId
            )
        }
    }

    // =========================================================================
    // UNREAD
    // =========================================================================

    override suspend fun markAsUnread(
        messageId: Int
    ): AppResult<Unit> {

        return executeUnitRequest(
            fallbackMessage =
            "Mesaj okunmadı olarak işaretlenemedi."
        ) {

            mailboxApi.markAsUnread(
                messageId
            )
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    override suspend fun deleteMessage(
        messageId: Int
    ): AppResult<Unit> {

        return executeUnitRequest(
            fallbackMessage =
            "Mesaj kutunuzdan kaldırılamadı."
        ) {

            mailboxApi.deleteMessage(
                messageId
            )
        }
    }

    // =========================================================================
    // COMMON UNIT REQUEST
    // =========================================================================

    private suspend fun executeUnitRequest(
        fallbackMessage: String,
        request:
        suspend () ->
        com.alperensarac.projectmanagementkotlin.core.network.model.ApiResponse<
                EmptyObjectDto
                >
    ): AppResult<Unit> {

        return try {

            val response =
                request()

            if (!response.success) {

                return AppResult.Error(
                    createBusinessError(
                        message =
                        response.message,

                        errors =
                        response.errors,

                        fallbackMessage =
                        fallbackMessage
                    )
                )
            }

            AppResult.Success(
                data =
                Unit,

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

    // =========================================================================
    // BUSINESS ERROR
    // =========================================================================

    private fun createBusinessError(
        message: String,
        errors: Map<String, List<String>>?,
        fallbackMessage: String
    ): NetworkError {

        if (
            !errors.isNullOrEmpty()
        ) {

            return NetworkError.Validation(
                message =
                errors.values
                    .flatten()
                    .joinToString(
                        separator = "\n"
                    )
                    .ifBlank {
                        message.ifBlank {
                            fallbackMessage
                        }
                    },

                fieldErrors =
                errors
            )
        }

        return NetworkError.Unknown(
            message =
            message.ifBlank {
                fallbackMessage
            }
        )
    }

    private companion object {

        const val PAGE_SIZE =
            20

        const val PREFETCH_DISTANCE =
            5
    }
}