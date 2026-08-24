package com.alperensarac.projectmanagementkotlin.feature.mailbox.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.databinding.ItemMailboxRecipientBinding
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser

class MailboxRecipientAdapter(
    private val onRecipientClicked:
        (MailboxRecipientUser) -> Unit
) : ListAdapter<
        MailboxRecipientUser,
        MailboxRecipientAdapter.RecipientViewHolder
        >(
    DiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipientViewHolder {

        val binding =
            ItemMailboxRecipientBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return RecipientViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: RecipientViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    inner class RecipientViewHolder(
        private val binding:
        ItemMailboxRecipientBinding
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

                    onRecipientClicked(
                        getItem(position)
                    )
                }
        }

        fun bind(
            user: MailboxRecipientUser
        ) {

            binding.textViewRecipientAvatar.text =
                user.fullName
                    .trim()
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"

            binding.textViewRecipientName.text =
                user.fullName

            binding.textViewRecipientEmail.text =
                user.email
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<MailboxRecipientUser>() {

        override fun areItemsTheSame(
            oldItem: MailboxRecipientUser,
            newItem: MailboxRecipientUser
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MailboxRecipientUser,
            newItem: MailboxRecipientUser
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}