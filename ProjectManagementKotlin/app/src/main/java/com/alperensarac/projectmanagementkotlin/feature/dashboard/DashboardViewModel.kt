package com.alperensarac.projectmanagementkotlin.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alperensarac.projectmanagementkotlin.core.common.result.AppResult
import com.alperensarac.projectmanagementkotlin.core.network.model.toUserMessage
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardRecentTask
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardSummary
import com.alperensarac.projectmanagementkotlin.domain.usecase.dashboard.GetDashboardSummaryUseCase
import com.alperensarac.projectmanagementkotlin.domain.usecase.dashboard.GetRecentTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dashboard ekranının özet ve son görev verilerini yönetir.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getRecentTasksUseCase: GetRecentTasksUseCase
) : ViewModel() {

    private val mutableUiState =
        MutableStateFlow(
            DashboardUiState(
                isLoading = false
            )
        )

    val uiState: StateFlow<DashboardUiState> =
        mutableUiState.asStateFlow()

    init {
        loadDashboard()
    }

    /**
     * Summary ve son görev isteklerini paralel çalıştırır.
     */
    fun loadDashboard(
        isRefresh: Boolean = false
    ) {
        val currentState = mutableUiState.value

        if (currentState.isLoading || currentState.isRefreshing) {
            return
        }

        viewModelScope.launch {
            mutableUiState.value = currentState.copy(
                isLoading = !isRefresh && currentState.summary == null,
                isRefreshing = isRefresh,
                errorMessage = null
            )

            val summaryDeferred = async {
                getDashboardSummaryUseCase()
            }

            val recentTasksDeferred = async {
                getRecentTasksUseCase()
            }

            reduceResults(
                summaryResult = summaryDeferred.await(),
                recentTasksResult = recentTasksDeferred.await()
            )
        }
    }

    fun refresh() {
        loadDashboard(isRefresh = true)
    }

    private fun reduceResults(
        summaryResult: AppResult<DashboardSummary>,
        recentTasksResult: AppResult<List<DashboardRecentTask>>
    ) {
        val previousState = mutableUiState.value

        val newSummary = when (summaryResult) {
            is AppResult.Success -> summaryResult.data
            is AppResult.Error -> previousState.summary
        }

        val newRecentTasks = when (recentTasksResult) {
            is AppResult.Success -> recentTasksResult.data
            is AppResult.Error -> previousState.recentTasks
        }

        val errorMessage = when {
            summaryResult is AppResult.Error -> {
                summaryResult.error.toUserMessage()
            }

            recentTasksResult is AppResult.Error -> {
                recentTasksResult.error.toUserMessage()
            }

            else -> null
        }

        mutableUiState.value = previousState.copy(
            isLoading = false,
            isRefreshing = false,
            summary = newSummary,
            recentTasks = newRecentTasks,
            errorMessage = errorMessage
        )
    }
}