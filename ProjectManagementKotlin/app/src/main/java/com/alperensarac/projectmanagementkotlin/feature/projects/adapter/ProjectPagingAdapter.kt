package com.alperensarac.projectmanagementkotlin.feature.projects.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemProjectBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project

/**
 * Sayfalı proje listesini gösteren PagingDataAdapter.
 */
class ProjectPagingAdapter(
    private val dateTimeFormatter: DateTimeFormatter,
    private val onProjectClicked: (Project) -> Unit
) : PagingDataAdapter<Project, ProjectPagingAdapter.ProjectViewHolder>(
    ProjectDiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProjectViewHolder,
        position: Int
    ) {
        getItem(position)?.let(holder::bind)
    }

    inner class ProjectViewHolder(
        private val binding: ItemProjectBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            project: Project
        ) {
            val context =
                binding.root.context

            binding.textViewProjectName.text =
                project.name

            binding.textViewProjectDescription.text =
                project.description
                    ?.takeIf { it.isNotBlank() }
                    ?: context.getString(
                        R.string.projects_no_description
                    )

            binding.textViewProjectStatus.text =
                project.status

            binding.textViewProjectOwner.text =
                context.getString(
                    R.string.projects_owner_format,
                    project.ownerFullName
                )

            binding.textViewProjectCounts.text =
                context.getString(
                    R.string.projects_counts_format,
                    project.memberCount,
                    project.taskCount
                )

            binding.textViewProjectStartDate.text =
                context.getString(
                    R.string.projects_start_date_format,
                    dateTimeFormatter.formatUtcDateTime(
                        project.startDateUtc
                    )
                )

            binding.textViewProjectEndDate.text =
                if (project.endDateUtc == null) {
                    context.getString(
                        R.string.projects_no_end_date
                    )
                } else {
                    context.getString(
                        R.string.projects_end_date_format,
                        dateTimeFormatter.formatUtcDateTime(
                            project.endDateUtc
                        )
                    )
                }

            binding.textViewArchived.isVisible =
                project.isArchived

            binding.root.setOnClickListener {
                onProjectClicked(project)
            }
        }
    }

    private object ProjectDiffCallback :
        DiffUtil.ItemCallback<Project>() {

        override fun areItemsTheSame(
            oldItem: Project,
            newItem: Project
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Project,
            newItem: Project
        ): Boolean {
            return oldItem == newItem
        }
    }
}