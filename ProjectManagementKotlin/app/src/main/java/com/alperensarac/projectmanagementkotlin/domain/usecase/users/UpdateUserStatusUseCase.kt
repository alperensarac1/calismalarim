package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Kullanıcı hesabını aktif/pasif yapar.
 */
class UpdateUserStatusUseCase @Inject constructor(
    private val usersRepository:
    UsersRepository
) {

    suspend operator fun invoke(
        userId: Int,
        isActive: Boolean
    ): AppResult<User> {

        return usersRepository
            .updateUserStatus(
                userId = userId,
                isActive = isActive
            )
    }
}