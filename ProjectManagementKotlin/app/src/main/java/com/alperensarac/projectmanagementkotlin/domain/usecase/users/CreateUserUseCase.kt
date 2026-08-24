package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject

/**
 * Yeni kullanıcı oluşturma use case'i.
 */
class CreateUserUseCase @Inject constructor(
    private val usersRepository:
    UsersRepository
) {

    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole,
        department: String?,
        isActive: Boolean
    ): AppResult<User> {

        return usersRepository.createUser(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            role = role,
            department = department,
            isActive = isActive
        )
    }
}