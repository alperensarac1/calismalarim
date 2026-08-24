package com.alperensarac.projectmanagementkotlin.feature.mailbox.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFilter
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessage
import com.alperensarac.projectmanagementkotlin.domain.usecase.mailbox.GetMailboxMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Mailbox liste ekranının ViewModel'i.
 *
 * Aynı ekran içerisinde:
 *
 * - Gelen Kutusu
 * - Gönderilenler
 *
 * arasında geçiş yapıyoruz.
 */
@OptIn(
    FlowPreview::class,
    ExperimentalCoroutinesApi::class
)
@HiltViewModel
class MailboxListViewModel @Inject constructor(
    private val getMailboxMessagesUseCase:
    GetMailboxMessagesUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            MailboxListUiState()
        )

    val uiState: StateFlow<MailboxListUiState> =
        mutableUiState.asStateFlow()

    /**
     * Tek Paging akışımız.
     *
     * folder veya filter değişirse flatMapLatest eski PagingSource'u
     * iptal eder ve yenisini oluşturur.
     */
    val messages:
            Flow<PagingData<MailboxMessage>> =
        uiState
            .debounce(
                FILTER_DEBOUNCE_MS
            )
            .map { state ->

                MailboxRequestState(
                    folder =
                    state.folder,

                    filter =
                    MailboxFilter(
                        search =
                        state.search
                            .trim()
                            .takeIf {
                                it.isNotBlank()
                            },

                        /*
                         * Sent ekranında IsRead kullanmıyoruz.
                         *
                         * Backend DTO teknik olarak alanı kabul etse de
                         * backend açıklamasına göre Inbox filtresidir.
                         */
                        isRead =
                        if (
                            state.folder ==
                            MailboxFolder.INBOX
                        ) {
                            state.readFilter
                                .toApiValue()
                        } else {
                            null
                        },

                        hasAttachment =
                        state.attachmentFilter
                            .toApiValue()
                    )
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { requestState ->

                getMailboxMessagesUseCase(
                    folder =
                    requestState.folder,

                    filter =
                    requestState.filter
                )
            }
            .cachedIn(
                viewModelScope
            )

    // =========================================================================
    // FOLDER
    // =========================================================================

    fun selectInbox() {

        mutableUiState.update { state ->

            state.copy(
                folder =
                MailboxFolder.INBOX
            )
        }
    }

    fun selectSent() {

        mutableUiState.update { state ->

            state.copy(
                folder =
                MailboxFolder.SENT,

                /*
                 * Sent ekranına geçtiğimizde Inbox'a özel read filtresini
                 * temizliyoruz.
                 */
                readFilter =
                MailboxReadFilter.ALL
            )
        }
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    fun onSearchChanged(
        value: String
    ) {

        mutableUiState.update { state ->

            state.copy(
                search = value
            )
        }
    }

    // =========================================================================
    // READ FILTER
    // =========================================================================

    fun onReadFilterChanged(
        filter: MailboxReadFilter
    ) {

        mutableUiState.update { state ->

            state.copy(
                readFilter = filter
            )
        }
    }

    // =========================================================================
    // ATTACHMENT FILTER
    // =========================================================================

    fun onAttachmentFilterChanged(
        filter: MailboxAttachmentFilter
    ) {

        mutableUiState.update { state ->

            state.copy(
                attachmentFilter = filter
            )
        }
    }

    /**
     * uiState'ten Paging request üretirken kullandığımız internal model.
     */
    private data class MailboxRequestState(
        val folder: MailboxFolder,
        val filter: MailboxFilter
    )

    private companion object {

        const val FILTER_DEBOUNCE_MS =
            350L
    }
}