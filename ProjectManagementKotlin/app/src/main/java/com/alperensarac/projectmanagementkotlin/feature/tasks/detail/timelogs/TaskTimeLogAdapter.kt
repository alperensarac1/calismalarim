package com.alperensarac.projectmanagementkotlin.feature.tasks.detail.timelogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemTaskTimeLogBinding
import com.alperensarac.projectmanagementkotlin.domain.model.timelogs.TaskTimeLog

/**
 * Görev zaman kayıtları RecyclerView adapter'ı.
 *
 * canEdit / canDelete bilgileri backend'den gelir.
 */
class TaskTimeLogAdapter(
    private val dateTimeFormatter: DateTimeFormatter,

    private val processingTimeLogId: () -> Int?,

    private val onEditClicked: (TaskTimeLog) -> Unit,

    private val onDeleteClicked: (TaskTimeLog) -> Unit

) : ListAdapter<TaskTimeLog, TaskTimeLogAdapter.TimeLogViewHolder>(
    DiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimeLogViewHolder {

        val binding =
            ItemTaskTimeLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return TimeLogViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TimeLogViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    fun refreshOperationState() {

        if (itemCount > 0) {

            notifyItemRangeChanged(
                0,
                itemCount
            )
        }
    }

    inner class TimeLogViewHolder(
        private val binding: ItemTaskTimeLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: TaskTimeLog
        ) {

            val context =
                binding.root.context

            binding.textViewTimeLogUser.text =
                item.userFullName

            binding.textViewTimeLogEmail.text =
                item.userEmail

            binding.textViewTimeLogHours.text =
                context.getString(
                    R.string.time_log_hours_format,
                    item.hours
                )

            binding.textViewTimeLogDescription.text =
                item.description
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: context.getString(
                        R.string.time_log_no_description
                    )

            binding.textViewTimeLogWorkDate.text =
                context.getString(
                    R.string.time_log_work_date_format,

                    dateTimeFormatter
                        .formatUtcDateTime(
                            item.workDateUtc
                        )
                )

            binding.buttonEditTimeLog.isVisible =
                item.canEdit

            binding.buttonDeleteTimeLog.isVisible =
                item.canDelete

            val isProcessing =
                processingTimeLogId() ==
                        item.id

            binding.progressIndicatorTimeLogOperation.isVisible =
                isProcessing

            binding.buttonEditTimeLog.isEnabled =
                !isProcessing

            binding.buttonDeleteTimeLog.isEnabled =
                !isProcessing

            binding.buttonEditTimeLog
                .setOnClickListener {

                    onEditClicked(item)
                }

            binding.buttonDeleteTimeLog
                .setOnClickListener {

                    onDeleteClicked(item)
                }
        }
    }

    private object DiffCallback :
        DiffUtil.ItemCallback<TaskTimeLog>() {

        override fun areItemsTheSame(
            oldItem: TaskTimeLog,
            newItem: TaskTimeLog
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TaskTimeLog,
            newItem: TaskTimeLog
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}