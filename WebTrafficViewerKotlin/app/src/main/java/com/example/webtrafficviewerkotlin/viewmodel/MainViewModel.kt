package com.example.webtrafficviewerkotlin.viewmodel


import androidx.lifecycle.ViewModel
import com.example.webtrafficviewerkotlin.model.FilterUiState
import com.example.webtrafficviewerkotlin.model.NetworkLog
import com.example.webtrafficviewerkotlin.util.RequestFilterUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class MainViewModel : ViewModel() {

    // Tüm yakalanan ham loglar burada tutulur
    private val _allLogs = MutableStateFlow<List<NetworkLog>>(emptyList())
    val allLogs: StateFlow<List<NetworkLog>> = _allLogs.asStateFlow()

    // Checkbox durumları burada tutulur
    private val _filterState = MutableStateFlow(FilterUiState())
    val filterState: StateFlow<FilterUiState> = _filterState.asStateFlow()

    // Ekranda gösterilecek filtrelenmiş log listesi
    val visibleLogs: StateFlow<List<NetworkLog>> =
        combine(_allLogs, _filterState) { logs, filters ->
            applyFilters(logs, filters)
        }.let { flow ->
            MutableStateFlow(emptyList<NetworkLog>()).also { state ->
                // Bu yapı yerine stateIn de kullanılabilir ama şimdilik sade tuttuk
            }
        }

    // stateIn kullanmadan daha net bir yaklaşım:
    private val _visibleLogs = MutableStateFlow<List<NetworkLog>>(emptyList())
    val visibleLogs2: StateFlow<List<NetworkLog>> = _visibleLogs.asStateFlow()

    init {
        recalculateVisibleLogs()
    }

    fun addLog(log: NetworkLog) {
        val current = _allLogs.value.toMutableList()
        current.add(0, log)
        _allLogs.value = current
        recalculateVisibleLogs()
    }

    fun clearLogs() {
        _allLogs.value = emptyList()
        recalculateVisibleLogs()
    }

    fun getCurrentFilterState(): FilterUiState = _filterState.value

    fun updateEnableFilter(value: Boolean) {
        _filterState.value = _filterState.value.copy(enableFilter = value)
        recalculateVisibleLogs()
    }

    fun updateOnlyApi(value: Boolean) {
        _filterState.value = _filterState.value.copy(onlyApiRequests = value)
        recalculateVisibleLogs()
    }

    fun updateEnableJsHook(value: Boolean) {
        _filterState.value = _filterState.value.copy(enableJsHook = value)
    }

    fun updateShowOnlyGet(value: Boolean) {
        val newState = if (value) {
            _filterState.value.copy(showOnlyGet = true, showOnlyPost = false)
        } else {
            _filterState.value.copy(showOnlyGet = false)
        }
        _filterState.value = newState
        recalculateVisibleLogs()
    }

    fun updateShowOnlyPost(value: Boolean) {
        val newState = if (value) {
            _filterState.value.copy(showOnlyPost = true, showOnlyGet = false)
        } else {
            _filterState.value.copy(showOnlyPost = false)
        }
        _filterState.value = newState
        recalculateVisibleLogs()
    }

    fun getAllLogsSnapshot(): List<NetworkLog> = _allLogs.value

    private fun recalculateVisibleLogs() {
        _visibleLogs.value = applyFilters(_allLogs.value, _filterState.value)
    }

    private fun applyFilters(
        logs: List<NetworkLog>,
        filters: FilterUiState
    ): List<NetworkLog> {
        if (!filters.enableFilter) return logs

        return logs.filter { log ->
            val methodOk = when {
                filters.showOnlyGet -> log.method.equals("GET", ignoreCase = true)
                filters.showOnlyPost -> log.method.equals("POST", ignoreCase = true)
                else -> log.method.equals("GET", ignoreCase = true) ||
                        log.method.equals("POST", ignoreCase = true)
            }

            val ignoredOk = !RequestFilterUtils.shouldIgnore(log.url)

            val apiOk = if (filters.onlyApiRequests) {
                RequestFilterUtils.looksLikeApi(log.url) ||
                        log.resourceType.equals("api", ignoreCase = true) ||
                        log.source == "JS_HOOK"
            } else {
                true
            }

            methodOk && ignoredOk && apiOk
        }
    }
}