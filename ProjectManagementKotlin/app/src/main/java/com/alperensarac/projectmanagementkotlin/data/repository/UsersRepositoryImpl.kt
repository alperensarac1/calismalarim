package com.alperensarac.projectmanagementkotlin.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.users.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.UsersApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.CreateUserRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.ResetUserPasswordRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.UpdateUserRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.users.UpdateUserStatusRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.paging.UsersPagingSource
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserFilter
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Admin kullanıcı yönetiminin gerçek Retrofit/Paging implementasyonudur.
 */
@Singleton
class UsersRepositoryImpl @Inject constructor(
    private val usersApi: UsersApi,
    private val networkErrorMapper: NetworkErrorMapper
) : UsersRepository {

    // =========================================================================
    // LIST
    // =========================================================================

    override fun getUsers(
        filter: UserFilter
    ): Flow<PagingData<User>> {

        return Pager(
            config =
            PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PREFETCH_DISTANCE,
                enablePlaceholders = false
            ),

            pagingSourceFactory = {

                UsersPagingSource(
                    usersApi = usersApi,
                    filter = filter
                )
            }
        ).flow
    }

    // =========================================================================
    // DETAIL
    // =========================================================================

    override suspend fun getUserById(
        userId: Int
    ): AppResult<User> {

        return try {

            val response =
                usersApi.getUserById(
                    userId = userId
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcı bilgileri alınamadı."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    override suspend fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        role: UserRole,
        department: String?,
        isActive: Boolean
    ): AppResult<User> {

        return try {

            val response =
                usersApi.createUser(
                    request =
                    CreateUserRequestDto(
                        firstName =
                        firstName.trim(),

                        lastName =
                        lastName.trim(),

                        email =
                        email.trim(),

                        password =
                        password,

                        role =
                        role.apiValue,

                        department =
                        department
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            },

                        isActive =
                        isActive
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcı oluşturulamadı."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    override suspend fun updateUser(
        userId: Int,
        firstName: String,
        lastName: String,
        email: String,
        role: UserRole,
        department: String?
    ): AppResult<User> {

        return try {

            val response =
                usersApi.updateUser(
                    userId = userId,

                    request =
                    UpdateUserRequestDto(
                        firstName =
                        firstName.trim(),

                        lastName =
                        lastName.trim(),

                        email =
                        email.trim(),

                        role =
                        role.apiValue,

                        department =
                        department
                            ?.trim()
                            ?.takeIf {
                                it.isNotBlank()
                            }
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcı güncellenemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // STATUS
    // =========================================================================

    override suspend fun updateUserStatus(
        userId: Int,
        isActive: Boolean
    ): AppResult<User> {

        return try {

            val response =
                usersApi.updateUserStatus(
                    userId = userId,

                    request =
                    UpdateUserStatusRequestDto(
                        isActive = isActive
                    )
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcı durumu değiştirilemedi."
                    )
                )
            }

            AppResult.Success(
                data = data.toDomain(),
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // RESET PASSWORD
    // =========================================================================

    override suspend fun resetUserPassword(
        userId: Int,
        newPassword: String
    ): AppResult<Unit> {

        return try {

            val response =
                usersApi.resetUserPassword(
                    userId = userId,

                    request =
                    ResetUserPasswordRequestDto(
                        newPassword =
                        newPassword
                    )
                )

            if (
                !response.success
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcının şifresi sıfırlanamadı."
                    )
                )
            }

            AppResult.Success(
                data = Unit,
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    override suspend fun deleteUser(
        userId: Int
    ): AppResult<Unit> {

        return try {

            val response =
                usersApi.deleteUser(
                    userId = userId
                )

            if (
                !response.success
            ) {

                return AppResult.Error(
                    createBusinessError(
                        message = response.message,
                        errors = response.errors,
                        fallbackMessage =
                        "Kullanıcı silinemedi."
                    )
                )
            }

            AppResult.Success(
                data = Unit,
                message = response.message
            )

        } catch (
            throwable: Throwable
        ) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    // =========================================================================
    // BUSINESS ERROR
    // =========================================================================

    private fun createBusinessError(
        message: String,
        errors: Map<String, List<String>>?,
        fallbackMessage: String
    ): NetworkError {

        if (
            !errors.isNullOrEmpty()
        ) {

            val validationMessage =
                errors.values
                    .flatten()
                    .joinToString("\n")
                    .ifBlank {

                        message.ifBlank {
                            fallbackMessage
                        }
                    }

            return NetworkError.Validation(
                message = validationMessage,
                fieldErrors = errors
            )
        }

        return NetworkError.Unknown(
            message =
            message.ifBlank {
                fallbackMessage
            }
        )
    }

    private companion object {

        const val PAGE_SIZE =
            20

        const val PREFETCH_DISTANCE =
            5
    }
}