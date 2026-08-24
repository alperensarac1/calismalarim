package com.alperensarac.projectmanagementkotlin.feature.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.Task
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskFilter
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.domain.usecase.tasks.GetTasksUseCase
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
 * Tasks ekranı ViewModel'i.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            TasksUiState()
        )

    val uiState: StateFlow<TasksUiState> =
        mutableUiState.asStateFlow()

    /**
     * Her filtre değişiminde yeni Pager oluşturulur.
     *
     * Aramaya debounce uygulanır.
     */
    val tasks: Flow<PagingData<Task>> =
        mutableUiState
            .debounce(
                SEARCH_DEBOUNCE_MILLIS
            )
            .distinctUntilChanged()
            .flatMapLatest { state ->

                getTasksUseCase(
                    filter =
                    state.toFilter()
                )
            }
            .cachedIn(
                viewModelScope
            )

    fun onSearchChanged(
        value: String
    ) {

        mutableUiState.update { state ->

            state.copy(
                search = value
            )
        }
    }

    fun onStatusChanged(
        status: TaskStatus?
    ) {

        mutableUiState.update { state ->

            state.copy(
                selectedStatus = status
            )
        }
    }

    fun onPriorityChanged(
        priority: TaskPriority?
    ) {

        mutableUiState.update { state ->

            state.copy(
                selectedPriority = priority
            )
        }
    }

    fun onOverdueFilterChanged(
        filter: OverdueFilter
    ) {

        mutableUiState.update { state ->

            state.copy(
                overdueFilter = filter
            )
        }
    }

    private fun TasksUiState.toFilter(): TaskFilter {

        return TaskFilter(
            search =
            search.trim(),

            projectId =
            null,

            assignedToUserId =
            null,

            status =
            selectedStatus,

            priority =
            selectedPriority,

            isOverdue =
            when (overdueFilter) {

                OverdueFilter.ALL ->
                    null

                OverdueFilter.OVERDUE_ONLY ->
                    true

                OverdueFilter.NOT_OVERDUE ->
                    false
            }
        )
    }

    private companion object {

        const val SEARCH_DEBOUNCE_MILLIS =
            400L
    }
}