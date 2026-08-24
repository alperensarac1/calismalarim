package com.alperensarac.projectmanagementkotlin.feature.mailbox.compose

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRules
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.SearchMailboxRecipientsUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.SendMailboxMessageUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.ValidateMailboxAttachmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MailboxComposeViewModel @Inject constructor(
    private val searchMailboxRecipientsUseCase:
    SearchMailboxRecipientsUseCase,

    private val sendMailboxMessageUseCase:
    SendMailboxMessageUseCase,

    private val validateMailboxAttachmentsUseCase:
    ValidateMailboxAttachmentsUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            MailboxComposeUiState()
        )

    val uiState:
            StateFlow<MailboxComposeUiState> =
        mutableUiState.asStateFlow()

    private val eventChannel =
        Channel<MailboxComposeUiEvent>(
            Channel.BUFFERED
        )

    val events =
        eventChannel.receiveAsFlow()

    private var searchJob: Job? =
        null
    private var replyInitialized =
        false
    // =========================================================================
    // RECIPIENT SEARCH
    // =========================================================================

    fun onRecipientSearchChanged(
        value: String
    ) {

        val normalized =
            value.take(
                MAXIMUM_SEARCH_LENGTH
            )

        mutableUiState.update {

            it.copy(
                recipientSearch =
                normalized
            )
        }

        searchJob?.cancel()

        searchJob =
            viewModelScope.launch {

                delay(
                    SEARCH_DEBOUNCE_MS
                )

                searchRecipients(
                    normalized
                )
            }
    }

    private suspend fun searchRecipients(
        search: String
    ) {

        mutableUiState.update {

            it.copy(
                isSearchingRecipients =
                true,

                recipientSearchError =
                null
            )
        }

        when (
            val result =
                searchMailboxRecipientsUseCase(
                    search
                )
        ) {

            is AppResult.Success -> {

                val selectedIds =
                    mutableUiState
                        .value
                        .selectedRecipients
                        .map {
                            it.id
                        }
                        .toSet()

                mutableUiState.update {

                    it.copy(
                        isSearchingRecipients =
                        false,

                        /*
                         * Seçilmiş kullanıcıları sonuç listesinden
                         * çıkarıyoruz. Aynı kişi ikinci kez seçilemez.
                         */
                        recipientResults =
                        result.data.filter { user ->
                            user.id !in selectedIds
                        },

                        recipientSearchError =
                        null
                    )
                }
            }

            is AppResult.Error -> {

                mutableUiState.update {

                    it.copy(
                        isSearchingRecipients =
                        false,

                        recipientResults =
                        emptyList(),

                        recipientSearchError =
                        result.error
                            .toUserMessage()
                    )
                }
            }
        }
    }

    // =========================================================================
    // RECIPIENT SELECTION
    // =========================================================================

    fun selectRecipient(
        user: MailboxRecipientUser
    ) {

        val state =
            mutableUiState.value

        if (
            user.id <= 0 ||
            !user.isActive
        ) {
            return
        }

        if (
            state.selectedRecipients
                .any {
                    it.id == user.id
                }
        ) {

            sendMessage(
                "Bu kullanıcı zaten alıcı listesinde."
            )

            return
        }

        if (
            state.selectedRecipients.size >=
            MailboxRules.MAXIMUM_RECIPIENT_COUNT
        ) {

            sendMessage(
                "Bir mesaj en fazla 50 kullanıcıya gönderilebilir."
            )

            return
        }

        mutableUiState.update {

            it.copy(
                selectedRecipients =
                it.selectedRecipients +
                        user,

                recipientResults =
                it.recipientResults
                    .filterNot { result ->
                        result.id == user.id
                    }
            )
        }
    }

    fun removeRecipient(
        user: MailboxRecipientUser
    ) {

        mutableUiState.update {

            it.copy(
                selectedRecipients =
                it.selectedRecipients
                    .filterNot { selected ->
                        selected.id ==
                                user.id
                    }
            )
        }
    }

    // =========================================================================
    // SUBJECT / BODY
    // =========================================================================

    fun onSubjectChanged(
        value: String
    ) {

        mutableUiState.update {

            it.copy(
                subject =
                value.take(
                    MailboxRules
                        .MAXIMUM_SUBJECT_LENGTH
                )
            )
        }
    }

    fun onBodyChanged(
        value: String
    ) {

        mutableUiState.update {

            it.copy(
                body =
                value.take(
                    MailboxRules
                        .MAXIMUM_BODY_LENGTH
                )
            )
        }
    }

    // =========================================================================
// REPLY INITIALIZATION
// =========================================================================

    /**
     * Mesaj detay ekranından "Cevapla" ile gelindiğinde Compose state'ini
     * hazırlar.
     *
     * Burada MailboxRecipientUser nesnesini elle oluşturmuyoruz.
     *
     * Bunun sebebi:
     *
     * - aktiflik bilgisi
     * - backend'in güncel kullanıcı bilgisi
     * - domain modelinin doğruluğu
     *
     * SearchMailboxRecipientsUseCase üzerinden tekrar doğrulanmış olur.
     */
    fun initializeReply(
        senderId: Int,
        senderEmail: String?,
        originalSubject: String?
    ) {

        if (replyInitialized) {
            return
        }

        val normalizedEmail =
            senderEmail
                ?.trim()
                .orEmpty()

        if (
            senderId <= 0 ||
            normalizedEmail.isBlank()
        ) {
            return
        }

        /*
         * Aynı ViewModel instance'ında yalnızca bir kez çalışsın.
         */
        replyInitialized =
            true

        viewModelScope.launch {

            // ---------------------------------------------------------------------
            // SUBJECT
            // ---------------------------------------------------------------------

            val replySubject =
                createReplySubject(
                    originalSubject =
                    originalSubject
                        .orEmpty()
                )

            mutableUiState.update { state ->

                state.copy(
                    subject =
                    if (
                        state.subject.isBlank()
                    ) {
                        replySubject
                    } else {
                        state.subject
                    }
                )
            }

            // ---------------------------------------------------------------------
            // RECIPIENT
            // ---------------------------------------------------------------------

            /*
             * E-posta ile arıyoruz.
             *
             * Ardından sonuçtan önce ID ile eşleştiriyoruz.
             * Böylece aynı mail benzeri bir durum olsa bile gerçek kullanıcı
             * id'si esas alınır.
             */
            when (
                val result =
                    searchMailboxRecipientsUseCase(
                        normalizedEmail
                    )
            ) {

                is AppResult.Success -> {

                    val recipient =
                        result.data
                            .firstOrNull { user ->

                                user.id ==
                                        senderId

                            }
                            ?: result.data
                                .firstOrNull { user ->

                                    user.email.equals(
                                        normalizedEmail,
                                        ignoreCase = true
                                    )
                                }

                    if (
                        recipient == null
                    ) {

                        sendMessageInternal(
                            "Mesajı gönderen kullanıcı alıcı listesinde bulunamadı."
                        )

                        return@launch
                    }

                    if (
                        !recipient.isActive
                    ) {

                        sendMessageInternal(
                            "Mesajı gönderen kullanıcı artık aktif değil."
                        )

                        return@launch
                    }

                    mutableUiState.update { state ->

                        /*
                         * Orientation/process UI tekrarlarında duplicate
                         * recipient oluşmasını önlüyoruz.
                         */
                        if (
                            state.selectedRecipients.any {
                                it.id == recipient.id
                            }
                        ) {

                            state

                        } else {

                            state.copy(
                                selectedRecipients =
                                state.selectedRecipients +
                                        recipient,

                                recipientResults =
                                state.recipientResults
                                    .filterNot {
                                        it.id ==
                                                recipient.id
                                    }
                            )
                        }
                    }
                }

                is AppResult.Error -> {

                    sendMessageInternal(
                        result.error
                            .toUserMessage()
                    )
                }
            }
        }
    }

    /**
     * Reply konusunu üretir.
     *
     * Aynı mesaja tekrar tekrar cevap verilirse:
     *
     * Ynt: Konu
     *
     * değerinin:
     *
     * Ynt: Ynt: Konu
     *
     * haline gelmesini engelleriz.
     */
    private fun createReplySubject(
        originalSubject: String
    ): String {

        val normalized =
            originalSubject
                .trim()
                .ifBlank {
                    "Konusuz mesaj"
                }

        if (
            normalized.startsWith(
                REPLY_SUBJECT_PREFIX,
                ignoreCase = true
            ) ||
            normalized.startsWith(
                ENGLISH_REPLY_SUBJECT_PREFIX,
                ignoreCase = true
            )
        ) {

            return normalized
                .take(
                    MailboxRules
                        .MAXIMUM_SUBJECT_LENGTH
                )
        }

        return "$REPLY_SUBJECT_PREFIX $normalized"
            .take(
                MailboxRules
                    .MAXIMUM_SUBJECT_LENGTH
            )
    }

    // =========================================================================
    // ATTACHMENTS
    // =========================================================================

    fun addAttachments(
        files: List<MailboxSelectedFile>
    ) {

        if (files.isEmpty()) {
            return
        }

        val current =
            mutableUiState
                .value
                .attachments

        /*
         * Aynı URI iki kere seçilmişse ikinci kez eklemiyoruz.
         */
        val merged =
            (
                    current +
                            files
                    )
                .distinctBy {
                    it.uri
                }

        val validation =
            validateMailboxAttachmentsUseCase(
                merged
            )

        val error =
            validation.exceptionOrNull()

        if (error != null) {

            sendMessage(
                error.message
                    ?: "Dosyalar eklenemedi."
            )

            return
        }

        mutableUiState.update {

            it.copy(
                attachments =
                merged
            )
        }
    }

    fun removeAttachment(
        file: MailboxSelectedFile
    ) {

        if (
            mutableUiState.value
                .isSending
        ) {
            return
        }

        mutableUiState.update {

            it.copy(
                attachments =
                it.attachments
                    .filterNot { attachment ->
                        attachment.uri ==
                                file.uri
                    }
            )
        }
    }

    // =========================================================================
    // SEND
    // =========================================================================

    fun send(
        contentResolver: ContentResolver
    ) {

        val state =
            mutableUiState.value

        if (state.isSending) {
            return
        }

        viewModelScope.launch {

            mutableUiState.update {

                it.copy(
                    isSending =
                    true,

                    uploadProgress =
                    0
                )
            }

            try {

                val result =
                    sendMailboxMessageUseCase(
                        contentResolver =
                        contentResolver,

                        recipientUserIds =
                        state.selectedRecipients
                            .map {
                                it.id
                            },

                        subject =
                        state.subject,

                        body =
                        state.body,

                        attachments =
                        state.attachments,

                        onUploadProgress = { progress ->

                            mutableUiState.update {

                                it.copy(
                                    uploadProgress =
                                    progress.percentage
                                )
                            }
                        }
                    )

                when (result) {

                    is AppResult.Success -> {

                        mutableUiState.update {

                            it.copy(
                                isSending =
                                false,

                                uploadProgress =
                                100
                            )
                        }

                        eventChannel.send(
                            MailboxComposeUiEvent
                                .MessageSent(
                                    message =
                                    result.message
                                        ?: "Mesaj başarıyla gönderildi."
                                )
                        )
                    }

                    is AppResult.Error -> {

                        mutableUiState.update {

                            it.copy(
                                isSending =
                                false
                            )
                        }

                        sendMessageInternal(
                            result.error
                                .toUserMessage()
                        )
                    }
                }

            } catch (
                exception: IllegalArgumentException
            ) {

                mutableUiState.update {

                    it.copy(
                        isSending =
                        false
                    )
                }

                sendMessageInternal(
                    exception.message
                        ?: "Mesaj bilgileri geçersiz."
                )

            } catch (
                throwable: Throwable
            ) {

                mutableUiState.update {

                    it.copy(
                        isSending =
                        false
                    )
                }

                sendMessageInternal(
                    throwable.message
                        ?: "Mesaj gönderilemedi."
                )
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

            sendMessageInternal(
                message
            )
        }
    }

    private suspend fun sendMessageInternal(
        message: String
    ) {

        eventChannel.send(
            MailboxComposeUiEvent
                .ShowMessage(
                    message
                )
        )
    }

    private companion object {

        const val SEARCH_DEBOUNCE_MS =
            350L

        const val MAXIMUM_SEARCH_LENGTH =
            250

        const val REPLY_SUBJECT_PREFIX =
            "Ynt:"

        const val ENGLISH_REPLY_SUBJECT_PREFIX =
            "Re:"
    }
}