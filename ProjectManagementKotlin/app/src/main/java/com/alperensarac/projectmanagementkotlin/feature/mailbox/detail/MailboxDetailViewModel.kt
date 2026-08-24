package com.alperensarac.projectmanagementkotlin.feature.mailbox.detail

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.data.download.MailboxAttachmentDownloader
import com.alperensarac.projectmanagementkotlin.data.download.MailboxDownloadResult
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxAttachment
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.DeleteMailboxMessageUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.GetMailboxMessageDetailUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.MarkMailboxMessageAsReadUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.MarkMailboxMessageAsUnreadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mailbox detail ViewModel.
 */
@HiltViewModel
class MailboxDetailViewModel @Inject constructor(
    private val getMailboxMessageDetailUseCase:
    GetMailboxMessageDetailUseCase,

    private val markMailboxMessageAsReadUseCase:
    MarkMailboxMessageAsReadUseCase,

    private val markMailboxMessageAsUnreadUseCase:
    MarkMailboxMessageAsUnreadUseCase,

    private val deleteMailboxMessageUseCase:
    DeleteMailboxMessageUseCase,

    private val mailboxAttachmentDownloader:
    MailboxAttachmentDownloader
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            MailboxDetailUiState()
        )

    val uiState:
            StateFlow<MailboxDetailUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<MailboxDetailUiEvent>(
            Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    // =========================================================================
    // LOAD
    // =========================================================================

    fun loadMessage(
        messageId: Int,
        markAsRead: Boolean
    ) {

        if (
            messageId <= 0 ||
            mutableUiState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update { state ->

                state.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            when (
                val result =
                    getMailboxMessageDetailUseCase(
                        messageId = messageId,
                        markAsRead = markAsRead
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isLoading = false,
                            message = result.data,
                            errorMessage = null
                        )
                    }

                    /*
                     * Inbox detail endpointini markAsRead=true ile çağırdıysak
                     * backend liste durumunu değiştirmiş olabilir.
                     */
                    if (markAsRead) {

                        eventChannel.send(
                            MailboxDetailUiEvent
                                .InboxMessageMarkedAsRead
                        )
                    }
                }

                is AppResult.Error -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isLoading = false,

                            errorMessage =
                            result.error
                                .toUserMessage()
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // MARK READ
    // =========================================================================

    fun markAsRead() {

        val message =
            mutableUiState.value.message
                ?: return

        if (
            mutableUiState.value
                .isOperationRunning
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isOperationRunning = true
                )
            }

            when (
                val result =
                    markMailboxMessageAsReadUseCase(
                        message.id
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isOperationRunning = false,

                            message =
                            state.message
                                ?.copy(
                                    isRead = true
                                )
                        )
                    }

                    sendMessage(
                        result.message
                            ?: "Mesaj okundu olarak işaretlendi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            isOperationRunning = false
                        )
                    }

                    sendMessage(
                        result.error
                            .toUserMessage()
                    )
                }
            }
        }
    }

    // =========================================================================
    // MARK UNREAD
    // =========================================================================

    fun markAsUnread() {

        val message =
            mutableUiState.value.message
                ?: return

        if (
            mutableUiState.value
                .isOperationRunning
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isOperationRunning = true
                )
            }

            when (
                val result =
                    markMailboxMessageAsUnreadUseCase(
                        message.id
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update { state ->

                        state.copy(
                            isOperationRunning = false,

                            message =
                            state.message
                                ?.copy(
                                    isRead = false,
                                    readAtUtc = null
                                )
                        )
                    }

                    sendMessage(
                        result.message
                            ?: "Mesaj okunmadı olarak işaretlendi."
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            isOperationRunning = false
                        )
                    }

                    sendMessage(
                        result.error
                            .toUserMessage()
                    )
                }
            }
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    fun deleteMessage() {

        val message =
            mutableUiState.value.message
                ?: return

        if (
            mutableUiState.value
                .isOperationRunning
        ) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {
                it.copy(
                    isOperationRunning = true
                )
            }

            when (
                val result =
                    deleteMailboxMessageUseCase(
                        message.id
                    )
            ) {

                is AppResult.Success -> {

                    mutableUiState.update {
                        it.copy(
                            isOperationRunning = false
                        )
                    }

                    eventChannel.send(
                        MailboxDetailUiEvent
                            .MessageDeleted(
                                message =
                                result.message
                                    ?: "Mesaj kutunuzdan kaldırıldı."
                            )
                    )
                }

                is AppResult.Error -> {

                    mutableUiState.update {
                        it.copy(
                            isOperationRunning = false
                        )
                    }

                    sendMessage(
                        result.error
                            .toUserMessage()
                    )
                }
            }
        }
    }

    // =========================================================================
    // DOWNLOAD
    // =========================================================================

    /**
     * Attachment'ı kullanıcının SAF ile seçtiği destination Uri'ye indirir.
     */
    fun downloadAttachment(
        attachment: MailboxAttachment,
        destinationUri: Uri,
        contentResolver: ContentResolver
    ) {

        val state =
            mutableUiState.value

        /*
         * Aynı anda iki dosya indirmiyoruz.
         */
        if (state.isDownloading) {
            return
        }

        if (!attachment.isAvailable) {

            sendMessage(
                "Bu dosya artık indirilemiyor."
            )

            return
        }

        viewModelScope.launch {

            mutableUiState.update {

                it.copy(
                    downloadingAttachmentId =
                    attachment.id,

                    downloadProgress =
                    0
                )
            }

            val result =
                mailboxAttachmentDownloader
                    .download(
                        messageId =
                        attachment.messageId,

                        attachmentId =
                        attachment.id,

                        destinationUri =
                        destinationUri,

                        contentResolver =
                        contentResolver,

                        onProgress = { progress ->

                            mutableUiState.update {

                                it.copy(
                                    downloadProgress =
                                    progress
                                )
                            }
                        }
                    )

            when (result) {

                MailboxDownloadResult.Success -> {

                    mutableUiState.update {

                        it.copy(
                            downloadingAttachmentId =
                            null,

                            downloadProgress =
                            null
                        )
                    }

                    eventChannel.send(
                        MailboxDetailUiEvent
                            .AttachmentDownloaded(
                                fileName =
                                attachment
                                    .originalFileName
                            )
                    )
                }

                is MailboxDownloadResult.Error -> {

                    mutableUiState.update {

                        it.copy(
                            downloadingAttachmentId =
                            null,

                            downloadProgress =
                            null
                        )
                    }

                    sendMessage(
                        result.message
                    )
                }
            }
        }
    }

    // =========================================================================
    // EVENT
    // =========================================================================

    private fun sendMessage(
        message: String
    ) {

        viewModelScope.launch {

            eventChannel.send(
                MailboxDetailUiEvent
                    .ShowMessage(
                        message
                    )
            )
        }
    }
}