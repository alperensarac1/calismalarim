package com.alperensarac.projectmanagementkotlin.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.alperensarac.projectmanagementkotlin.data.mapper.tasks.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.TasksApi
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskFilter

/**
 * GET /api/Tasks endpointini Paging 3 sistemine bağlar.
 */
class TasksPagingSource(
    private val tasksApi: TasksApi,
    private val filter: TaskFilter
) : PagingSource<Int, Task>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Task> {

        val currentPage =
            params.key ?: INITIAL_PAGE

        return try {

            val response =
                tasksApi.getTasks(
                    page = currentPage,

                    /*
                     * Paging config içerisindeki pageSize ile aynı değer
                     * kullanılacaktır.
                     */
                    pageSize = params.loadSize,

                    search =
                    filter.search
                        .trim()
                        .takeIf {
                            it.isNotBlank()
                        },

                    projectId =
                    filter.projectId,

                    assignedToUserId =
                    filter.assignedToUserId,

                    status =
                    filter.status?.apiValue,

                    priority =
                    filter.priority?.apiValue,

                    isOverdue =
                    filter.isOverdue
                )

            val pagedResult =
                response.data

            if (
                !response.success ||
                pagedResult == null
            ) {

                return LoadResult.Error(
                    IllegalStateException(
                        response.message.ifBlank {
                            "Görevler alınamadı."
                        }
                    )
                )
            }

            val tasks =
                pagedResult.items.map { dto ->
                    dto.toDomain()
                }

            LoadResult.Page(
                data = tasks,

                prevKey =
                if (
                    pagedResult.hasPreviousPage
                ) {
                    currentPage - 1
                } else {
                    null
                },

                nextKey =
                if (
                    pagedResult.hasNextPage
                ) {
                    currentPage + 1
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
        state: PagingState<Int, Task>
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

        const val INITIAL_PAGE =
            1
    }
}