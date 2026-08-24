package com.alperensarac.projectmanagementkotlin.feature.mailbox.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.databinding.ItemMailboxLoadStateBinding

/**
 * Paging append/prepend yükleme ve retry görünümü.
 */
class MailboxLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<
        MailboxLoadStateAdapter.LoadStateViewHolder
        >() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder {

        val binding =
            ItemMailboxLoadStateBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return LoadStateViewHolder(
            binding = binding,
            retry = retry
        )
    }

    override fun onBindViewHolder(
        holder: LoadStateViewHolder,
        loadState: LoadState
    ) {

        holder.bind(
            loadState
        )
    }

    class LoadStateViewHolder(
        private val binding:
        ItemMailboxLoadStateBinding,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        init {

            binding.buttonMailboxLoadStateRetry
                .setOnClickListener {

                    retry()
                }
        }

        fun bind(
            loadState: LoadState
        ) {

            binding
                .progressIndicatorMailboxLoadState
                .isVisible =
                loadState is
                        LoadState.Loading

            binding
                .buttonMailboxLoadStateRetry
                .isVisible =
                loadState is
                        LoadState.Error

            binding
                .textViewMailboxLoadStateError
                .isVisible =
                loadState is
                        LoadState.Error

            binding
                .textViewMailboxLoadStateError
                .text =
                (loadState as? LoadState.Error)
                    ?.error
                    ?.message
                    .orEmpty()
        }
    }
}