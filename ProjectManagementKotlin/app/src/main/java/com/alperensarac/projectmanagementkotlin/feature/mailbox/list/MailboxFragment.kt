package com.alperensarac.projectmanagementkotlin.feature.mailbox.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.FragmentMailboxBinding
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.feature.mailbox.MailboxNavigationResult
import com.alperensarac.projectmanagementkotlin.feature.mailbox.detail.MailboxDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Mailbox ana ekranı.
 *
 * Tek ekran üzerinden:
 *
 * - Gelen Kutusu
 * - Gönderilenler
 *
 * yönetilir.
 *
 * Desteklenen özellikler:
 *
 * - Paging 3
 * - Search
 * - Read filter
 * - Attachment filter
 * - Swipe refresh
 * - Retry
 * - Detail navigation
 * - Compose navigation
 * - Alt ekranlardan dönüldüğünde otomatik refresh
 */
@AndroidEntryPoint
class MailboxFragment : Fragment() {

    // =========================================================================
    // VIEW BINDING
    // =========================================================================

    private var _binding:
            FragmentMailboxBinding? =
        null

    private val binding:
            FragmentMailboxBinding
        get() =
            checkNotNull(_binding) {
                "FragmentMailboxBinding view lifecycle dışında kullanılamaz."
            }

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel:
            MailboxListViewModel
            by viewModels()

    // =========================================================================
    // DEPENDENCIES
    // =========================================================================

    @Inject
    lateinit var dateTimeFormatter:
            DateTimeFormatter

    // =========================================================================
    // ADAPTER
    // =========================================================================

    private lateinit var mailboxAdapter:
            MailboxMessagePagingAdapter

    // =========================================================================
    // LOCAL UI STATE
    // =========================================================================

    /**
     * Adapter:
     *
     * Inbox -> gönderen
     * Sent  -> alıcı
     *
     * gösterdiği için mevcut folder bilgisini bilmesi gerekir.
     */
    private var currentFolder:
            MailboxFolder =
        MailboxFolder.INBOX

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentMailboxBinding.inflate(
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

        configureRecyclerView()

        configureFolderToggle()

        configureSearch()

        configureReadFilter()

        configureAttachmentFilter()

        configureRefresh()

        configureCompose()

        observeMailboxResults()

        observeUiState()

        observeMessages()

        observeLoadState()
    }

    // =========================================================================
    // RECYCLER VIEW
    // =========================================================================

    private fun configureRecyclerView() {

        mailboxAdapter =
            MailboxMessagePagingAdapter(

                dateTimeFormatter =
                dateTimeFormatter,

                folderProvider = {
                    currentFolder
                },

                onMessageClicked = { message ->

                    openMessageDetail(
                        messageId =
                        message.id
                    )
                }
            )

        binding.recyclerViewMailbox.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                mailboxAdapter
                    .withLoadStateFooter(

                        footer =
                        MailboxLoadStateAdapter(
                            retry = {
                                mailboxAdapter.retry()
                            }
                        )
                    )

            /*
             * Mesaj satırlarının yüksekliği sabit değil.
             *
             * Preview uzunluğu vb. değişebildiği için false.
             */
            setHasFixedSize(
                false
            )
        }
    }

    // =========================================================================
    // FOLDER
    // =========================================================================

    private fun configureFolderToggle() {

        binding.toggleGroupMailboxFolder
            .addOnButtonCheckedListener {
                    _,
                    checkedId,
                    isChecked ->

                /*
                 * ToggleGroup eski buton için unchecked callback'i de
                 * gönderdiğinden yalnızca checked olan butonla ilgileniyoruz.
                 */
                if (!isChecked) {
                    return@addOnButtonCheckedListener
                }

                when (checkedId) {

                    R.id.buttonMailboxInbox -> {

                        viewModel.selectInbox()
                    }

                    R.id.buttonMailboxSent -> {

                        viewModel.selectSent()
                    }
                }
            }
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    private fun configureSearch() {

        binding.editTextMailboxSearch
            .doAfterTextChanged { editable ->

                viewModel.onSearchChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // READ FILTER
    // =========================================================================

    private fun configureReadFilter() {

        val labels =
            listOf(

                getString(
                    R.string.mailbox_read_all
                ),

                getString(
                    R.string.mailbox_read_only
                ),

                getString(
                    R.string.mailbox_unread_only
                )
            )

        val dropdownAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteMailboxReadFilter
            .setAdapter(
                dropdownAdapter
            )

        binding.autoCompleteMailboxReadFilter
            .setText(
                labels.first(),
                false
            )

        binding.autoCompleteMailboxReadFilter
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val filter =
                    when (position) {

                        1 ->
                            MailboxReadFilter.READ

                        2 ->
                            MailboxReadFilter.UNREAD

                        else ->
                            MailboxReadFilter.ALL
                    }

                viewModel.onReadFilterChanged(
                    filter
                )
            }
    }

    // =========================================================================
    // ATTACHMENT FILTER
    // =========================================================================

    private fun configureAttachmentFilter() {

        val labels =
            listOf(

                getString(
                    R.string.mailbox_attachment_all
                ),

                getString(
                    R.string.mailbox_attachment_only
                ),

                getString(
                    R.string.mailbox_without_attachment
                )
            )

        val dropdownAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteMailboxAttachmentFilter
            .setAdapter(
                dropdownAdapter
            )

        binding.autoCompleteMailboxAttachmentFilter
            .setText(
                labels.first(),
                false
            )

        binding.autoCompleteMailboxAttachmentFilter
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val filter =
                    when (position) {

                        1 ->
                            MailboxAttachmentFilter
                                .WITH_ATTACHMENT

                        2 ->
                            MailboxAttachmentFilter
                                .WITHOUT_ATTACHMENT

                        else ->
                            MailboxAttachmentFilter
                                .ALL
                    }

                viewModel
                    .onAttachmentFilterChanged(
                        filter
                    )
            }
    }

    // =========================================================================
    // REFRESH
    // =========================================================================

    private fun configureRefresh() {

        binding.swipeRefreshMailbox
            .setOnRefreshListener {

                /*
                 * PagingSource invalid edilir ve backend source of truth
                 * olarak yeniden sorgulanır.
                 */
                mailboxAdapter.refresh()
            }

        binding.buttonMailboxRetry
            .setOnClickListener {

                mailboxAdapter.retry()
            }
    }

    // =========================================================================
    // COMPOSE
    // =========================================================================

    /**
     * Yeni mesaj ekranına gider.
     */
    private fun configureCompose() {

        binding.fabMailboxCompose
            .setOnClickListener {

                /*
                 * Çok hızlı çift tıklamada iki navigation yapılmasını
                 * engelliyoruz.
                 */
                if (
                    findNavController()
                        .currentDestination
                        ?.id !=
                    R.id.mailboxFragment
                ) {
                    return@setOnClickListener
                }

                findNavController()
                    .navigate(
                        R.id.action_mailboxFragment_to_mailboxComposeFragment
                    )
            }
    }

    // =========================================================================
    // CHILD SCREEN RESULT
    // =========================================================================

    /**
     * Detail veya Compose ekranından dönüldüğünde mailbox verilerinin
     * değişip değişmediğini kontrol eder.
     *
     * Örneğin:
     *
     * - yeni mesaj gönderildi
     * - mesaj silindi
     * - read/unread değişti
     *
     * ise Paging refresh edilir.
     */
    private fun observeMailboxResults() {

        val savedStateHandle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?: return

        savedStateHandle
            .getLiveData<Boolean>(
                MailboxNavigationResult
                    .MAILBOX_CHANGED
            )
            .observe(
                viewLifecycleOwner
            ) { changed ->

                if (changed != true) {
                    return@observe
                }

                /*
                 * Lokal adapter listesini elle değiştirmiyoruz.
                 *
                 * Backend source of truth.
                 */
                mailboxAdapter.refresh()

                /*
                 * Event tek kullanımlık.
                 */
                savedStateHandle
                    .remove<Boolean>(
                        MailboxNavigationResult
                            .MAILBOX_CHANGED
                    )
            }
    }

    // =========================================================================
    // UI STATE
    // =========================================================================

    private fun observeUiState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.uiState
                            .collectLatest { state ->

                                renderUiState(
                                    state
                                )
                            }
                    }
            }
    }

    private fun renderUiState(
        state: MailboxListUiState
    ) {

        val previousFolder =
            currentFolder

        currentFolder =
            state.folder

        // ---------------------------------------------------------------------
        // TOGGLE
        // ---------------------------------------------------------------------

        val expectedButtonId =
            when (state.folder) {

                MailboxFolder.INBOX ->
                    R.id.buttonMailboxInbox

                MailboxFolder.SENT ->
                    R.id.buttonMailboxSent
            }

        if (
            binding.toggleGroupMailboxFolder
                .checkedButtonId !=
            expectedButtonId
        ) {

            binding.toggleGroupMailboxFolder
                .check(
                    expectedButtonId
                )
        }

        // ---------------------------------------------------------------------
        // READ FILTER
        // ---------------------------------------------------------------------

        binding.textInputLayoutMailboxReadFilter
            .isVisible =
            state.folder ==
                    MailboxFolder.INBOX

        // ---------------------------------------------------------------------
        // EMPTY TITLE
        // ---------------------------------------------------------------------

        binding.textViewMailboxEmptyTitle.text =
            when (state.folder) {

                MailboxFolder.INBOX ->

                    getString(
                        R.string.mailbox_inbox_empty
                    )

                MailboxFolder.SENT ->

                    getString(
                        R.string.mailbox_sent_empty
                    )
            }

        // ---------------------------------------------------------------------
        // REBIND
        // ---------------------------------------------------------------------

        /*
         * Folder değişince mevcut ViewHolder'ların Sender/Recipient
         * gösterimini yeniden bind ediyoruz.
         */
        if (
            previousFolder !=
            currentFolder &&
            ::mailboxAdapter.isInitialized
        ) {

            mailboxAdapter
                .notifyItemRangeChanged(
                    0,
                    mailboxAdapter.itemCount
                )
        }
    }

    // =========================================================================
    // PAGING DATA
    // =========================================================================

    private fun observeMessages() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.messages
                            .collectLatest { pagingData ->

                                mailboxAdapter.submitData(
                                    pagingData
                                )
                            }
                    }
            }
    }

    // =========================================================================
    // PAGING LOAD STATE
    // =========================================================================

    private fun observeLoadState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        mailboxAdapter
                            .loadStateFlow
                            .collectLatest { loadStates ->

                                renderLoadState(
                                    refresh =
                                    loadStates.refresh
                                )
                            }
                    }
            }
    }

    private fun renderLoadState(
        refresh: LoadState
    ) {

        val itemCount =
            mailboxAdapter.itemCount

        val isLoading =
            refresh is
                    LoadState.Loading

        val isError =
            refresh is
                    LoadState.Error

        val isNotLoading =
            refresh is
                    LoadState.NotLoading

        // ---------------------------------------------------------------------
        // INITIAL LOADING
        // ---------------------------------------------------------------------

        binding.progressIndicatorMailbox
            .isVisible =
            isLoading &&
                    itemCount == 0 &&
                    !binding
                        .swipeRefreshMailbox
                        .isRefreshing

        // ---------------------------------------------------------------------
        // SWIPE REFRESH
        // ---------------------------------------------------------------------

        if (!isLoading) {

            binding.swipeRefreshMailbox
                .isRefreshing =
                false
        }

        // ---------------------------------------------------------------------
        // LIST
        // ---------------------------------------------------------------------

        binding.recyclerViewMailbox
            .isVisible =
            itemCount > 0

        // ---------------------------------------------------------------------
        // EMPTY
        // ---------------------------------------------------------------------

        binding.layoutMailboxEmpty
            .isVisible =
            isNotLoading &&
                    itemCount == 0

        // ---------------------------------------------------------------------
        // ERROR
        // ---------------------------------------------------------------------

        binding.layoutMailboxError
            .isVisible =
            isError &&
                    itemCount == 0

        if (
            refresh is
                    LoadState.Error
        ) {

            binding.textViewMailboxError.text =
                refresh.error
                    .message
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: getString(
                        R.string.mailbox_load_error
                    )
        }
    }

    // =========================================================================
    // DETAIL NAVIGATION
    // =========================================================================

    private fun openMessageDetail(
        messageId: Int
    ) {

        if (
            messageId <= 0
        ) {
            return
        }

        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.mailboxFragment
        ) {
            return
        }

        findNavController()
            .navigate(

                R.id.action_mailboxFragment_to_mailboxDetailFragment,

                bundleOf(

                    MailboxDetailFragment
                        .ARG_MESSAGE_ID
                            to messageId,

                    MailboxDetailFragment
                        .ARG_SOURCE_FOLDER
                            to currentFolder.name
                )
            )
    }

    // =========================================================================
    // DESTROY VIEW
    // =========================================================================

    override fun onDestroyView() {

        if (
            ::mailboxAdapter.isInitialized
        ) {

            binding.recyclerViewMailbox
                .adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }
}