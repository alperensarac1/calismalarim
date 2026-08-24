package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Admin tarafından kullanıcı şifresini sıfırlar.
 */
class ResetUserPasswordUseCase @Inject constructor(
    private val usersRepository:
    UsersRepository
) {

    suspend operator fun invoke(
        userId: Int,
        newPassword: String
    ): AppResult<Unit> {

        return usersRepository
            .resetUserPassword(
                userId = userId,
                newPassword = newPassword
            )
    }
}