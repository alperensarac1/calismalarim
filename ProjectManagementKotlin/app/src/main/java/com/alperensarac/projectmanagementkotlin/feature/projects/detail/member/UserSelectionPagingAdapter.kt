package com.alperensarac.projectmanagementkotlin.feature.projects.detail.member

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.ItemSelectableUserBinding
import com.alperensarac.projectmanagementkotlin.domain.model.users.User

/**
 * Kullanıcı seçim ekranlarında kullanılabilen ortak Paging adapter'dır.
 *
 * Şu anda:
 *
 * - projeye üye ekleme
 * - proje sahibi seçme
 *
 * ekranlarında kullanılabilir.
 *
 * Adapter'ın görevi yalnızca:
 *
 * - kullanıcıyı göstermek
 * - seçili kullanıcıyı işaretlemek
 * - kullanıcı tıklamasını dışarı iletmek
 *
 * İş kuralları ViewModel / use-case katmanında kalır.
 */
class UserSelectionPagingAdapter(

    /**
     * Şu anda seçili olan User.id değerini döndürür.
     *
     * null:
     * Hiçbir kullanıcı seçili değildir.
     */
    private val selectedUserId:
        () -> Int?,

    /**
     * Kullanıcı satırına tıklandığında çağrılır.
     */
    private val onUserClicked:
        (User) -> Unit

) : PagingDataAdapter<
        User,
        UserSelectionPagingAdapter.UserViewHolder
        >(
    UserDiffCallback
) {

    // =========================================================================
    // CREATE VIEW HOLDER
    // =========================================================================

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {

        val binding =
            ItemSelectableUserBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return UserViewHolder(
            binding
        )
    }

    // =========================================================================
    // BIND
    // =========================================================================

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {

        /*
         * PagingDataAdapter bazı pozisyonlarda null döndürebilir.
         *
         * Placeholder kullanmasak bile güvenli davranıyoruz.
         */
        getItem(
            position
        )?.let { user ->

            holder.bind(
                user
            )
        }
    }

    // =========================================================================
    // REFRESH SELECTION
    // =========================================================================

    /**
     * selectedUserId adapter item modelinin içinde değildir.
     *
     * Örneğin:
     *
     * kullanıcı A seçildi
     *
     * sonra:
     *
     * kullanıcı B seçildi
     *
     * Bu durumda:
     *
     * A satırının seçili görünümünü kaldırmak,
     * B satırını seçili göstermek gerekir.
     *
     * Bundan dolayı görünür dataset'i yeniden bind ediyoruz.
     */
    fun refreshSelection() {

        if (
            itemCount <= 0
        ) {
            return
        }

        notifyItemRangeChanged(
            0,
            itemCount
        )
    }

    // =========================================================================
    // VIEW HOLDER
    // =========================================================================

    inner class UserViewHolder(
        private val binding:
        ItemSelectableUserBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            user: User
        ) {

            val context =
                binding.root.context

            // -----------------------------------------------------------------
            // AVATAR
            // -----------------------------------------------------------------

            /*
             * Gerçek profil resmi olmadığı için kullanıcının adının
             * ilk harfini avatar olarak gösteriyoruz.
             */
            binding.textViewSelectableUserAvatar
                .text =
                user.fullName
                    .trim()
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"

            // -----------------------------------------------------------------
            // NAME
            // -----------------------------------------------------------------

            binding.textViewSelectableUserName
                .text =
                user.fullName

            // -----------------------------------------------------------------
            // EMAIL
            // -----------------------------------------------------------------

            binding.textViewSelectableUserEmail
                .text =
                user.email

            // -----------------------------------------------------------------
            // SYSTEM ROLE
            // -----------------------------------------------------------------

            binding.textViewSelectableUserRole
                .text =
                context.getString(
                    R.string.add_member_system_role_format,
                    user.role
                )

            // -----------------------------------------------------------------
            // DEPARTMENT
            // -----------------------------------------------------------------

            binding.textViewSelectableUserDepartment
                .text =
                user.department
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: context.getString(
                        R.string.add_member_department_empty
                    )

            // -----------------------------------------------------------------
            // SELECTED
            // -----------------------------------------------------------------

            val isSelected =
                selectedUserId() ==
                        user.id

            binding.iconSelectedUser
                .isVisible =
                isSelected

            /*
             * item root MaterialCardView / Checkable yapısındaysa
             * seçili görünümünü de buradan yönetiyoruz.
             */
            binding.root.isChecked =
                isSelected

            // -----------------------------------------------------------------
            // CLICK
            // -----------------------------------------------------------------

            binding.root
                .setOnClickListener {

                    onUserClicked(
                        user
                    )
                }
        }
    }

    // =========================================================================
    // DIFF CALLBACK
    // =========================================================================

    private object UserDiffCallback :
        DiffUtil.ItemCallback<User>() {

        override fun areItemsTheSame(
            oldItem: User,
            newItem: User
        ): Boolean {

            /*
             * Aynı backend kullanıcısı mı?
             */
            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: User,
            newItem: User
        ): Boolean {

            /*
             * User data class olduğu için structural equality yeterlidir.
             */
            return oldItem ==
                    newItem
        }
    }
}