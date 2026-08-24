package com.alperensarac.projectmanagementkotlin.feature.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.FragmentTasksBinding
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskPriority
import com.alperensarac.projectmanagementkotlin.domain.model.tasks.TaskStatus
import com.alperensarac.projectmanagementkotlin.feature.tasks.adapter.TaskPagingAdapter
import com.alperensarac.projectmanagementkotlin.feature.tasks.adapter.TasksLoadStateAdapter
import com.alperensarac.projectmanagementkotlin.feature.tasks.detail.TaskDetailFragment
import com.alperensarac.projectmanagementkotlin.feature.tasks.navigation.TaskNavigationResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Gerçek backend'e bağlı görev liste ekranıdır.
 *
 * Desteklenen özellikler:
 *
 * - Paging 3
 * - Arama
 * - Status filtresi
 * - Priority filtresi
 * - Overdue filtresi
 * - Pull-to-refresh
 * - Task Detail navigation
 * - Detay ekranından dönüşte otomatik liste yenileme
 */
@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding:
            FragmentTasksBinding? =
        null

    private val binding:
            FragmentTasksBinding
        get() =
            checkNotNull(_binding) {
                "FragmentTasksBinding view lifecycle dışında kullanılamaz."
            }

    private val viewModel:
            TasksViewModel
            by viewModels()

    @Inject
    lateinit var dateTimeFormatter:
            DateTimeFormatter

    private lateinit var taskAdapter:
            TaskPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentTasksBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        configureRecyclerView()

        configureSearch()

        configureStatusFilter()

        configurePriorityFilter()

        configureOverdueFilter()

        configureRefresh()

        /*
         * TaskDetailFragment'ten gelen değişiklikleri dinler.
         */
        observeTaskNavigationResults()

        observeTasks()

        observeLoadState()
    }

    // =========================================================================
    // RECYCLER VIEW
    // =========================================================================

    private fun configureRecyclerView() {

        taskAdapter =
            TaskPagingAdapter(

                dateTimeFormatter =
                dateTimeFormatter,

                onTaskClicked = { task ->

                    openTaskDetail(
                        taskId = task.id
                    )
                }
            )

        binding.recyclerViewTasks.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                taskAdapter.withLoadStateFooter(
                    footer =
                    TasksLoadStateAdapter(
                        retry =
                        taskAdapter::retry
                    )
                )
        }
    }

    // =========================================================================
    // NAVIGATION RESULT
    // =========================================================================

    /**
     * TaskDetailFragment içerisinde bir görev değiştirildiğinde:
     *
     * previousBackStackEntry.savedStateHandle
     *
     * üzerinden bu Fragment'e sonuç gönderilir.
     *
     * Sonrasında Paging 3 listesini baştan yükleriz.
     */
    private fun observeTaskNavigationResults() {

        val savedStateHandle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?: return

        // ---------------------------------------------------------------------
        // TASK CHANGED
        // ---------------------------------------------------------------------

        savedStateHandle
            .getLiveData<Boolean>(
                TaskNavigationResult.TASK_CHANGED
            )
            .observe(
                viewLifecycleOwner
            ) { changed ->

                if (changed != true) {
                    return@observe
                }

                /*
                 * Backend'deki son listeyi tekrar alır.
                 */
                taskAdapter.refresh()

                /*
                 * Event'in configuration change sonrasında tekrar
                 * çalışmaması için consume ediyoruz.
                 */
                savedStateHandle.remove<Boolean>(
                    TaskNavigationResult.TASK_CHANGED
                )

                savedStateHandle.remove<Int>(
                    TaskNavigationResult.TASK_ID
                )
            }

        // ---------------------------------------------------------------------
        // TASK DELETED
        // ---------------------------------------------------------------------

        savedStateHandle
            .getLiveData<Boolean>(
                TaskNavigationResult.TASK_DELETED
            )
            .observe(
                viewLifecycleOwner
            ) { deleted ->

                if (deleted != true) {
                    return@observe
                }

                /*
                 * Silinen görev PagingData içerisinde hâlâ bulunabileceği
                 * için server listesini yeniden yüklüyoruz.
                 */
                taskAdapter.refresh()

                savedStateHandle.remove<Boolean>(
                    TaskNavigationResult.TASK_DELETED
                )

                savedStateHandle.remove<Int>(
                    TaskNavigationResult.TASK_ID
                )
            }
    }

    /**
     * Görev detay ekranını açar.
     */
    private fun openTaskDetail(
        taskId: Int
    ) {

        /*
         * Kullanıcı çok hızlı çift tıklarsa iki navigation yapılmasını
         * engelliyoruz.
         */
        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.tasksFragment
        ) {
            return
        }

        findNavController().navigate(
            R.id.action_tasksFragment_to_taskDetailFragment,

            bundleOf(
                TaskDetailFragment.ARG_TASK_ID
                        to taskId
            )
        )
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    private fun configureSearch() {

        binding.editTextTaskSearch
            .doAfterTextChanged { editable ->

                viewModel.onSearchChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // STATUS FILTER
    // =========================================================================

    private fun configureStatusFilter() {

        val labels =
            listOf(
                getString(
                    R.string.tasks_filter_all_statuses
                ),
                getString(
                    R.string.tasks_status_todo
                ),
                getString(
                    R.string.tasks_status_in_progress
                ),
                getString(
                    R.string.tasks_status_in_review
                ),
                getString(
                    R.string.tasks_status_done
                )
            )

        binding.autoCompleteTaskStatus
            .setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    labels
                )
            )

        binding.autoCompleteTaskStatus
            .setText(
                labels.first(),
                false
            )

        binding.autoCompleteTaskStatus
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val status =
                    when (position) {

                        1 ->
                            TaskStatus.TODO

                        2 ->
                            TaskStatus.IN_PROGRESS

                        3 ->
                            TaskStatus.IN_REVIEW

                        4 ->
                            TaskStatus.DONE

                        else ->
                            null
                    }

                viewModel.onStatusChanged(
                    status
                )
            }
    }

    // =========================================================================
    // PRIORITY FILTER
    // =========================================================================

    private fun configurePriorityFilter() {

        val labels =
            listOf(
                getString(
                    R.string.tasks_filter_all_priorities
                ),
                getString(
                    R.string.tasks_priority_low
                ),
                getString(
                    R.string.tasks_priority_medium
                ),
                getString(
                    R.string.tasks_priority_high
                ),
                getString(
                    R.string.tasks_priority_critical
                )
            )

        binding.autoCompleteTaskPriority
            .setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    labels
                )
            )

        binding.autoCompleteTaskPriority
            .setText(
                labels.first(),
                false
            )

        binding.autoCompleteTaskPriority
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val priority =
                    when (position) {

                        1 ->
                            TaskPriority.LOW

                        2 ->
                            TaskPriority.MEDIUM

                        3 ->
                            TaskPriority.HIGH

                        4 ->
                            TaskPriority.CRITICAL

                        else ->
                            null
                    }

                viewModel.onPriorityChanged(
                    priority
                )
            }
    }

    // =========================================================================
    // OVERDUE FILTER
    // =========================================================================

    private fun configureOverdueFilter() {

        val labels =
            listOf(
                getString(
                    R.string.tasks_filter_overdue_all
                ),
                getString(
                    R.string.tasks_filter_overdue_only
                ),
                getString(
                    R.string.tasks_filter_not_overdue
                )
            )

        binding.autoCompleteTaskOverdue
            .setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    labels
                )
            )

        binding.autoCompleteTaskOverdue
            .setText(
                labels.first(),
                false
            )

        binding.autoCompleteTaskOverdue
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val filter =
                    when (position) {

                        1 ->
                            OverdueFilter.OVERDUE_ONLY

                        2 ->
                            OverdueFilter.NOT_OVERDUE

                        else ->
                            OverdueFilter.ALL
                    }

                viewModel.onOverdueFilterChanged(
                    filter
                )
            }
    }

    // =========================================================================
    // REFRESH
    // =========================================================================

    private fun configureRefresh() {

        binding.swipeRefreshTasks
            .setOnRefreshListener {

                taskAdapter.refresh()
            }

        binding.buttonRetryTasks
            .setOnClickListener {

                taskAdapter.retry()
            }
    }

    // =========================================================================
    // PAGING DATA
    // =========================================================================

    private fun observeTasks() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.tasks.collectLatest { pagingData ->

                    taskAdapter.submitData(
                        pagingData
                    )
                }
            }
        }
    }

    // =========================================================================
    // PAGING LOAD STATE
    // =========================================================================

    private fun observeLoadState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                taskAdapter
                    .loadStateFlow
                    .collectLatest { loadStates ->

                        val refresh =
                            loadStates.refresh

                        val isLoading =
                            refresh is
                                    LoadState.Loading

                        val isError =
                            refresh is
                                    LoadState.Error

                        // -----------------------------------------------------
                        // INITIAL LOADING
                        // -----------------------------------------------------

                        binding
                            .progressIndicatorTasks
                            .isVisible =
                            isLoading &&
                                    taskAdapter.itemCount == 0 &&
                                    !binding
                                        .swipeRefreshTasks
                                        .isRefreshing

                        // -----------------------------------------------------
                        // SWIPE REFRESH
                        // -----------------------------------------------------

                        if (!isLoading) {

                            binding
                                .swipeRefreshTasks
                                .isRefreshing =
                                false
                        }

                        // -----------------------------------------------------
                        // EMPTY
                        // -----------------------------------------------------

                        binding
                            .layoutTasksEmpty
                            .isVisible =
                            refresh is
                                    LoadState.NotLoading &&
                                    taskAdapter.itemCount == 0

                        // -----------------------------------------------------
                        // ERROR
                        // -----------------------------------------------------

                        binding
                            .layoutTasksError
                            .isVisible =
                            isError &&
                                    taskAdapter.itemCount == 0

                        // -----------------------------------------------------
                        // LIST
                        // -----------------------------------------------------

                        binding
                            .recyclerViewTasks
                            .isVisible =
                            taskAdapter.itemCount > 0

                        if (
                            refresh is
                                    LoadState.Error
                        ) {

                            binding
                                .textViewTasksError
                                .text =
                                refresh.error.message
                                    ?: getString(
                                        R.string.tasks_load_error
                                    )
                        }
                    }
            }
        }
    }

    override fun onDestroyView() {

        if (::taskAdapter.isInitialized) {

            binding.recyclerViewTasks.adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }
}