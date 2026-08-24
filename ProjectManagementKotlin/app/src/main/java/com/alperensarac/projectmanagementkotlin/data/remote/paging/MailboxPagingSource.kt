package com.alperensarac.projectmanagementkotlin.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.alperensarac.projectmanagementkotlin.data.mapper.mailbox.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.MailboxApi
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFilter
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxFolder
import com.alperensarac.projectmanagementkotlin.domain.model.mailbox.MailboxMessage

/**
 * Inbox / Sent için ortak PagingSource.
 */
class MailboxPagingSource(
    private val mailboxApi: MailboxApi,
    private val folder: MailboxFolder,
    private val filter: MailboxFilter
) : PagingSource<Int, MailboxMessage>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, MailboxMessage> {

        val page =
            params.key ?: FIRST_PAGE

        return try {

            val response =
                when (folder) {

                    MailboxFolder.INBOX -> {

                        mailboxApi.getInbox(
                            page = page,

                            pageSize =
                            params.loadSize,

                            search =
                            filter.search
                                ?.takeIf {
                                    it.isNotBlank()
                                },

                            isRead =
                            filter.isRead,

                            hasAttachment =
                            filter.hasAttachment
                        )
                    }

                    MailboxFolder.SENT -> {

                        mailboxApi.getSent(
                            page = page,

                            pageSize =
                            params.loadSize,

                            search =
                            filter.search
                                ?.takeIf {
                                    it.isNotBlank()
                                },

                            /*
                             * Backend aynı query DTO'yu kullanıyor.
                             */
                            isRead =
                            filter.isRead,

                            hasAttachment =
                            filter.hasAttachment
                        )
                    }
                }

            val pagedResult =
                response.data

            if (
                !response.success ||
                pagedResult == null
            ) {

                return LoadResult.Error(
                    IllegalStateException(
                        response.message.ifBlank {
                            "Mesajlar getirilemedi."
                        }
                    )
                )
            }

            LoadResult.Page(
                data =
                pagedResult.items.map {
                    it.toDomain()
                },

                prevKey =
                if (pagedResult.hasPreviousPage) {
                    page - 1
                } else {
                    null
                },

                nextKey =
                if (pagedResult.hasNextPage) {
                    page + 1
                } else {
                    null
                }
            )

        } catch (throwable: Throwable) {

            LoadResult.Error(
                throwable
            )
        }
    }

    override fun getRefreshKey(
        state: PagingState<Int, MailboxMessage>
    ): Int? {

        val anchorPosition =
            state.anchorPosition
                ?: return null

        val anchorPage =
            state.closestPageToPosition(
                anchorPosition
            )

        return anchorPage
            ?.prevKey
            ?.plus(1)
            ?: anchorPage
                ?.nextKey
                ?.minus(1)
    }

    private companion object {

        const val FIRST_PAGE =
            1
    }
}