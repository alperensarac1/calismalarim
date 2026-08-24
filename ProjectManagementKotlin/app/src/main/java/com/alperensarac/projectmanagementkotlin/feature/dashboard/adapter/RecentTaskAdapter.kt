package com.alperensarac.projectmanagementkotlin.feature.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemDashboardRecentTaskBinding
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardRecentTask

/**
 * Dashboard son görev listesini gösteren ListAdapter.
 *
 * ListAdapter ve DiffUtil yalnızca değişen satırların güncellenmesini sağlar.
 */
class RecentTaskAdapter(
    private val dateTimeFormatter: DateTimeFormatter,
    private val onTaskClicked: (DashboardRecentTask) -> Unit
) : ListAdapter<DashboardRecentTask, RecentTaskAdapter.TaskViewHolder>(
    TaskDiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {
        val binding = ItemDashboardRecentTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemDashboardRecentTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            task: DashboardRecentTask
        ) {
            val context = binding.root.context

            binding.textViewTaskTitle.text =
                task.title

            binding.textViewTaskProject.text =
                task.projectName

            binding.textViewTaskStatus.text =
                task.status

            binding.textViewTaskPriority.text =
                task.priority

            binding.textViewTaskAssignedUser.text =
                task.assignedToUserFullName
                    ?.takeIf { it.isNotBlank() }
                    ?: context.getString(
                        R.string.dashboard_task_unassigned
                    )

            binding.textViewTaskDueDate.text =
                if (task.dueDateUtc.isNullOrBlank()) {
                    context.getString(
                        R.string.dashboard_task_no_due_date
                    )
                } else {
                    context.getString(
                        R.string.dashboard_task_due_date_format,
                        dateTimeFormatter.formatUtcDateTime(
                            task.dueDateUtc
                        )
                    )
                }

            binding.textViewTaskOverdue.text =
                if (task.isOverdue) {
                    context.getString(
                        R.string.dashboard_task_overdue
                    )
                } else {
                    ""
                }

            binding.textViewTaskOverdue.visibility =
                if (task.isOverdue) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

            binding.root.setOnClickListener {
                onTaskClicked(task)
            }
        }
    }

    private object TaskDiffCallback :
        DiffUtil.ItemCallback<DashboardRecentTask>() {

        override fun areItemsTheSame(
            oldItem: DashboardRecentTask,
            newItem: DashboardRecentTask
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DashboardRecentTask,
            newItem: DashboardRecentTask
        ): Boolean {
            return oldItem == newItem
        }
    }
}