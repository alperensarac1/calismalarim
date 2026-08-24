package com.alperensarac.projectmanagementkotlin.domain.repository

import android.content.ContentResolver
import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFilter
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessage
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessageDetail
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxUploadProgress
import kotlinx.coroutines.flow.Flow

/**
 * Mailbox repository sözleşmesi.
 *
 * NOT:
 * ContentResolver burada Android bağımlılığı oluşturuyor.
 *
 * İleride mimariyi Android-framework bağımsızlaştırmak istersek upload
 * tarafını ayrı MailboxUploadRepository interface'ine taşıyabiliriz.
 *
 * Şimdilik mevcut Android-only uygulamada sade ve anlaşılır ilerliyoruz.
 */
interface MailboxRepository {

    // -------------------------------------------------------------------------
    // LIST
    // -------------------------------------------------------------------------

    fun getMessages(
        folder: MailboxFolder,
        filter: MailboxFilter
    ): Flow<PagingData<MailboxMessage>>

    // -------------------------------------------------------------------------
    // DETAIL
    // -------------------------------------------------------------------------

    suspend fun getMessageById(
        messageId: Int,
        markAsRead: Boolean
    ): AppResult<MailboxMessageDetail>

    // -------------------------------------------------------------------------
    // SEND
    // -------------------------------------------------------------------------

    suspend fun sendMessage(
        contentResolver: ContentResolver,
        recipientUserIds: List<Int>,
        subject: String,
        body: String,
        attachments: List<MailboxSelectedFile>,
        onUploadProgress: (MailboxUploadProgress) -> Unit
    ): AppResult<MailboxMessageDetail>

    // -------------------------------------------------------------------------
    // READ / UNREAD
    // -------------------------------------------------------------------------

    suspend fun markAsRead(
        messageId: Int
    ): AppResult<Unit>

    suspend fun markAsUnread(
        messageId: Int
    ): AppResult<Unit>

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    suspend fun deleteMessage(
        messageId: Int
    ): AppResult<Unit>
}