package com.alperensarac.projectmanagementkotlin.feature.tasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemTaskBinding
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task

/**
 * Sayfalı görev listesini gösterir.
 */
class TaskPagingAdapter(
    private val dateTimeFormatter: DateTimeFormatter,
    private val onTaskClicked: (Task) -> Unit
) : PagingDataAdapter<
        Task,
        TaskPagingAdapter.TaskViewHolder
        >(TaskDiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {

        val binding =
            ItemTaskBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return TaskViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {

        getItem(position)
            ?.let(
                holder::bind
            )
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            task: Task
        ) {

            val context =
                binding.root.context

            // -----------------------------------------------------------------
            // TITLE / PROJECT
            // -----------------------------------------------------------------

            binding.textViewTaskTitle.text =
                task.title

            binding.textViewTaskProject.text =
                task.projectName

            // -----------------------------------------------------------------
            // DESCRIPTION
            // -----------------------------------------------------------------

            binding.textViewTaskDescription.text =
                task.description
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: context.getString(
                        R.string.tasks_no_description
                    )

            // -----------------------------------------------------------------
            // STATUS / PRIORITY
            // -----------------------------------------------------------------

            binding.textViewTaskStatus.text =
                task.status

            binding.textViewTaskPriority.text =
                task.priority

            // -----------------------------------------------------------------
            // ASSIGNED USER
            // -----------------------------------------------------------------

            binding.textViewTaskAssignedUser.text =
                context.getString(
                    R.string.tasks_assigned_user_format,

                    task.assignedToUserFullName
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: context.getString(
                            R.string.tasks_unassigned
                        )
                )

            // -----------------------------------------------------------------
            // DUE DATE
            // -----------------------------------------------------------------

            binding.textViewTaskDueDate.text =
                task.dueDateUtc
                    ?.let { date ->

                        context.getString(
                            R.string.tasks_due_date_format,

                            dateTimeFormatter
                                .formatUtcDateTime(
                                    date
                                )
                        )
                    }
                    ?: context.getString(
                        R.string.tasks_no_due_date
                    )

            // -----------------------------------------------------------------
            // HOURS
            // -----------------------------------------------------------------

            binding.textViewTaskHours.text =
                context.getString(
                    R.string.tasks_hours_format,

                    task.estimatedHours
                        ?: 0.0,

                    task.actualHours
                )

            // -----------------------------------------------------------------
            // COMMENTS
            // -----------------------------------------------------------------

            binding.textViewTaskCommentCount.text =
                context.getString(
                    R.string.tasks_comment_count_format,
                    task.commentCount
                )

            // -----------------------------------------------------------------
            // OVERDUE
            // -----------------------------------------------------------------

            binding.textViewTaskOverdue.isVisible =
                task.isOverdue

            // -----------------------------------------------------------------
            // CLICK
            // -----------------------------------------------------------------

            binding.root.setOnClickListener {

                onTaskClicked(
                    task
                )
            }
        }
    }

    private object TaskDiffCallback :
        DiffUtil.ItemCallback<Task>() {

        override fun areItemsTheSame(
            oldItem: Task,
            newItem: Task
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Task,
            newItem: Task
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}