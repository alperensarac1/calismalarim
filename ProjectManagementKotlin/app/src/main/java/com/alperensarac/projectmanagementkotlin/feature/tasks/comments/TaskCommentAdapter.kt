package com.alperensarac.projectmanagementkotlin.feature.tasks.detail.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemTaskCommentBinding
import com.alperensarac.projectmanagementkotlin.domain.model.comments.TaskComment

/**
 * Görev yorumları adapter'ı.
 *
 * canEdit / canDelete yetkileri doğrudan backend'den gelir.
 */
class TaskCommentAdapter(
    private val dateTimeFormatter: DateTimeFormatter,

    /**
     * ViewModel'deki güncelleme state'ini okur.
     */
    private val updatingCommentId: () -> Int?,

    /**
     * ViewModel'deki silme state'ini okur.
     */
    private val deletingCommentId: () -> Int?,

    private val onEditClicked: (TaskComment) -> Unit,

    private val onDeleteClicked: (TaskComment) -> Unit

) : ListAdapter<TaskComment, TaskCommentAdapter.CommentViewHolder>(
    CommentDiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {

        val binding =
            ItemTaskCommentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return CommentViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: CommentViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    /**
     * updatingCommentId ve deletingCommentId TaskComment modelinin parçası
     * olmadığı için state değişince satırları tekrar bind ediyoruz.
     */
    fun refreshOperationState() {

        if (itemCount > 0) {

            notifyItemRangeChanged(
                0,
                itemCount
            )
        }
    }

    inner class CommentViewHolder(
        private val binding: ItemTaskCommentBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            comment: TaskComment
        ) {

            binding.textViewCommentAvatar.text =
                comment.userFullName
                    .trim()
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"

            binding.textViewCommentUserName.text =
                comment.userFullName

            binding.textViewCommentEmail.text =
                comment.userEmail

            binding.textViewCommentContent.text =
                comment.content

            binding.textViewCommentCreatedAt.text =
                dateTimeFormatter.formatUtcDateTime(
                    comment.createdAtUtc
                )

            binding.textViewCommentEdited.isVisible =
                comment.updatedAtUtc != null

            // -----------------------------------------------------------------
            // PERMISSIONS
            // -----------------------------------------------------------------

            binding.buttonEditComment.isVisible =
                comment.canEdit

            binding.buttonDeleteComment.isVisible =
                comment.canDelete

            // -----------------------------------------------------------------
            // OPERATION STATE
            // -----------------------------------------------------------------

            val isUpdating =
                updatingCommentId() ==
                        comment.id

            val isDeleting =
                deletingCommentId() ==
                        comment.id

            val isProcessing =
                isUpdating ||
                        isDeleting

            binding.buttonEditComment.isEnabled =
                !isProcessing

            binding.buttonDeleteComment.isEnabled =
                !isProcessing

            binding.progressIndicatorCommentOperation.isVisible =
                isProcessing

            // -----------------------------------------------------------------
            // CLICK
            // -----------------------------------------------------------------

            binding.buttonEditComment.setOnClickListener {

                onEditClicked(
                    comment
                )
            }

            binding.buttonDeleteComment.setOnClickListener {

                onDeleteClicked(
                    comment
                )
            }
        }
    }

    private object CommentDiffCallback :
        DiffUtil.ItemCallback<TaskComment>() {

        override fun areItemsTheSame(
            oldItem: TaskComment,
            newItem: TaskComment
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TaskComment,
            newItem: TaskComment
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}