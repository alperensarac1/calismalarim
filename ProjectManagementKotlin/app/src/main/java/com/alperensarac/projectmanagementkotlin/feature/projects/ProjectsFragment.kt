package com.alperensarac.projectmanagementkotlin.feature.projects

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
import com.alperensarac.projectmanagementkotlin.databinding.FragmentProjectsBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectStatus
import com.alperensarac.projectmanagementkotlin.feature.projects.adapter.ProjectPagingAdapter
import com.alperensarac.projectmanagementkotlin.feature.projects.adapter.ProjectsLoadStateAdapter
import com.alperensarac.projectmanagementkotlin.feature.projects.create.CreateProjectFragment
import com.alperensarac.projectmanagementkotlin.feature.projects.detail.ProjectDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Paging 3 tabanlı proje listesi ekranıdır.
 *
 * Admin kullanıcılarda yeni proje oluşturma desteği de sağlar.
 */
@AndroidEntryPoint
class ProjectsFragment :
    Fragment() {

    private var _binding:
            FragmentProjectsBinding? =
        null

    private val binding:
            FragmentProjectsBinding
        get() =
            checkNotNull(_binding)

    private val viewModel:
            ProjectsViewModel
            by viewModels()

    @Inject
    lateinit var dateTimeFormatter:
            DateTimeFormatter

    private lateinit var projectAdapter:
            ProjectPagingAdapter

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentProjectsBinding.inflate(
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

        configureFilters()

        configureActions()

        observeProjects()

        observeLoadState()

        observeCreatePermission()

        observeProjectCreationResult()

        observeProjectDetailResult()
    }

    // =========================================================================
    // RECYCLER VIEW
    // =========================================================================

    private fun configureRecyclerView() {

        projectAdapter =
            ProjectPagingAdapter(
                dateTimeFormatter =
                dateTimeFormatter,

                onProjectClicked = { project ->

                    openProjectDetail(
                        projectId =
                        project.id
                    )
                }
            )

        binding.recyclerViewProjects
            .apply {

                layoutManager =
                    LinearLayoutManager(
                        requireContext()
                    )

                adapter =
                    projectAdapter
                        .withLoadStateFooter(
                            footer =
                            ProjectsLoadStateAdapter(
                                retry =
                                projectAdapter::retry
                            )
                        )
            }
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================

    private fun configureActions() {

        binding.buttonCreateProject
            .setOnClickListener {

                openCreateProject()
            }
    }

    private fun openCreateProject() {

        if (
            !viewModel
                .canCreateProject
                .value
        ) {
            return
        }

        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.projectsFragment
        ) {
            return
        }

        findNavController()
            .navigate(
                R.id.action_projectsFragment_to_createProjectFragment
            )
    }

    // =========================================================================
    // DETAIL
    // =========================================================================

    private fun openProjectDetail(
        projectId: Int
    ) {

        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.projectsFragment
        ) {
            return
        }

        findNavController()
            .navigate(
                R.id.action_projectsFragment_to_projectDetailFragment,

                bundleOf(
                    ProjectDetailFragment
                        .ARG_PROJECT_ID
                            to projectId
                )
            )
    }

    // =========================================================================
    // FILTERS
    // =========================================================================

    private fun configureFilters() {

        binding.editTextProjectSearch
            .doAfterTextChanged { editable ->

                viewModel.onSearchChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }

        configureStatusFilter()

        configureArchivedFilter()

        binding.swipeRefreshProjects
            .setOnRefreshListener {

                projectAdapter.refresh()
            }

        binding.buttonRetryProjects
            .setOnClickListener {

                projectAdapter.retry()
            }
    }

    // =========================================================================
    // STATUS FILTER
    // =========================================================================

    private fun configureStatusFilter() {

        /*
         * Backend enum'un tamamını kullanıyoruz:
         *
         * Planning
         * Active
         * OnHold
         * Completed
         * Cancelled
         */
        val statuses =
            ProjectStatus.entries

        val statusLabels =
            listOf(
                getString(
                    R.string.projects_status_all
                )
            ) +
                    statuses.map {
                        it.displayName
                    }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                statusLabels
            )

        binding.autoCompleteProjectStatus
            .setAdapter(
                adapter
            )

        binding.autoCompleteProjectStatus
            .setText(
                getString(
                    R.string.projects_status_all
                ),
                false
            )

        binding.autoCompleteProjectStatus
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                if (
                    position == 0
                ) {

                    viewModel.onStatusChanged(
                        null
                    )

                } else {

                    /*
                     * İlk item "Tümü" olduğu için enum index'i
                     * position - 1'dir.
                     */
                    val selectedStatus =
                        statuses[
                            position - 1
                        ]

                    viewModel.onStatusChanged(
                        selectedStatus.apiValue
                    )
                }
            }
    }

    // =========================================================================
    // ARCHIVED FILTER
    // =========================================================================

    private fun configureArchivedFilter() {

        val archivedItems =
            listOf(
                getString(
                    R.string.projects_archived_active_only
                ),

                getString(
                    R.string.projects_archived_only
                ),

                getString(
                    R.string.projects_archived_all
                )
            )

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                archivedItems
            )

        binding.autoCompleteProjectArchived
            .setAdapter(
                adapter
            )

        binding.autoCompleteProjectArchived
            .setText(
                getString(
                    R.string.projects_archived_active_only
                ),
                false
            )

        binding.autoCompleteProjectArchived
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val filter =
                    when (
                        position
                    ) {

                        0 ->
                            ArchivedFilter.ACTIVE_ONLY

                        1 ->
                            ArchivedFilter.ARCHIVED_ONLY

                        else ->
                            ArchivedFilter.ALL
                    }

                viewModel
                    .onArchivedFilterChanged(
                        filter
                    )
            }
    }

    // =========================================================================
    // PROJECTS
    // =========================================================================

    private fun observeProjects() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.projects
                            .collectLatest { pagingData ->

                                projectAdapter
                                    .submitData(
                                        pagingData
                                    )
                            }
                    }
            }
    }

    // =========================================================================
    // ADMIN PERMISSION
    // =========================================================================

    private fun observeCreatePermission() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel
                            .canCreateProject
                            .collect { canCreate ->

                                binding
                                    .buttonCreateProject
                                    .isVisible =
                                    canCreate
                            }
                    }
            }
    }

    // =========================================================================
    // CREATE RESULT
    // =========================================================================

    private fun observeProjectCreationResult() {

        val savedStateHandle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?: return

        savedStateHandle
            .getLiveData<Boolean>(
                CreateProjectFragment
                    .RESULT_PROJECT_CREATED
            )
            .observe(
                viewLifecycleOwner
            ) { created ->

                if (
                    created == true
                ) {

                    /*
                     * Backend'e tekrar GET atarak yeni projeyi listeye
                     * getiriyoruz.
                     */
                    projectAdapter.refresh()

                    /*
                     * Aynı sonucu Fragment tekrar STARTED olduğunda
                     * yeniden tüketmemesi için siliyoruz.
                     */
                    savedStateHandle.remove<Boolean>(
                        CreateProjectFragment
                            .RESULT_PROJECT_CREATED
                    )

                    savedStateHandle.remove<Int>(
                        CreateProjectFragment
                            .RESULT_CREATED_PROJECT_ID
                    )
                }
            }
    }

    // =========================================================================
    // LOAD STATE
    // =========================================================================

    private fun observeLoadState() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        projectAdapter
                            .loadStateFlow
                            .collectLatest { loadStates ->

                                val refreshState =
                                    loadStates.refresh

                                val isLoading =
                                    refreshState is
                                            LoadState.Loading

                                val isError =
                                    refreshState is
                                            LoadState.Error

                                binding
                                    .progressIndicatorProjects
                                    .isVisible =
                                    isLoading &&
                                            projectAdapter
                                                .itemCount == 0 &&
                                            !binding
                                                .swipeRefreshProjects
                                                .isRefreshing

                                if (
                                    !isLoading
                                ) {

                                    binding
                                        .swipeRefreshProjects
                                        .isRefreshing =
                                        false
                                }

                                binding
                                    .layoutProjectsEmpty
                                    .isVisible =
                                    refreshState is
                                            LoadState.NotLoading &&
                                            projectAdapter
                                                .itemCount == 0

                                binding
                                    .layoutProjectsError
                                    .isVisible =
                                    isError &&
                                            projectAdapter
                                                .itemCount == 0

                                binding
                                    .recyclerViewProjects
                                    .isVisible =
                                    projectAdapter
                                        .itemCount > 0

                                if (
                                    refreshState is
                                            LoadState.Error
                                ) {

                                    binding
                                        .textViewProjectsError
                                        .text =
                                        refreshState
                                            .error
                                            .message
                                            ?: getString(
                                                R.string.projects_load_error
                                            )
                                }
                            }
                    }
            }
    }

    private fun observeProjectDetailResult() {

        val handle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?: return

        handle
            .getLiveData<Boolean>(
                ProjectDetailFragment
                    .RESULT_PROJECT_CHANGED
            )
            .observe(
                viewLifecycleOwner
            ) { changed ->

                if (
                    changed == true
                ) {

                    projectAdapter.refresh()

                    handle.remove<Boolean>(
                        ProjectDetailFragment
                            .RESULT_PROJECT_CHANGED
                    )
                }
            }
    }

    // =========================================================================
    // DESTROY
    // =========================================================================

    override fun onDestroyView() {

        binding.recyclerViewProjects
            .adapter =
            null

        _binding =
            null

        super.onDestroyView()
    }
}