package com.alperensarac.projectmanagementkotlin.feature.mailbox.compose

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.data.upload.MailboxFileMetadataResolver
import com.alperensarac.projectmanagementkotlin.databinding.FragmentMailboxComposeBinding
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRules
import com.alperensarac.projectmanagementkotlin.feature.mailbox.MailboxNavigationResult
import com.alperensarac.projectmanagementkotlin.feature.mailbox.detail.MailboxDetailFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MailboxComposeFragment : Fragment() {

    // =========================================================================
    // BINDING
    // =========================================================================

    private var _binding:
            FragmentMailboxComposeBinding? =
        null

    private val binding:
            FragmentMailboxComposeBinding
        get() =
            checkNotNull(_binding)

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel:
            MailboxComposeViewModel
            by viewModels()

    // =========================================================================
    // DEPENDENCIES
    // =========================================================================

    @Inject
    lateinit var fileMetadataResolver:
            MailboxFileMetadataResolver

    // =========================================================================
    // ADAPTERS
    // =========================================================================

    private lateinit var recipientAdapter:
            MailboxRecipientAdapter

    private lateinit var attachmentAdapter:
            MailboxSelectedAttachmentAdapter

    // =========================================================================
    // DOCUMENT PICKER
    // =========================================================================

    /**
     * ACTION_OPEN_DOCUMENT tabanlı çoklu dosya seçimi.
     *
     * Dosya içeriğini burada okumuyoruz.
     * Yalnızca Uri alıyoruz.
     */
    private val attachmentPicker =
        registerForActivityResult(
            ActivityResultContracts
                .OpenMultipleDocuments()
        ) { uris ->

            if (uris.isEmpty()) {
                return@registerForActivityResult
            }

            val resolver =
                requireContext()
                    .contentResolver

            val files =
                uris.mapNotNull { uri ->

                    /*
                     * URI okuma yetkisini uygulama yeniden açıldığında da
                     * koruyabilmek için mümkünse persist ediyoruz.
                     */
                    runCatching {

                        resolver
                            .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                    }

                    val result =
                        fileMetadataResolver.resolve(
                            contentResolver =
                            resolver,

                            uri =
                            uri
                        )

                    result
                        .onFailure { error ->

                            Snackbar.make(
                                binding.root,
                                error.message
                                    ?: "Dosya bilgisi okunamadı.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        .getOrNull()
                }

            viewModel.addAttachments(
                files
            )
        }

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentMailboxComposeBinding
                .inflate(
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

        configureToolbar()

        configureRecipients()

        configureMessageFields()

        configureAttachments()

        configureSendButton()

        observeState()

        observeEvents()

        configureReply()
    }

    // =========================================================================
    // TOOLBAR
    // =========================================================================

    private fun configureToolbar() {

        binding.toolbarMailboxCompose
            .setNavigationOnClickListener {

                findNavController()
                    .navigateUp()
            }
    }

    // =========================================================================
    // RECIPIENT
    // =========================================================================

    private fun configureRecipients() {

        recipientAdapter =
            MailboxRecipientAdapter(
                onRecipientClicked = { user ->

                    viewModel.selectRecipient(
                        user
                    )
                }
            )

        binding.recyclerViewRecipientResults.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                recipientAdapter

            isNestedScrollingEnabled =
                false
        }

        binding.editTextRecipientSearch
            .doAfterTextChanged { editable ->

                viewModel.onRecipientSearchChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }

        /*
         * Ekran açıldığında boş Search ile ilk aktif kullanıcı
         * grubunu getir.
         */
        if (
            viewModel.uiState
                .value
                .recipientResults
                .isEmpty()
        ) {

            viewModel.onRecipientSearchChanged(
                ""
            )
        }
    }

    private fun renderSelectedRecipients(
        recipients:
        List<MailboxRecipientUser>
    ) {

        binding
            .chipGroupSelectedRecipients
            .removeAllViews()

        recipients.forEach { user ->

            val chip =
                Chip(
                    requireContext()
                ).apply {

                    text =
                        user.fullName

                    isCloseIconVisible =
                        true

                    isCheckable =
                        false

                    setOnCloseIconClickListener {

                        viewModel.removeRecipient(
                            user
                        )
                    }
                }

            binding
                .chipGroupSelectedRecipients
                .addView(
                    chip
                )
        }

        binding.textViewRecipientCount.text =
            getString(
                R.string.mailbox_recipient_count_format,
                recipients.size,
                MailboxRules.MAXIMUM_RECIPIENT_COUNT
            )
    }

    // =========================================================================
    // MESSAGE FIELDS
    // =========================================================================

    private fun configureMessageFields() {

        binding.editTextMailboxSubject
            .doAfterTextChanged { editable ->

                viewModel.onSubjectChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }

        binding.editTextMailboxBody
            .doAfterTextChanged { editable ->

                viewModel.onBodyChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
// REPLY
// =========================================================================

    /**
     * MailboxDetailFragment üzerindeki Cevapla butonundan gelen
     * başlangıç bilgilerini ViewModel'e aktarır.
     *
     * Normal "Yeni Mesaj" akışında argument'lar bulunmayacağı için
     * hiçbir işlem yapılmaz.
     */
    private fun configureReply() {

        val senderId =
            arguments
                ?.getInt(
                    MailboxDetailFragment
                        .ARG_REPLY_SENDER_ID,
                    INVALID_REPLY_SENDER_ID
                )
                ?: INVALID_REPLY_SENDER_ID

        val senderEmail =
            arguments
                ?.getString(
                    MailboxDetailFragment
                        .ARG_REPLY_SENDER_EMAIL
                )

        val subject =
            arguments
                ?.getString(
                    MailboxDetailFragment
                        .ARG_REPLY_SUBJECT
                )

        if (
            senderId <= 0 ||
            senderEmail.isNullOrBlank()
        ) {

            /*
             * Bu normal "Yeni Mesaj" ekranıdır.
             */
            return
        }

        /*
         * Toolbar başlığını da cevap moduna göre değiştirebiliriz.
         */
        binding.toolbarMailboxCompose
            .title =
            "Mesajı Yanıtla"

        viewModel.initializeReply(
            senderId =
            senderId,

            senderEmail =
            senderEmail,

            originalSubject =
            subject
        )
    }

    // =========================================================================
    // ATTACHMENTS
    // =========================================================================

    private fun configureAttachments() {

        attachmentAdapter =
            MailboxSelectedAttachmentAdapter(
                onRemoveClicked = { file ->

                    viewModel.removeAttachment(
                        file
                    )
                }
            )

        binding.recyclerViewSelectedAttachments
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

        binding.buttonAddMailboxAttachment
            .setOnClickListener {

                val state =
                    viewModel.uiState.value

                if (
                    !state.canAddMoreAttachments
                ) {

                    showSnackbar(
                        "Bir mesaja en fazla 10 dosya eklenebilir."
                    )

                    return@setOnClickListener
                }

                attachmentPicker.launch(
                    ALLOWED_PICKER_MIME_TYPES
                )
            }
    }

    // =========================================================================
    // SEND
    // =========================================================================

    private fun configureSendButton() {

        binding.buttonSendMailboxMessage
            .setOnClickListener {

                clearInputErrors()

                val state =
                    viewModel.uiState.value

                if (
                    state.selectedRecipients
                        .isEmpty()
                ) {

                    showSnackbar(
                        "En az bir alıcı seçilmelidir."
                    )

                    return@setOnClickListener
                }

                if (
                    state.subject
                        .isBlank()
                ) {

                    binding
                        .textInputLayoutMailboxSubject
                        .error =
                        "Mesaj konusu zorunludur."

                    return@setOnClickListener
                }

                if (
                    state.body
                        .isBlank()
                ) {

                    binding
                        .textInputLayoutMailboxBody
                        .error =
                        "Mesaj içeriği zorunludur."

                    return@setOnClickListener
                }

                viewModel.send(
                    contentResolver =
                    requireContext()
                        .contentResolver
                )
            }
    }

    private fun clearInputErrors() {

        binding
            .textInputLayoutMailboxSubject
            .error =
            null

        binding
            .textInputLayoutMailboxBody
            .error =
            null
    }

    // =========================================================================
    // STATE
    // =========================================================================

    private fun observeState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
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

    private fun renderState(
        state: MailboxComposeUiState
    ) {

        // ---------------------------------------------------------------------
        // RECIPIENT SEARCH
        // ---------------------------------------------------------------------

        binding.progressRecipientSearch
            .isVisible =
            state.isSearchingRecipients

        binding.textViewRecipientSearchError
            .isVisible =
            !state.recipientSearchError
                .isNullOrBlank()

        binding.textViewRecipientSearchError
            .text =
            state.recipientSearchError
                .orEmpty()

        recipientAdapter.submitList(
            state.recipientResults
        )

        renderSelectedRecipients(
            state.selectedRecipients
        )

        // ---------------------------------------------------------------------
        // MESSAGE FIELDS
        // ---------------------------------------------------------------------

        /*
         * Reply modunda subject ViewModel tarafından otomatik oluşturulabilir.
         *
         * EditText -> ViewModel listener'ı da bulunduğu için yalnız metin
         * gerçekten farklıysa setText yapıyoruz.
         */
        val currentSubjectText =
            binding.editTextMailboxSubject
                .text
                ?.toString()
                .orEmpty()

        if (
            currentSubjectText !=
            state.subject
        ) {

            binding.editTextMailboxSubject
                .setText(
                    state.subject
                )

            binding.editTextMailboxSubject
                .setSelection(
                    binding.editTextMailboxSubject
                        .text
                        ?.length
                        ?: 0
                )
        }

        // ---------------------------------------------------------------------
        // COUNTERS
        // ---------------------------------------------------------------------

        binding.textViewMailboxSubjectCount.text =
            getString(
                R.string.mailbox_character_count_format,
                state.subject.length,
                MailboxRules.MAXIMUM_SUBJECT_LENGTH
            )

        binding.textViewMailboxBodyCount.text =
            getString(
                R.string.mailbox_character_count_format,
                state.body.length,
                MailboxRules.MAXIMUM_BODY_LENGTH
            )

        // ---------------------------------------------------------------------
        // ATTACHMENTS
        // ---------------------------------------------------------------------

        attachmentAdapter.submitList(
            state.attachments
        )

        binding.textViewAttachmentSummary.text =
            getString(
                R.string.mailbox_attachment_summary_format,
                state.attachmentCount,
                MailboxRules.MAXIMUM_ATTACHMENT_COUNT,
                formatFileSize(
                    state.totalAttachmentBytes
                )
            )

        binding.buttonAddMailboxAttachment
            .isEnabled =
            !state.isSending &&
                    state.canAddMoreAttachments

        // ---------------------------------------------------------------------
        // UPLOAD
        // ---------------------------------------------------------------------

        binding.progressMailboxUpload
            .isVisible =
            state.isSending

        binding.textViewMailboxUploadProgress
            .isVisible =
            state.isSending

        if (state.isSending) {

            binding.progressMailboxUpload.progress =
                state.uploadProgress

            binding.textViewMailboxUploadProgress.text =
                getString(
                    R.string.mailbox_upload_progress_format,
                    state.uploadProgress
                )
        }

        // ---------------------------------------------------------------------
        // FORM ENABLED
        // ---------------------------------------------------------------------

        binding.buttonSendMailboxMessage
            .isEnabled =
            state.canSend

        binding.editTextRecipientSearch
            .isEnabled =
            !state.isSending

        binding.editTextMailboxSubject
            .isEnabled =
            !state.isSending

        binding.editTextMailboxBody
            .isEnabled =
            !state.isSending
    }

    // =========================================================================
    // EVENTS
    // =========================================================================

    private fun observeEvents() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.events
                            .collect { event ->

                                when (event) {

                                    is MailboxComposeUiEvent
                                    .ShowMessage -> {

                                        showSnackbar(
                                            event.message
                                        )
                                    }

                                    is MailboxComposeUiEvent
                                    .MessageSent -> {

                                        notifyMailboxChanged()

                                        /*
                                         * Compose ekranından mailbox
                                         * listesine dönüyoruz.
                                         */
                                        findNavController()
                                            .navigateUp()
                                    }
                                }
                            }
                    }
            }
    }

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

    // =========================================================================
    // HELPERS
    // =========================================================================

    private fun showSnackbar(
        message: String
    ) {

        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun formatFileSize(
        bytes: Long
    ): String {

        val mb =
            1024.0 *
                    1024.0

        return String.format(
            Locale.getDefault(),
            "%.2f MB / 200 MB",
            bytes / mb
        )
    }

    // =========================================================================
    // DESTROY
    // =========================================================================

    override fun onDestroyView() {

        if (
            ::recipientAdapter.isInitialized
        ) {

            binding.recyclerViewRecipientResults
                .adapter =
                null
        }

        if (
            ::attachmentAdapter.isInitialized
        ) {

            binding.recyclerViewSelectedAttachments
                .adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }

    private companion object {

        val ALLOWED_PICKER_MIME_TYPES =
            arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/zip",
                "application/x-zip-compressed",
                "image/png",
                "image/jpeg"
            )
        const val INVALID_REPLY_SENDER_ID =
            -1
    }
}