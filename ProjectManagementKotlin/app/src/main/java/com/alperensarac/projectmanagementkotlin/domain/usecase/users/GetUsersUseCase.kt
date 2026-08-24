package com.alperensarac.projectmanagementkotlin.domain.usecase.users

import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserFilter
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Admin kullanıcı listesini getirir.
 */
class GetUsersUseCase @Inject constructor(
    private val usersRepository:
    UsersRepository
) {

    operator fun invoke(
        filter: UserFilter
    ): Flow<PagingData<User>> {

        return usersRepository
            .getUsers(
                filter = filter
            )
    }
}