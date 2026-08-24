package com.alperensarac.projectmanagementkotlin.feature.projects.create.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.alperensarac.projectmanagementkotlin.domain.model.users.User
import com.alperensarac.projectmanagementkotlin.domain.usecase.users.SearchProjectOwnersUseCase
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
 * Proje sahibi seçim ekranını yönetir.
 *
 * Bu ekran normal kullanıcı seçme ekranından farklıdır.
 *
 * Burada yalnızca:
 *
 * - aktif
 * - Admin
 * - ProjectManager
 *
 * kullanıcıları gösteriyoruz.
 *
 * Filtreleme detayını ViewModel'e koymuyoruz.
 *
 * Bunun yerine:
 *
 * SearchProjectOwnersUseCase
 *
 * üzerinden domain katmanında gerçekleştiriyoruz.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ProjectOwnerSelectionViewModel @Inject constructor(

    private val searchProjectOwnersUseCase:
    SearchProjectOwnersUseCase

) : ViewModel() {

    // =========================================================================
    // STATE
    // =========================================================================

    private val mutableUiState =
        MutableStateFlow(
            ProjectOwnerSelectionUiState()
        )

    val uiState:
            StateFlow<ProjectOwnerSelectionUiState> =
        mutableUiState.asStateFlow()

    // =========================================================================
    // USERS
    // =========================================================================

    /**
     * Arama değiştikçe owner olmaya uygun kullanıcıları getirir.
     *
     * 400 ms debounce:
     *
     * A
     * Ah
     * Ahm
     * Ahme
     * Ahmet
     *
     * yazılırken her tuş için ayrı HTTP request atılmasını azaltır.
     */
    val users:
            Flow<PagingData<User>> =

        mutableUiState
            .debounce(
                SEARCH_DEBOUNCE_MILLIS
            )
            .distinctUntilChanged { old, new ->

                old.search ==
                        new.search
            }
            .flatMapLatest { state ->

                searchProjectOwnersUseCase(
                    search =
                    state.search
                )
            }
            .cachedIn(
                viewModelScope
            )

    // =========================================================================
    // SEARCH
    // =========================================================================

    fun onSearchChanged(
        value: String
    ) {

        mutableUiState.update { state ->

            state.copy(
                search =
                value
            )
        }
    }

    // =========================================================================
    // SELECT
    // =========================================================================

    fun selectUser(
        user: User
    ) {

        /*
         * UseCase zaten yalnızca uygun kullanıcıları gönderiyor.
         *
         * Yine de ViewModel içerisinde ikinci bir güvenlik kontrolü
         * uyguluyoruz.
         */
        if (
            !user.isActive ||
            !isAllowedOwnerRole(
                user.role
            )
        ) {
            return
        }

        mutableUiState.update { state ->

            state.copy(
                selectedUser =
                user
            )
        }
    }

    // =========================================================================
    // ROLE CHECK
    // =========================================================================

    private fun isAllowedOwnerRole(
        role: String
    ): Boolean {

        return role.equals(
            ADMIN_ROLE,
            ignoreCase = true
        ) ||
                role.equals(
                    PROJECT_MANAGER_ROLE,
                    ignoreCase = true
                )
    }

    private companion object {

        const val SEARCH_DEBOUNCE_MILLIS =
            400L

        const val ADMIN_ROLE =
            "Admin"

        const val PROJECT_MANAGER_ROLE =
            "ProjectManager"
    }
}