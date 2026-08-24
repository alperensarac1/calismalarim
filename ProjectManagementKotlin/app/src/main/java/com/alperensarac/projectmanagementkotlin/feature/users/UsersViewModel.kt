package com.alperensarac.projectmanagementkotlin.feature.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserFilter
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.GetUsersUseCase
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

/**
 * Admin kullanıcı yönetimi liste ekranının ViewModel'idir.
 *
 * Kullanıcı:
 *
 * - arama
 * - rol filtresi
 * - aktif/pasif filtresi
 *
 * değiştirdiğinde yeni PagingSource oluşturulur.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val getUsersUseCase:
    GetUsersUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            UsersUiState()
        )

    val uiState:
            StateFlow<UsersUiState> =
        mutableUiState.asStateFlow()

    val users:
            Flow<PagingData<User>> =

        mutableUiState
            .debounce(
                SEARCH_DEBOUNCE_MILLIS
            )
            .distinctUntilChanged()
            .flatMapLatest { state ->

                getUsersUseCase(
                    filter =
                    state.toUserFilter()
                )
            }
            .cachedIn(
                viewModelScope
            )

    // =========================================================================
    // SEARCH
    // =========================================================================

    fun onSearchChanged(
        search: String
    ) {

        mutableUiState.update { state ->

            state.copy(
                search =
                search
            )
        }
    }

    // =========================================================================
    // ROLE
    // =========================================================================

    fun onRoleChanged(
        role: String?
    ) {

        mutableUiState.update { state ->

            state.copy(
                selectedRole =
                role
            )
        }
    }

    // =========================================================================
    // ACTIVE FILTER
    // =========================================================================

    fun onActiveFilterChanged(
        filter: UserActiveFilter
    ) {

        mutableUiState.update { state ->

            state.copy(
                activeFilter =
                filter
            )
        }
    }

    // =========================================================================
    // MAPPER
    // =========================================================================

    private fun UsersUiState
            .toUserFilter():
            UserFilter {

        return UserFilter(
            search =
            search.trim(),

            role =
            selectedRole,

            isActive =
            when (
                activeFilter
            ) {

                UserActiveFilter.ALL ->
                    null

                UserActiveFilter.ACTIVE_ONLY ->
                    true

                UserActiveFilter.PASSIVE_ONLY ->
                    false
            }
        )
    }

    private companion object {

        const val SEARCH_DEBOUNCE_MILLIS =
            400L
    }
}