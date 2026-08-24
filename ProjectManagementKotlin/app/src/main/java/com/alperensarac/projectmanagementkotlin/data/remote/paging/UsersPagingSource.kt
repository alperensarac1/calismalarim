package com.alperensarac.projectmanagementkotlin.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.alperensarac.projectmanagementkotlin.data.mapper.users.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.UsersApi
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserFilter

/**
 * GET /api/Users endpointini Paging 3 ile kullanır.
 *
 * Backend sayfalama:
 *
 * Page
 * PageSize
 *
 * Filtreler:
 *
 * Search
 * Role
 * IsActive
 */
class UsersPagingSource(
    private val usersApi: UsersApi,
    private val filter: UserFilter
) : PagingSource<Int, User>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, User> {

        val page =
            params.key ?: FIRST_PAGE

        return try {

            val response =
                usersApi.getUsers(
                    page = page,
                    pageSize = params.loadSize,

                    search =
                    filter.search
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        },

                    role =
                    filter.role,

                    isActive =
                    filter.isActive
                )

            val data =
                response.data

            if (
                !response.success ||
                data == null
            ) {

                return LoadResult.Error(
                    IllegalStateException(
                        response.message
                            .ifBlank {
                                "Kullanıcılar alınamadı."
                            }
                    )
                )
            }

            LoadResult.Page(
                data =
                data.items.map { dto ->
                    dto.toDomain()
                },

                prevKey =
                if (
                    data.hasPreviousPage
                ) {
                    page - 1
                } else {
                    null
                },

                nextKey =
                if (
                    data.hasNextPage
                ) {
                    page + 1
                } else {
                    null
                }
            )

        } catch (
            throwable: Throwable
        ) {

            LoadResult.Error(
                throwable
            )
        }
    }

    override fun getRefreshKey(
        state: PagingState<Int, User>
    ): Int? {

        val anchorPosition =
            state.anchorPosition
                ?: return null

        val anchorPage =
            state.closestPageToPosition(
                anchorPosition
            )

        return anchorPage?.prevKey
            ?.plus(1)
            ?: anchorPage?.nextKey
                ?.minus(1)
    }

    private companion object {

        const val FIRST_PAGE =
            1
    }
}