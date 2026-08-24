package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Kullanıcının profil/yönetim bilgilerini günceller.
 *
 * Aktiflik burada değiştirilmez.
 */
class UpdateUserUseCase @Inject constructor(
    private val usersRepository:
    UsersRepository
) {

    suspend operator fun invoke(
        userId: Int,
        firstName: String,
        lastName: String,
        email: String,
        role: UserRole,
        department: String?
    ): AppResult<User> {

        return usersRepository.updateUser(
            userId = userId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            role = role,
            department = department
        )
    }
}