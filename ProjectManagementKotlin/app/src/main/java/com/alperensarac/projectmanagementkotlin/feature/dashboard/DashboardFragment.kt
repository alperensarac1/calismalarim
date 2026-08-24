package com.alperensarac.projectmanagementkotlin.feature.dashboard

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.core.common.formatter.DateTimeFormatter
import com.alperensarac.projectmanagementkotlin.databinding.FragmentDashboardBinding
import com.alperensarac.projectmanagementkotlin.domain.model.dashboard.DashboardSummary
import com.alperensarac.projectmanagementkotlin.feature.dashboard.adapter.RecentTaskAdapter
import com.alperensarac.projectmanagementkotlin.feature.tasks.detail.TaskDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Dashboard ekranıdır.
 *
 * Bu ekran artık yalnızca bilgi göstermez.
 *
 * Aynı zamanda uygulamanın hızlı navigasyon merkezi olarak çalışır.
 *
 * Desteklenen navigation:
 *
 * Total Projects
 *      ↓
 * Projects
 *
 * Active Projects
 *      ↓
 * Projects
 *
 * Total Tasks
 *      ↓
 * Tasks
 *
 * Overdue Tasks
 *      ↓
 * Tasks
 *
 * My Tasks
 *      ↓
 * Tasks
 *
 * Completed Tasks
 *      ↓
 * Tasks
 *
 * Recent Task
 *      ↓
 * Task Detail
 */
@AndroidEntryPoint
class DashboardFragment : Fragment() {

    // =========================================================================
    // VIEW BINDING
    // =========================================================================

    private var _binding:
            FragmentDashboardBinding? =
        null

    private val binding:
            FragmentDashboardBinding
        get() =
            checkNotNull(_binding) {
                "FragmentDashboardBinding view lifecycle dışında kullanılamaz."
            }

    // =========================================================================
    // VIEW MODEL
    // =========================================================================

    private val viewModel:
            DashboardViewModel
            by viewModels()

    // =========================================================================
    // DEPENDENCIES
    // =========================================================================

    @Inject
    lateinit var dateTimeFormatter:
            DateTimeFormatter

    // =========================================================================
    // ADAPTER
    // =========================================================================

    private lateinit var recentTaskAdapter:
            RecentTaskAdapter

    // =========================================================================
    // TASK DETAIL REFRESH
    // =========================================================================

    /**
     * Dashboard'dan Task Detail ekranına girilmişse geri dönüldüğünde
     * dashboard verilerini tekrar yüklemek için kullanılır.
     */
    private var shouldRefreshAfterTaskDetail =
        false

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentDashboardBinding.inflate(
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

        configureListeners()

        configureSummaryNavigation()

        observeUiState()
    }

    override fun onResume() {

        super.onResume()

        if (
            shouldRefreshAfterTaskDetail
        ) {

            shouldRefreshAfterTaskDetail =
                false

            viewModel.refresh()
        }
    }

    // =========================================================================
    // RECYCLER VIEW
    // =========================================================================

    private fun configureRecyclerView() {

        recentTaskAdapter =
            RecentTaskAdapter(

                dateTimeFormatter =
                dateTimeFormatter,

                onTaskClicked = { task ->

                    openTaskDetail(
                        taskId =
                        task.id
                    )
                }
            )

        binding.recyclerViewRecentTasks.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                recentTaskAdapter

            setHasFixedSize(
                false
            )
        }
    }

    // =========================================================================
    // LISTENERS
    // =========================================================================

    private fun configureListeners() {

        binding.swipeRefreshDashboard
            .setOnRefreshListener {

                viewModel.refresh()
            }

        binding.buttonRetryDashboard
            .setOnClickListener {

                viewModel.loadDashboard()
            }
    }

    // =========================================================================
    // SUMMARY NAVIGATION
    // =========================================================================

    /**
     * Dashboard özet kartlarını tıklanabilir hale getirir.
     *
     * Bu aşamada filtre parametresi göndermiyoruz.
     *
     * Önce mevcut Projects / Tasks ekranlarını açıyoruz.
     *
     * Sonraki aşamada:
     *
     * Overdue Tasks -> IsOverdue=true
     * My Tasks      -> AssignedToUserId=currentUser
     * Completed     -> Status=Done
     *
     * şeklinde gerçek filtre navigation ekleyebiliriz.
     */
    private fun configureSummaryNavigation() {

        // -------------------------------------------------------------------------
        // TOTAL PROJECTS
        // -------------------------------------------------------------------------

        binding.includeTotalProjects.root
            .setOnClickListener {

                navigateToProjects()
            }

        // -------------------------------------------------------------------------
        // ACTIVE PROJECTS
        // -------------------------------------------------------------------------

        binding.includeActiveProjects.root
            .setOnClickListener {

                navigateToProjects()
            }

        // -------------------------------------------------------------------------
        // TOTAL TASKS
        // -------------------------------------------------------------------------

        binding.includeTotalTasks.root
            .setOnClickListener {

                navigateToTasks()
            }

        // -------------------------------------------------------------------------
        // OVERDUE TASKS
        // -------------------------------------------------------------------------

        binding.includeOverdueTasks.root
            .setOnClickListener {

                navigateToTasks()
            }

        // -------------------------------------------------------------------------
        // MY TASKS
        // -------------------------------------------------------------------------

        binding.includeMyTasks.root
            .setOnClickListener {

                navigateToTasks()
            }

        // -------------------------------------------------------------------------
        // COMPLETED TASKS
        // -------------------------------------------------------------------------

        binding.includeCompletedTasks.root
            .setOnClickListener {

                navigateToTasks()
            }
    }

    // =========================================================================
    // PROJECTS NAVIGATION
    // =========================================================================

    private fun navigateToProjects() {

        /*
         * Çok hızlı çift tıklamayı engelliyoruz.
         */
        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.dashboardFragment
        ) {
            return
        }

        /*
         * Projects ana destination.
         *
         * Eğer projende ID farklıysa navigation graph'taki gerçek ID'yi
         * kullanmamız gerekir.
         */
        findNavController()
            .navigate(
                R.id.projectsFragment
            )
    }

    // =========================================================================
    // TASKS NAVIGATION
    // =========================================================================

    private fun navigateToTasks() {

        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.dashboardFragment
        ) {
            return
        }

        /*
         * Tasks ana destination.
         */
        findNavController()
            .navigate(
                R.id.tasksFragment
            )
    }

    // =========================================================================
    // TASK DETAIL
    // =========================================================================

    private fun openTaskDetail(
        taskId: Int
    ) {

        if (
            taskId <= 0
        ) {
            return
        }

        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.dashboardFragment
        ) {
            return
        }

        shouldRefreshAfterTaskDetail =
            true

        try {

            findNavController()
                .navigate(
                    R.id.taskDetailFragment,

                    bundleOf(

                        TaskDetailFragment
                            .ARG_TASK_ID
                                to taskId
                    )
                )

        } catch (
            throwable: Throwable
        ) {

            shouldRefreshAfterTaskDetail =
                false

            throw throwable
        }
    }

    // =========================================================================
    // UI STATE
    // =========================================================================

    @RequiresApi(
        Build.VERSION_CODES.O
    )
    private fun observeUiState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.uiState
                            .collect(
                                ::renderUiState
                            )
                    }
            }
    }

    // =========================================================================
    // RENDER
    // =========================================================================

    @RequiresApi(
        Build.VERSION_CODES.O
    )
    private fun renderUiState(
        state: DashboardUiState
    ) {

        binding.progressIndicatorDashboard
            .isVisible =
            state.isLoading &&
                    !state.hasContent

        binding.swipeRefreshDashboard
            .isRefreshing =
            state.isRefreshing

        binding.layoutDashboardContent
            .isVisible =
            state.hasContent

        binding.layoutDashboardError
            .isVisible =
            !state.errorMessage
                .isNullOrBlank() &&
                    !state.hasContent

        binding.textViewDashboardError.text =
            state.errorMessage
                .orEmpty()

        state.summary
            ?.let(
                ::renderSummary
            )

        recentTaskAdapter.submitList(
            state.recentTasks
        )

        binding.textViewRecentTasksEmpty
            .isVisible =
            state.hasContent &&
                    state.isRecentTasksEmpty
    }

    // =========================================================================
    // SUMMARY
    // =========================================================================

    @RequiresApi(
        Build.VERSION_CODES.O
    )
    private fun renderSummary(
        summary: DashboardSummary
    ) {

        // =====================================================================
        // PROJECT COUNTS
        // =====================================================================

        /**
         * <include> ViewBinding oluşturduğu için önce include binding'e,
         * ardından onun içindeki TextView'a erişiyoruz.
         */
        binding.includeTotalProjects
            .textViewTotalProjectsValue
            .text =
            summary.totalProjectCount
                .toString()

        binding.includeActiveProjects
            .textViewActiveProjectsValue
            .text =
            summary.activeProjectCount
                .toString()

        // =====================================================================
        // TASK COUNTS
        // =====================================================================

        binding.includeTotalTasks
            .textViewTotalTasksValue
            .text =
            summary.totalTaskCount
                .toString()

        binding.includeOverdueTasks
            .textViewOverdueTasksValue
            .text =
            summary.overdueTaskCount
                .toString()

        binding.includeMyTasks
            .textViewMyTasksValue
            .text =
            summary.myAssignedTaskCount
                .toString()

        binding.includeCompletedTasks
            .textViewCompletedTasksValue
            .text =
            summary.doneTaskCount
                .toString()

        // =====================================================================
        // PERCENTAGES
        // =====================================================================

        binding.textViewCompletionPercentage
            .text =
            getString(
                R.string.dashboard_percentage_format,
                summary.taskCompletionPercentage
            )

        binding.textViewTimeUsagePercentage
            .text =
            getString(
                R.string.dashboard_percentage_format,
                summary.timeUsagePercentage
            )

        // =====================================================================
        // GENERATED AT
        // =====================================================================

        binding.textViewGeneratedAt
            .text =
            getString(
                R.string.dashboard_generated_at_format,
                dateTimeFormatter
                    .formatUtcDateTime(
                        summary.generatedAtUtc
                    )
            )
    }

    // =========================================================================
    // DESTROY VIEW
    // =========================================================================

    override fun onDestroyView() {

        if (
            ::recentTaskAdapter.isInitialized
        ) {

            binding.recyclerViewRecentTasks
                .adapter =
                null
        }

        _binding =
            null

        super.onDestroyView()
    }
}