package com.alperensarac.projectmanagementkotlin.feature.mailbox.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.databinding.ItemMailboxAttachmentBinding
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxAttachment
import java.util.Locale

/**
 * Mesaj attachment'larını gösterir.
 */
class MailboxAttachmentAdapter(
    private val downloadingAttachmentId: () -> Int?,
    private val downloadProgress: () -> Int?,
    private val onAttachmentClicked:
        (MailboxAttachment) -> Unit
) : ListAdapter<
        MailboxAttachment,
        MailboxAttachmentAdapter.AttachmentViewHolder
        >(
    DiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttachmentViewHolder {

        val binding =
            ItemMailboxAttachmentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return AttachmentViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: AttachmentViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    /**
     * Progress ViewModel state'inde tutulduğu için item model değişmeden
     * satırı tekrar bind edebilmemizi sağlar.
     */
    fun refreshDownloadState() {

        if (itemCount > 0) {

            notifyItemRangeChanged(
                0,
                itemCount
            )
        }
    }

    inner class AttachmentViewHolder(
        private val binding:
        ItemMailboxAttachmentBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            attachment: MailboxAttachment
        ) {

            binding
                .textViewMailboxAttachmentName
                .text =
                attachment.originalFileName

            binding
                .textViewMailboxAttachmentType
                .text =
                attachment.contentType

            binding
                .textViewMailboxAttachmentSize
                .text =
                formatFileSize(
                    attachment.fileSize
                )

            // -----------------------------------------------------------------
            // AVAILABILITY
            // -----------------------------------------------------------------

            binding
                .textViewMailboxAttachmentUnavailable
                .isVisible =
                !attachment.isAvailable

            // -----------------------------------------------------------------
            // DOWNLOAD STATE
            // -----------------------------------------------------------------

            val isDownloading =
                downloadingAttachmentId() ==
                        attachment.id

            binding
                .progressIndicatorAttachmentDownload
                .isVisible =
                isDownloading

            binding
                .textViewAttachmentDownloadProgress
                .isVisible =
                isDownloading

            if (isDownloading) {

                val progress =
                    downloadProgress()

                binding
                    .progressIndicatorAttachmentDownload
                    .progress =
                    progress ?: 0

                binding
                    .textViewAttachmentDownloadProgress
                    .text =
                    progress
                        ?.let {
                            "%$it"
                        }
                        ?: ""
            }

            binding
                .buttonMailboxAttachmentDownload
                .isEnabled =
                attachment.isAvailable &&
                        !isDownloading

            // -----------------------------------------------------------------
            // CLICK
            // -----------------------------------------------------------------

            binding
                .buttonMailboxAttachmentDownload
                .setOnClickListener {

                    if (
                        attachment.isAvailable &&
                        !isDownloading
                    ) {

                        onAttachmentClicked(
                            attachment
                        )
                    }
                }
        }

        private fun formatFileSize(
            bytes: Long
        ): String {

            val kb =
                1024.0

            val mb =
                kb * 1024.0

            val gb =
                mb * 1024.0

            return when {

                bytes >= gb ->

                    String.format(
                        Locale.getDefault(),
                        "%.2f GB",
                        bytes / gb
                    )

                bytes >= mb ->

                    String.format(
                        Locale.getDefault(),
                        "%.2f MB",
                        bytes / mb
                    )

                bytes >= kb ->

                    String.format(
                        Locale.getDefault(),
                        "%.1f KB",
                        bytes / kb
                    )

                else ->

                    "$bytes B"
            }
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<MailboxAttachment>() {

        override fun areItemsTheSame(
            oldItem: MailboxAttachment,
            newItem: MailboxAttachment
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MailboxAttachment,
            newItem: MailboxAttachment
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}