package com.alperensarac.projectmanagementkotlin.feature.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectFilter
import com.alperensarac.projectmanagementkotlin.domain.usecase.auth.GetCurrentUserUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.projects.GetProjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Projeler ekranının:
 *
 * - filtrelerini
 * - Paging 3 akışını
 * - rol bazlı proje oluşturma yetkisini
 *
 * yönetir.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val getProjectsUseCase:
    GetProjectsUseCase,

    private val getCurrentUserUseCase:
    GetCurrentUserUseCase
) : ViewModel() {

    // =========================================================================
    // FILTER STATE
    // =========================================================================

    private val mutableUiState =
        MutableStateFlow(
            ProjectsUiState()
        )

    val uiState:
            StateFlow<ProjectsUiState> =
        mutableUiState.asStateFlow()

    // =========================================================================
    // ADMIN PERMISSION
    // =========================================================================

    /**
     * Proje oluştur butonunun gösterilip gösterilmeyeceğini belirler.
     *
     * Şu aşamada kullanıcının istediği yönetim modeli gereği yalnızca
     * Admin'e gösteriyoruz.
     */
    private val mutableCanCreateProject =
        MutableStateFlow(
            false
        )

    val canCreateProject:
            StateFlow<Boolean> =
        mutableCanCreateProject
            .asStateFlow()

    // =========================================================================
    // PAGING
    // =========================================================================

    val projects:
            Flow<PagingData<Project>> =

        mutableUiState
            .debounce(
                SEARCH_DEBOUNCE_MILLIS
            )
            .distinctUntilChanged()
            .flatMapLatest { state ->

                getProjectsUseCase(
                    filter =
                    state.toProjectFilter()
                )
            }
            .cachedIn(
                viewModelScope
            )

    init {

        loadCreatePermission()
    }

    // =========================================================================
    // PERMISSION
    // =========================================================================

    private fun loadCreatePermission() {

        viewModelScope.launch {

            when (
                val result =
                    getCurrentUserUseCase()
            ) {

                is AppResult.Success -> {

                    val user =
                        result.data

                    mutableCanCreateProject.value =
                        user.isAdmin ||
                                user.isProjectManager
                }

                is AppResult.Error -> {

                    mutableCanCreateProject.value =
                        false
                }
            }
        }
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    fun onSearchChanged(
        search: String
    ) {

        mutableUiState.update { state ->

            state.copy(
                search = search
            )
        }
    }

    // =========================================================================
    // STATUS
    // =========================================================================

    fun onStatusChanged(
        status: String?
    ) {

        mutableUiState.update { state ->

            state.copy(
                selectedStatus = status
            )
        }
    }

    // =========================================================================
    // ARCHIVED
    // =========================================================================

    fun onArchivedFilterChanged(
        filter: ArchivedFilter
    ) {

        mutableUiState.update { state ->

            state.copy(
                archivedFilter = filter
            )
        }
    }

    // =========================================================================
    // FILTER MAPPER
    // =========================================================================

    private fun ProjectsUiState
            .toProjectFilter():
            ProjectFilter {

        return ProjectFilter(
            search =
            search.trim(),

            status =
            selectedStatus,

            isArchived =
            when (
                archivedFilter
            ) {

                ArchivedFilter.ACTIVE_ONLY ->
                    false

                ArchivedFilter.ARCHIVED_ONLY ->
                    true

                ArchivedFilter.ALL ->
                    null
            },

            ownerId =
            null
        )
    }

    private companion object {

        const val SEARCH_DEBOUNCE_MILLIS =
            400L

    }
}