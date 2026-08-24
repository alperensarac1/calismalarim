package com.alperensarac.projectmanagementkotlin.feature.mailbox.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemMailboxMessageBinding
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessage

/**
 * Inbox ve Sent listelerinde kullanılan ortak PagingDataAdapter.
 */
class MailboxMessagePagingAdapter(
    private val dateTimeFormatter: DateTimeFormatter,
    private val folderProvider: () -> MailboxFolder,
    private val onMessageClicked: (MailboxMessage) -> Unit
) : PagingDataAdapter<
        MailboxMessage,
        MailboxMessagePagingAdapter.MailboxMessageViewHolder
        >(
    DiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MailboxMessageViewHolder {

        val binding =
            ItemMailboxMessageBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return MailboxMessageViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: MailboxMessageViewHolder,
        position: Int
    ) {

        getItem(position)
            ?.let(
                holder::bind
            )
    }

    inner class MailboxMessageViewHolder(
        private val binding: ItemMailboxMessageBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        init {

            binding.root
                .setOnClickListener {

                    val position =
                        bindingAdapterPosition

                    if (
                        position ==
                        RecyclerView.NO_POSITION
                    ) {
                        return@setOnClickListener
                    }

                    getItem(position)
                        ?.let(
                            onMessageClicked
                        )
                }
        }

        fun bind(
            message: MailboxMessage
        ) {

            val context =
                binding.root.context

            val folder =
                folderProvider()

            // -----------------------------------------------------------------
            // AVATAR
            // -----------------------------------------------------------------

            val displayName =
                when (folder) {

                    MailboxFolder.INBOX ->
                        message.sender.fullName

                    MailboxFolder.SENT ->
                        message.recipients
                            .firstOrNull()
                            ?.fullName
                            ?: context.getString(
                                R.string.mailbox_unknown_user
                            )
                }

            binding.textViewMailboxAvatar.text =
                displayName
                    .trim()
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"

            // -----------------------------------------------------------------
            // PERSON
            // -----------------------------------------------------------------

            binding.textViewMailboxPerson.text =
                when (folder) {

                    MailboxFolder.INBOX ->
                        message.sender.fullName

                    MailboxFolder.SENT -> {

                        if (
                            message.recipients.size <= 1
                        ) {

                            displayName

                        } else {

                            context.getString(
                                R.string.mailbox_multiple_recipients_format,
                                displayName,
                                message.recipients.size - 1
                            )
                        }
                    }
                }

            // -----------------------------------------------------------------
            // SUBJECT
            // -----------------------------------------------------------------

            binding.textViewMailboxSubject.text =
                message.subject
                    .ifBlank {

                        context.getString(
                            R.string.mailbox_no_subject
                        )
                    }

            // -----------------------------------------------------------------
            // PREVIEW
            // -----------------------------------------------------------------

            binding.textViewMailboxPreview.text =
                message.bodyPreview

            // -----------------------------------------------------------------
            // DATE
            // -----------------------------------------------------------------

            binding.textViewMailboxDate.text =
                dateTimeFormatter
                    .formatUtcDateTime(
                        message.sentAtUtc
                    )

            // -----------------------------------------------------------------
            // ATTACHMENT
            // -----------------------------------------------------------------

            binding.layoutMailboxAttachment.isVisible =
                message.hasAttachment

            binding.textViewMailboxAttachmentCount.text =
                context.getString(
                    R.string.mailbox_attachment_count_format,
                    message.attachmentCount
                )

            // -----------------------------------------------------------------
            // READ STATE
            // -----------------------------------------------------------------

            /*
             * Backend'e göre Sent ekranında isRead null olabilir.
             *
             * Bu nedenle okunmadı göstergesini yalnızca Inbox'ta
             * değerlendiriyoruz.
             */
            val isUnread =
                folder ==
                        MailboxFolder.INBOX &&
                        message.isRead == false

            binding.viewMailboxUnreadDot.isVisible =
                isUnread

            binding.textViewMailboxSubject
                .setTypeface(
                    binding.textViewMailboxSubject.typeface,

                    if (isUnread) {
                        android.graphics.Typeface.BOLD
                    } else {
                        android.graphics.Typeface.NORMAL
                    }
                )

            binding.textViewMailboxPerson
                .setTypeface(
                    binding.textViewMailboxPerson.typeface,

                    if (isUnread) {
                        android.graphics.Typeface.BOLD
                    } else {
                        android.graphics.Typeface.NORMAL
                    }
                )
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<MailboxMessage>() {

        override fun areItemsTheSame(
            oldItem: MailboxMessage,
            newItem: MailboxMessage
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MailboxMessage,
            newItem: MailboxMessage
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}