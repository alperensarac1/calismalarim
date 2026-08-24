package com.alperensarac.projectmanagementkotlin.feature.mailbox.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.FragmentMailboxDetailBinding
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxAttachment
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessageDetail
import com.alperensarac.projectmanagementkotlin.feature.mailbox.MailboxNavigationResult
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MailboxDetailFragment : Fragment() {

    private var _binding:
            FragmentMailboxDetailBinding? =
        null

    private val binding:
            FragmentMailboxDetailBinding
        get() =
            checkNotNull(_binding)

    private val viewModel:
            MailboxDetailViewModel
            by viewModels()

    @Inject
    lateinit var dateTimeFormatter:
            DateTimeFormatter

    private lateinit var attachmentAdapter:
            MailboxAttachmentAdapter

    private val messageId: Int
        get() =
            requireArguments()
                .getInt(
                    ARG_MESSAGE_ID,
                    INVALID_ID
                )

    private val sourceFolder:
            MailboxFolder
        get() {

            val value =
                requireArguments()
                    .getString(
                        ARG_SOURCE_FOLDER
                    )

            return runCatching {

                MailboxFolder.valueOf(
                    value.orEmpty()
                )

            }.getOrDefault(
                MailboxFolder.INBOX
            )
        }
    private var latestUiState =
        MailboxDetailUiState()
    private var pendingDownloadAttachment:
            MailboxAttachment? =
        null

    private val createDocumentLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/octet-stream"
            )
        ) { uri ->

            val attachment =
                pendingDownloadAttachment

            pendingDownloadAttachment =
                null

            if (
                uri == null ||
                attachment == null
            ) {
                return@registerForActivityResult
            }

            viewModel.downloadAttachment(
                attachment = attachment,
                destinationUri = uri,
                contentResolver =
                requireContext()
                    .contentResolver
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentMailboxDetailBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        configureAttachments()

        configureListeners()

        observeState()

        observeEvents()

        if (
            savedInstanceState ==
            null
        ) {

            viewModel.loadMessage(
                messageId =
                messageId,

                /*
                 * Inbox mesajı açılırsa backend'in mevcut
                 * markAsRead=true davranışını kullanıyoruz.
                 *
                 * Sent mesajında okundu state'ini değiştirmiyoruz.
                 */
                markAsRead =
                sourceFolder ==
                        MailboxFolder.INBOX
            )
        }
    }

    // =========================================================================
    // ATTACHMENTS
    // =========================================================================

    private fun configureAttachments() {

        attachmentAdapter =
            MailboxAttachmentAdapter(

                downloadingAttachmentId = {
                    latestUiState
                        .downloadingAttachmentId
                },

                downloadProgress = {
                    latestUiState
                        .downloadProgress
                },

                onAttachmentClicked = { attachment ->

                    onAttachmentClicked(
                        attachment
                    )
                }
            )

        binding.recyclerViewMailboxAttachments
            .apply {

                layoutManager =
                    LinearLayoutManager(
                        requireContext()
                    )

                adapter =
                    attachmentAdapter

                isNestedScrollingEnabled =
                    false
            }
    }

    private fun onAttachmentClicked(
        attachment: MailboxAttachment
    ) {

        if (!attachment.isAvailable) {

            Snackbar.make(
                binding.root,
                R.string.mailbox_attachment_unavailable,
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }

        pendingDownloadAttachment =
            attachment

        /*
         * Android kullanıcıya:
         *
         * "Bu dosyayı nereye kaydetmek istiyorsun?"
         *
         * ekranını açar.
         */
        createDocumentLauncher.launch(
            attachment.originalFileName
        )
    }

    // =========================================================================
    // LISTENERS
    // =========================================================================

    private fun configureListeners() {

        binding.toolbarMailboxDetail
            .setNavigationOnClickListener {

                findNavController()
                    .navigateUp()
            }
        binding.buttonMailboxReply
            .setOnClickListener {

                openReplyComposer()
            }

        binding.buttonMailboxDetailRetry
            .setOnClickListener {

                viewModel.loadMessage(
                    messageId =
                    messageId,

                    markAsRead =
                    sourceFolder ==
                            MailboxFolder.INBOX
                )
            }

        binding.buttonMailboxMarkRead
            .setOnClickListener {

                viewModel.markAsRead()
            }

        binding.buttonMailboxMarkUnread
            .setOnClickListener {

                viewModel.markAsUnread()
            }

        binding.buttonMailboxDelete
            .setOnClickListener {

                showDeleteDialog()
            }
    }

    private fun showDeleteDialog() {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                R.string.mailbox_delete_title
            )
            .setMessage(
                R.string.mailbox_delete_message
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.action_delete
            ) { _, _ ->

                viewModel.deleteMessage()
            }
            .show()
    }

    // =========================================================================
    // OBSERVERS
    // =========================================================================

    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.uiState
                    .collect { state ->

                        renderState(
                            state
                        )
                    }
            }
        }
    }

    private fun observeEvents() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.events
                    .collect { event ->

                        when (event) {

                            is MailboxDetailUiEvent
                            .ShowMessage -> {

                                Snackbar.make(
                                    binding.root,
                                    event.message,
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }

                            is MailboxDetailUiEvent
                            .MessageDeleted -> {

                                notifyMailboxChanged()

                                findNavController()
                                    .navigateUp()
                            }
                            is MailboxDetailUiEvent
                                .InboxMessageMarkedAsRead -> {

                                notifyMailboxChanged()
                            }
                            is MailboxDetailUiEvent.AttachmentDownloaded -> {

                                Snackbar.make(
                                    binding.root,
                                    getString(
                                        R.string.mailbox_attachment_downloaded,
                                        event.fileName
                                    ),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
            }
        }
    }

    // =========================================================================
    // RENDER
    // =========================================================================

    private fun renderState(
        state: MailboxDetailUiState
    ) {

        binding.progressIndicatorMailboxDetail
            .isVisible =
            state.isLoading &&
                    !state.hasContent

        binding.scrollViewMailboxDetail
            .isVisible =
            state.hasContent

        binding.layoutMailboxDetailError
            .isVisible =
            !state.hasContent &&
                    !state.errorMessage
                        .isNullOrBlank()

        binding.textViewMailboxDetailError.text =
            state.errorMessage.orEmpty()

        state.message
            ?.let(
                ::renderMessage
            )

        binding
            .layoutMailboxDetailActions
            .isEnabled =
            !state.isOperationRunning

        binding.buttonMailboxDelete.isEnabled =
            !state.isOperationRunning

        binding.buttonMailboxMarkRead.isEnabled =
            !state.isOperationRunning

        binding.buttonMailboxMarkUnread.isEnabled =
            !state.isOperationRunning
        if (
            ::attachmentAdapter.isInitialized
        ) {

            attachmentAdapter
                .refreshDownloadState()
        }
    }

    private fun renderMessage(
        message: MailboxMessageDetail
    ) {

        binding.textViewMailboxDetailSubject.text =
            message.subject
                .ifBlank {

                    getString(
                        R.string.mailbox_no_subject
                    )
                }

        binding.textViewMailboxDetailSender.text =
            getString(
                R.string.mailbox_from_format,
                message.sender.fullName
            )

        binding.textViewMailboxDetailSenderEmail.text =
            message.sender.email

        val recipients =
            message.recipients
                .joinToString(
                    separator = ", "
                ) {
                    it.fullName
                }

        binding.textViewMailboxDetailRecipients.text =
            getString(
                R.string.mailbox_to_format,
                recipients
            )

        binding.textViewMailboxDetailDate.text =
            dateTimeFormatter
                .formatUtcDateTime(
                    message.sentAtUtc
                )

        binding.textViewMailboxDetailBody.text =
            message.body

        // ---------------------------------------------------------------------
        // ATTACHMENTS
        // ---------------------------------------------------------------------

        val hasAttachments =
            message.attachments.isNotEmpty()

        binding.textViewMailboxAttachmentsTitle
            .isVisible =
            hasAttachments

        binding.recyclerViewMailboxAttachments
            .isVisible =
            hasAttachments

        attachmentAdapter.submitList(
            message.attachments
        )

        // ---------------------------------------------------------------------
        // READ ACTION
        // ---------------------------------------------------------------------

        val isInbox =
            sourceFolder ==
                    MailboxFolder.INBOX
        binding.buttonMailboxReply
            .isVisible =
            isInbox
        /*
         * Sent kutusunda IsRead null olabileceği için read/unread
         * aksiyonlarını göstermiyoruz.
         */
        binding.buttonMailboxMarkRead.isVisible =
            isInbox &&
                    message.isRead == false

        binding.buttonMailboxMarkUnread.isVisible =
            isInbox &&
                    message.isRead == true
    }

    /**
     * Liste ekranına refresh gerektiğini bildirir.
     */
    private fun notifyMailboxChanged() {

        findNavController()
            .previousBackStackEntry
            ?.savedStateHandle
            ?.set(
                MailboxNavigationResult
                    .MAILBOX_CHANGED,
                true
            )
    }

    override fun onDestroyView() {

        if (
            ::attachmentAdapter.isInitialized
        ) {

            binding
                .recyclerViewMailboxAttachments
                .adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }
    // =========================================================================
// REPLY
// =========================================================================

    /**
     * Açık olan mesajın gönderenine cevap yazma ekranını açar.
     *
     * ÖNEMLİ:
     *
     * Mailbox backend'i mesaj gönderirken e-posta değil
     * RecipientUserIds bekliyor.
     *
     * Bu nedenle Compose ekranına:
     *
     * - sender.id
     * - sender.email
     * - message.subject
     *
     * bilgilerini gönderiyoruz.
     *
     * Compose ViewModel daha sonra mevcut SearchMailboxRecipientsUseCase
     * üzerinden gerçek MailboxRecipientUser nesnesini bulacak.
     */
    private fun openReplyComposer() {

        val message =
            latestUiState.message
                ?: return

        /*
         * Gönderilenler klasöründe message.sender mevcut kullanıcıdır.
         *
         * "Cevapla" işleminin anlamlı olduğu temel senaryo Inbox olduğu
         * için yalnız gelen mesajlara cevap veriyoruz.
         */
        if (
            sourceFolder !=
            MailboxFolder.INBOX
        ) {
            return
        }

        val sender =
            message.sender

        if (
            sender.id <= 0 ||
            sender.email.isBlank()
        ) {

            Snackbar.make(
                binding.root,
                "Mesajı gönderen kullanıcı bilgisi bulunamadı.",
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }

        val arguments =
            Bundle().apply {

                putInt(
                    ARG_REPLY_SENDER_ID,
                    sender.id
                )

                putString(
                    ARG_REPLY_SENDER_EMAIL,
                    sender.email
                )

                putString(
                    ARG_REPLY_SUBJECT,
                    message.subject
                )
            }

        findNavController()
            .navigate(
                R.id.action_mailboxDetailFragment_to_mailboxComposeFragment,
                arguments
            )
    }

    companion object {

        const val ARG_MESSAGE_ID =
            "messageId"

        const val ARG_SOURCE_FOLDER =
            "sourceFolder"

        private const val INVALID_ID =
            -1
        const val ARG_REPLY_SENDER_ID =
            "replySenderId"

        const val ARG_REPLY_SENDER_EMAIL =
            "replySenderEmail"

        const val ARG_REPLY_SUBJECT =
            "replySubject"
    }
}