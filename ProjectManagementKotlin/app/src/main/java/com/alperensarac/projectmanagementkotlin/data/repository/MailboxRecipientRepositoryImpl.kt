package com.alperensarac.projectmanagementkotlin.data.repository

import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.NetworkError
import com.alperensarac.projectmanagementkotlin.core.network.parser.NetworkErrorMapper
import com.alperensarac.projectmanagementkotlin.data.mapper.mailbox.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.MailboxRecipientApi
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxRecipientUser
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRecipientRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MailboxRecipientRepositoryImpl @Inject constructor(
    private val api: MailboxRecipientApi,
    private val networkErrorMapper: NetworkErrorMapper
) : MailboxRecipientRepository {

    override suspend fun searchRecipients(
        search: String?
    ): AppResult<List<MailboxRecipientUser>> {

        return try {

            val response =
                api.getUsers(
                    page = 1,
                    pageSize = PAGE_SIZE,
                    search =
                    search
                        ?.trim()
                        ?.takeIf {
                            it.isNotBlank()
                        },
                    isActive = true
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return AppResult.Error(
                    NetworkError.Unknown(
                        message =
                        response.message
                            .ifBlank {
                                "Kullanıcılar getirilemedi."
                            }
                    )
                )
            }

            AppResult.Success(
                data =
                data.items
                    .filter {
                        it.isActive
                    }
                    .map {
                        it.toDomain()
                    },
                message =
                response.message
            )

        } catch (throwable: Throwable) {

            AppResult.Error(
                networkErrorMapper.map(
                    throwable
                )
            )
        }
    }

    private companion object {

        /*
         * Tek mesajda maksimum 50 alıcı seçilebildiği için
         * autocomplete sonucunda ilk 50 sonucu çekiyoruz.
         */
        const val PAGE_SIZE =
            50
    }
}