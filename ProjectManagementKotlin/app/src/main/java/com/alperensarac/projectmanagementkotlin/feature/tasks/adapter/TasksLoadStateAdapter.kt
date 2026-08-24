package com.alperensarac.projectmanagementkotlin.feature.tasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.databinding.ItemPagingLoadStateBinding

/**
 * Tasks Paging footer.
 */
class TasksLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<
        TasksLoadStateAdapter.LoadStateViewHolder
        >() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder {

        val binding =
            ItemPagingLoadStateBinding.inflate(
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
        ItemPagingLoadStateBinding,

        retry: () -> Unit

    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        init {

            binding.buttonPagingRetry
                .setOnClickListener {

                    retry()
                }
        }

        fun bind(
            loadState: LoadState
        ) {

            binding.progressIndicatorPaging.isVisible =
                loadState is
                        LoadState.Loading

            binding.buttonPagingRetry.isVisible =
                loadState is
                        LoadState.Error

            binding.textViewPagingError.isVisible =
                loadState is
                        LoadState.Error

            binding.textViewPagingError.text =
                (loadState as? LoadState.Error)
                    ?.error
                    ?.message
                    .orEmpty()
        }
    }
}