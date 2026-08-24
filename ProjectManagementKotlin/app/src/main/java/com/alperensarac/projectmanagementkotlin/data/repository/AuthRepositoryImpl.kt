package com.alperensarac.projectmanagementkotlin.data.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.datastore.token.TokenStorage
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.auth.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.AuthApi
import com.alperensarac.projectmanagementkotlin.data.remote.api.RefreshApi
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.LoginRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.LogoutRequestDto
import com.alperensarac.projectmanagementkotlin.data.remote.dto.auth.RefreshTokenRequestDto
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthSession
import com.alperensarac.projectmanagementkotlin.domain.model.auth.AuthUser
import com.alperensarac.projectmanagementkotlin.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepository interface'inin network ve token storage kullanan
 * gerçek implementasyonudur.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val refreshApi: RefreshApi,
    private val tokenStorage: TokenStorage,
    private val networkErrorMapper: NetworkErrorMapper
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<AuthSession> {
        return try {
            val response = authApi.login(
                request = LoginRequestDto(
                    email = email.trim(),
                    password = password
                )
            )

            val responseData = response.data

            if (!response.success || responseData == null) {
                return AppResult.Error(
                    error = createBackendBusinessError(
                        message = response.message,
                        errors = response.errors
                    )
                )
            }

            tokenStorage.saveTokens(
                accessToken = responseData.accessToken,
                refreshToken = responseData.refreshToken,
                accessTokenExpiresAtUtc = responseData.expiresAtUtc
            )

            AppResult.Success(
                data = responseData.toDomain(),
                message = response.message
            )
        } catch (throwable: Throwable) {
            AppResult.Error(
                error = networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun refreshSession(): AppResult<AuthSession> {
        return try {
            val tokenSnapshot =
                tokenStorage.getTokenSnapshot()

            val refreshToken =
                tokenSnapshot.refreshToken

            if (refreshToken.isNullOrBlank()) {
                return AppResult.Error(
                    error = NetworkError.Unauthorized(
                        message = "Kayıtlı oturum bulunamadı. Lütfen tekrar giriş yapın."
                    )
                )
            }

            /*
             * Manuel session refresh de authenticator içermeyen ayrı servis
             * üzerinden yapılır.
             */
            val response = refreshApi.refresh(
                request = RefreshTokenRequestDto(
                    refreshToken = refreshToken
                )
            )

            val responseData = response.data

            if (!response.success || responseData == null) {
                tokenStorage.clearTokens()

                return AppResult.Error(
                    error = NetworkError.Unauthorized(
                        message = response.message.ifBlank {
                            "Oturum yenilenemedi. Lütfen tekrar giriş yapın."
                        }
                    )
                )
            }

            tokenStorage.saveTokens(
                accessToken = responseData.accessToken,
                refreshToken = responseData.refreshToken,
                accessTokenExpiresAtUtc = responseData.expiresAtUtc
            )

            AppResult.Success(
                data = responseData.toDomain(),
                message = response.message
            )
        } catch (throwable: Throwable) {
            val mappedError =
                networkErrorMapper.map(throwable)

            if (mappedError is NetworkError.Unauthorized) {
                tokenStorage.clearTokens()
            }

            AppResult.Error(mappedError)
        }
    }

    override suspend fun getCurrentUser(): AppResult<AuthUser> {
        return try {
            val response =
                authApi.getMe()

            val responseData =
                response.data

            if (!response.success || responseData == null) {
                return AppResult.Error(
                    error = createBackendBusinessError(
                        message = response.message,
                        errors = response.errors
                    )
                )
            }

            AppResult.Success(
                data = responseData.toDomain(),
                message = response.message
            )
        } catch (throwable: Throwable) {
            AppResult.Error(
                error = networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        val tokenSnapshot =
            tokenStorage.getTokenSnapshot()

        val refreshToken =
            tokenSnapshot.refreshToken

        if (refreshToken.isNullOrBlank()) {
            tokenStorage.clearTokens()

            return AppResult.Success(
                data = Unit,
                message = "Oturum kapatıldı."
            )
        }

        return try {
            val response = authApi.logout(
                request = LogoutRequestDto(
                    refreshToken = refreshToken
                )
            )

            /*
             * Kullanıcının logout isteği sonrasında backend sonucu ne olursa
             * olsun cihazdaki tokenları temizleriz.
             */
            tokenStorage.clearTokens()

            if (!response.success) {
                AppResult.Error(
                    error = createBackendBusinessError(
                        message = response.message,
                        errors = response.errors
                    )
                )
            } else {
                AppResult.Success(
                    data = Unit,
                    message = response.message
                )
            }
        } catch (throwable: Throwable) {
            tokenStorage.clearTokens()

            AppResult.Error(
                error = networkErrorMapper.map(throwable)
            )
        }
    }

    override suspend fun clearLocalSession() {
        tokenStorage.clearTokens()
    }

    private fun createBackendBusinessError(
        message: String,
        errors: Map<String, List<String>>?
    ): NetworkError {
        if (!errors.isNullOrEmpty()) {
            val combinedMessage = errors.entries
                .flatMap { (field, messages) ->
                    messages.map { fieldMessage ->
                        "$field: $fieldMessage"
                    }
                }
                .joinToString(separator = "\n")

            return NetworkError.Validation(
                message = combinedMessage.ifBlank {
                    message.ifBlank {
                        "Gönderilen bilgileri kontrol edin."
                    }
                },
                fieldErrors = errors
            )
        }

        return NetworkError.Unknown(
            message = message.ifBlank {
                "İşlem tamamlanamadı."
            }
        )
    }
}