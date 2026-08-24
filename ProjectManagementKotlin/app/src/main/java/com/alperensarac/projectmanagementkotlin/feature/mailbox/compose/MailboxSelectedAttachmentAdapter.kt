package com.alperensarac.projectmanagementkotlin.feature.mailbox.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.data.upload.MailboxSelectedFile
import com.alperensarac.projectmanagementkotlin.databinding.ItemMailboxSelectedAttachmentBinding
import java.util.Locale

class MailboxSelectedAttachmentAdapter(
    private val onRemoveClicked:
        (MailboxSelectedFile) -> Unit
) : ListAdapter<
        MailboxSelectedFile,
        MailboxSelectedAttachmentAdapter.AttachmentViewHolder
        >(
    DiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AttachmentViewHolder {

        val binding =
            ItemMailboxSelectedAttachmentBinding.inflate(
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

    inner class AttachmentViewHolder(
        private val binding:
        ItemMailboxSelectedAttachmentBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            file: MailboxSelectedFile
        ) {

            binding.textViewSelectedAttachmentName.text =
                file.fileName

            binding.textViewSelectedAttachmentSize.text =
                formatFileSize(
                    file.sizeBytes
                )

            binding.buttonRemoveSelectedAttachment
                .setOnClickListener {

                    onRemoveClicked(
                        file
                    )
                }
        }

        private fun formatFileSize(
            bytes: Long
        ): String {

            val mb =
                1024.0 *
                        1024.0

            return if (
                bytes >= mb
            ) {

                String.format(
                    Locale.getDefault(),
                    "%.2f MB",
                    bytes / mb
                )

            } else {

                String.format(
                    Locale.getDefault(),
                    "%.1f KB",
                    bytes / 1024.0
                )
            }
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<MailboxSelectedFile>() {

        override fun areItemsTheSame(
            oldItem: MailboxSelectedFile,
            newItem: MailboxSelectedFile
        ): Boolean {

            return oldItem.uri ==
                    newItem.uri
        }

        override fun areContentsTheSame(
            oldItem: MailboxSelectedFile,
            newItem: MailboxSelectedFile
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}