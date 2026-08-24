package com.alperensarac.projectmanagementkotlin.feature.users.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.ItemUserBinding
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole

/**
 * Admin kullanıcı listesi Paging adapter'ıdır.
 */
class UserPagingAdapter(
    private val onUserClicked: (
        User
    ) -> Unit
) : PagingDataAdapter<
        User,
        UserPagingAdapter.UserViewHolder
        >(
    USER_COMPARATOR
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {

        val binding =
            ItemUserBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return UserViewHolder(
            binding = binding,
            onUserClicked = onUserClicked
        )
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {

        getItem(
            position
        )?.let(
            holder::bind
        )
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onUserClicked: (User) -> Unit
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        private var currentUser:
                User? =
            null

        init {

            binding.root
                .setOnClickListener {

                    currentUser?.let(
                        onUserClicked
                    )
                }
        }

        fun bind(
            user: User
        ) {

            currentUser =
                user

            binding.textViewUserFullName.text =
                user.fullName

            binding.textViewUserEmail.text =
                user.email

            binding.textViewUserRole.text =
                UserRole
                    .fromApiValue(
                        user.role
                    )
                    ?.displayName
                    ?: user.role

            binding.textViewUserDepartment.text =
                user.department
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: binding.root
                        .context
                        .getString(
                            R.string.user_department_not_defined
                        )

            binding.textViewUserAvatar.text =
                user.fullName
                    .trim()
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"

            if (
                user.isActive
            ) {

                binding.textViewUserStatus.text =
                    binding.root
                        .context
                        .getString(
                            R.string.user_status_active
                        )

                binding.textViewUserStatus
                    .setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.user_status_active
                        )
                    )

            } else {

                binding.textViewUserStatus.text =
                    binding.root
                        .context
                        .getString(
                            R.string.user_status_passive
                        )

                binding.textViewUserStatus
                    .setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.user_status_passive
                        )
                    )
            }
        }
    }

    private companion object {

        val USER_COMPARATOR =
            object :
                DiffUtil.ItemCallback<User>() {

                override fun areItemsTheSame(
                    oldItem: User,
                    newItem: User
                ): Boolean {

                    return oldItem.id ==
                            newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: User,
                    newItem: User
                ): Boolean {

                    return oldItem ==
                            newItem
                }
            }
    }
}