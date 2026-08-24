package com.alperensarac.projectmanagementkotlin.feature.projects.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.ItemProjectMemberBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember

/**
 * Proje üye listesi adapter'ı.
 */
class ProjectMemberAdapter(
    private val dateTimeFormatter: DateTimeFormatter,

    /**
     * Yönetim butonlarının görünürlüğünü belirler.
     */
    private val canManageMembers: () -> Boolean,

    /**
     * Şu anda mutation yapılan kullanıcının userId değeri.
     */
    private val processingUserId: () -> Int?,

    /**
     * Rol değiştir callback.
     */
    private val onChangeRoleClicked: (ProjectMember) -> Unit,

    /**
     * Projeden çıkar callback.
     */
    private val onRemoveMemberClicked: (ProjectMember) -> Unit

) : ListAdapter<ProjectMember, ProjectMemberAdapter.ProjectMemberViewHolder>(
    ProjectMemberDiffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProjectMemberViewHolder {

        val binding =
            ItemProjectMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ProjectMemberViewHolder(
            binding = binding
        )
    }

    override fun onBindViewHolder(
        holder: ProjectMemberViewHolder,
        position: Int
    ) {
        holder.bind(
            member = getItem(position)
        )
    }

    /**
     * State değiştiğinde permission/loading gibi List item'ın kendisinin
     * parçası olmayan bilgiler nedeniyle satırları yeniden bind etmek için
     * kullanacağız.
     */
    fun refreshUiState() {
        if (itemCount > 0) {
            notifyItemRangeChanged(
                0,
                itemCount
            )
        }
    }

    inner class ProjectMemberViewHolder(
        private val binding: ItemProjectMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            member: ProjectMember
        ) {

            val context =
                binding.root.context

            // -----------------------------------------------------------------
            // AVATAR
            // -----------------------------------------------------------------

            binding.textViewMemberAvatar.text =
                member.fullName
                    .trim()
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"

            // -----------------------------------------------------------------
            // BASIC INFORMATION
            // -----------------------------------------------------------------

            binding.textViewMemberName.text =
                member.fullName

            binding.textViewMemberEmail.text =
                member.email

            binding.textViewMemberProjectRole.text =
                context.getString(
                    R.string.project_detail_member_role_format,
                    member.projectRole
                )

            binding.textViewMemberSystemRole.text =
                context.getString(
                    R.string.project_detail_system_role_format,
                    member.systemRole
                )

            binding.textViewMemberJoinedAt.text =
                context.getString(
                    R.string.project_detail_joined_at_format,
                    dateTimeFormatter.formatUtcDateTime(
                        member.joinedAtUtc
                    )
                )

            // -----------------------------------------------------------------
            // BADGES
            // -----------------------------------------------------------------

            binding.textViewProjectOwnerBadge.isVisible =
                member.isProjectOwner

            binding.textViewInactiveBadge.isVisible =
                !member.isActive

            // -----------------------------------------------------------------
            // PERMISSION
            // -----------------------------------------------------------------

            val canManage =
                canManageMembers() &&
                        !member.isProjectOwner

            binding.layoutMemberActions.isVisible =
                canManage

            /*
             * Mutation bu kullanıcı üzerinde devam ediyorsa butonlar
             * disable edilir.
             */
            val isProcessing =
                processingUserId() ==
                        member.userId

            binding.buttonChangeProjectMemberRole.isEnabled =
                !isProcessing

            binding.buttonRemoveProjectMember.isEnabled =
                !isProcessing

            binding.progressIndicatorMemberOperation.isVisible =
                isProcessing

            // -----------------------------------------------------------------
            // CLICK
            // -----------------------------------------------------------------

            binding.buttonChangeProjectMemberRole
                .setOnClickListener {
                    onChangeRoleClicked(member)
                }

            binding.buttonRemoveProjectMember
                .setOnClickListener {
                    onRemoveMemberClicked(member)
                }
        }
    }

    private object ProjectMemberDiffCallback :
        DiffUtil.ItemCallback<ProjectMember>() {

        override fun areItemsTheSame(
            oldItem: ProjectMember,
            newItem: ProjectMember
        ): Boolean {
            /*
             * Endpoint işlemleri userId üzerinden yaptığı için UI identity
             * olarak da userId kullanıyoruz.
             */
            return oldItem.userId ==
                    newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: ProjectMember,
            newItem: ProjectMember
        ): Boolean {
            return oldItem ==
                    newItem
        }
    }
}