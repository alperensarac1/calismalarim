package com.alperensarac.projectmanagementkotlin.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.alperensarac.projectmanagementkotlin.data.mapper.projects.toDomain
import com.alperensarac.projectmanagementkotlin.data.remote.api.ProjectsApi
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectFilter

/**
 * Projects endpointini Paging 3 sistemine bağlayan PagingSource sınıfıdır.
 *
 * Backend sayfalaması 1'den başladığı için INITIAL_PAGE_NUMBER = 1
 * kullanıyoruz.
 */
class ProjectsPagingSource(
    private val projectsApi: ProjectsApi,
    private val filter: ProjectFilter
) : PagingSource<Int, Project>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, Project> {
        val currentPage =
            params.key ?: INITIAL_PAGE_NUMBER

        return try {
            val response = projectsApi.getProjects(
                page = currentPage,
                pageSize = params.loadSize,

                /*
                 * Boş arama string'i backend'e göndermiyoruz.
                 */
                search = filter.search
                    .trim()
                    .takeIf { it.isNotEmpty() },

                status = filter.status,
                isArchived = filter.isArchived,
                ownerId = filter.ownerId
            )

            val pagedResult = response.data

            /*
             * HTTP başarılı olsa bile backend success=false döndürebilir.
             */
            if (!response.success || pagedResult == null) {
                return LoadResult.Error(
                    IllegalStateException(
                        response.message.ifBlank {
                            "Projeler alınamadı."
                        }
                    )
                )
            }

            val projects = pagedResult.items.map { dto ->
                dto.toDomain()
            }

            /*
             * Backend hasPreviousPage ve hasNextPage değerlerini zaten
             * hesapladığı için onları kullanıyoruz.
             */
            val previousKey = if (
                pagedResult.hasPreviousPage
            ) {
                currentPage - 1
            } else {
                null
            }

            val nextKey = if (
                pagedResult.hasNextPage
            ) {
                currentPage + 1
            } else {
                null
            }

            LoadResult.Page(
                data = projects,
                prevKey = previousKey,
                nextKey = nextKey
            )
        } catch (throwable: Throwable) {
            /*
             * Paging 3 LoadState.Error üzerinden exception UI'a ulaşır.
             */
            LoadResult.Error(throwable)
        }
    }

    /**
     * Liste invalidate olduğunda hangi sayfadan yeniden başlanacağını
     * hesaplar.
     */
    override fun getRefreshKey(
        state: PagingState<Int, Project>
    ): Int? {
        val anchorPosition =
            state.anchorPosition ?: return null

        val anchorPage =
            state.closestPageToPosition(anchorPosition)

        return anchorPage?.prevKey?.plus(1)
            ?: anchorPage?.nextKey?.minus(1)
    }

    private companion object {
        const val INITIAL_PAGE_NUMBER = 1
    }
}