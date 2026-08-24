package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Admin kullanıcı detayını getirir.
 */
class GetUserByIdUseCase @Inject constructor(
    private val usersRepository:
    UsersRepository
) {

    suspend operator fun invoke(
        userId: Int
    ): AppResult<User> {

        return usersRepository
            .getUserById(
                userId = userId
            )
    }
}