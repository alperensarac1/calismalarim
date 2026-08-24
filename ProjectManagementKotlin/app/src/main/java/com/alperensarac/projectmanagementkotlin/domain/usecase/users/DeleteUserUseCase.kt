package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Admin kullanıcı silme işlemi.
 *
 * Backend ayrıca mevcut kullanıcının ID'sini UserService'e gönderiyor;
 * kendi hesabını silme gibi iş kuralları backend tarafında korunmaya
 * devam eder.
 */
class DeleteUserUseCase @Inject constructor(
    private val usersRepository:
    UsersRepository
) {

    suspend operator fun invoke(
        userId: Int
    ): AppResult<Unit> {

        return usersRepository
            .deleteUser(
                userId = userId
            )
    }
}