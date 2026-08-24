package com.alperensarac.projectmanagementkotlin.feature.projects.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import com.alperensarac.projectmanagementkotlin.databinding.FragmentProjectDetailBinding
import com.alperensarac.projectmanagementkotlin.domain.model.projects.Project
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMember
import com.alperensarac.projectmanagementkotlin.domain.model.projects.ProjectMemberRole
import com.alperensarac.projectmanagementkotlin.feature.projects.detail.adapter.ProjectMemberAdapter
import com.alperensarac.projectmanagementkotlin.feature.projects.detail.member.AddProjectMemberDialogFragment
import com.alperensarac.projectmanagementkotlin.feature.projects.edit.EditProjectFragment
import com.alperensarac.projectmanagementkotlin.feature.tasks.form.TaskFormDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Proje detay ekranıdır.
 */
@AndroidEntryPoint
class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? =
        null

    private val binding: FragmentProjectDetailBinding
        get() =
            checkNotNull(_binding) {
                "FragmentProjectDetailBinding view lifecycle dışında kullanılamaz."
            }

    private val viewModel: ProjectDetailViewModel by viewModels()

    @Inject
    lateinit var dateTimeFormatter: DateTimeFormatter

    private lateinit var projectMemberAdapter: ProjectMemberAdapter

    /**
     * Adapter callback'lerinin güncel state'e erişebilmesi için saklanır.
     */
    private var latestUiState =
        ProjectDetailUiState()

    private val projectId: Int
        get() =
            requireArguments().getInt(
                ARG_PROJECT_ID,
                INVALID_PROJECT_ID
            )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentProjectDetailBinding.inflate(
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

        /*
         * Mevcut dosyada fonksiyon vardı fakat çağrılmıyordu.
         */
        configureMemberAddedResult()

        configureTaskCreatedResult()

        observeUiState()

        observeUiEvents()

        configureProjectUpdatedResult()

        if (
            savedInstanceState == null &&
            latestUiState.project == null
        ) {

            viewModel.loadProject(
                projectId = projectId
            )
        }
    }

    // =========================================================================
    // RECYCLER VIEW
    // =========================================================================

    private fun configureRecyclerView() {

        projectMemberAdapter =
            ProjectMemberAdapter(

                dateTimeFormatter =
                dateTimeFormatter,

                canManageMembers = {
                    latestUiState.canMutateMembers
                },

                processingUserId = {
                    latestUiState.processingMemberUserId
                },

                onChangeRoleClicked = { member ->
                    showChangeRoleDialog(
                        member = member
                    )
                },

                onRemoveMemberClicked = { member ->
                    showRemoveMemberConfirmation(
                        member = member
                    )
                }
            )

        binding.recyclerViewProjectMembers.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                projectMemberAdapter

            /*
             * RecyclerView zaten NestedScrollView içerisinde.
             *
             * İki ayrı scroll sistemi çakışmasın.
             */
            isNestedScrollingEnabled =
                false
        }
    }

    // =========================================================================
    // LISTENERS
    // =========================================================================

    private fun configureListeners() {

        // -------------------------------------------------------------------------
        // BACK
        // -------------------------------------------------------------------------

        binding.buttonProjectDetailBack
            .setOnClickListener {

                findNavController()
                    .navigateUp()
            }

        // -------------------------------------------------------------------------
        // REFRESH
        // -------------------------------------------------------------------------

        binding.swipeRefreshProjectDetail
            .setOnRefreshListener {

                viewModel.refresh(
                    projectId = projectId
                )
            }

        // -------------------------------------------------------------------------
        // RETRY
        // -------------------------------------------------------------------------

        binding.buttonRetryProjectDetail
            .setOnClickListener {

                viewModel.loadProject(
                    projectId = projectId
                )
            }

        // -------------------------------------------------------------------------
        // ADD MEMBER
        // -------------------------------------------------------------------------

        binding.buttonAddProjectMember
            .setOnClickListener {

                if (
                    !latestUiState.canAddMembers
                ) {
                    return@setOnClickListener
                }

                AddProjectMemberDialogFragment
                    .newInstance(
                        projectId = projectId
                    )
                    .show(
                        childFragmentManager,
                        "AddProjectMemberDialog"
                    )
            }

        // -------------------------------------------------------------------------
        // CREATE TASK
        // -------------------------------------------------------------------------

        // -------------------------------------------------------------------------
// CREATE TASK
// -------------------------------------------------------------------------

        binding.buttonCreateProjectTask
            .setOnClickListener {

                /*
                 * Buton görünürlük kontrolüne güvenmiyoruz.
                 *
                 * İleride UI tarafında yanlışlıkla görünür hale gelirse bile
                 * burada ikinci bir guard bulunuyor.
                 */
                if (
                    !latestUiState.canCreateTask
                ) {
                    return@setOnClickListener
                }

                /*
                 * Aynı dialog'un hızlı çift tıklamada iki kere açılmasını
                 * engelliyoruz.
                 */
                val existingDialog =
                    childFragmentManager
                        .findFragmentByTag(
                            CREATE_TASK_DIALOG_TAG
                        )

                if (
                    existingDialog != null
                ) {
                    return@setOnClickListener
                }

                TaskFormDialogFragment
                    .newCreateInstance(
                        projectId = projectId
                    )
                    .show(
                        childFragmentManager,
                        CREATE_TASK_DIALOG_TAG
                    )
            }

        // -------------------------------------------------------------------------
        // ARCHIVE
        // -------------------------------------------------------------------------

        binding.buttonArchiveProject
            .setOnClickListener {

                val project =
                    latestUiState.project
                        ?: return@setOnClickListener

                showArchiveConfirmation(
                    project = project
                )
            }

        // -------------------------------------------------------------------------
        // DELETE PROJECT
        // -------------------------------------------------------------------------

        binding.buttonDeleteProject
            .setOnClickListener {

                val project =
                    latestUiState.project
                        ?: return@setOnClickListener

                showDeleteProjectConfirmation(
                    project = project
                )
            }
        binding.buttonEditProject
            .setOnClickListener {

                if (
                    !latestUiState.canEditProject
                ) {
                    return@setOnClickListener
                }

                if (
                    findNavController()
                        .currentDestination
                        ?.id !=
                    R.id.projectDetailFragment
                ) {
                    return@setOnClickListener
                }

                findNavController()
                    .navigate(
                        R.id.action_projectDetailFragment_to_editProjectFragment,

                        bundleOf(
                            EditProjectFragment.ARG_PROJECT_ID
                                    to projectId
                        )
                    )
            }
    }
    /**
     * Dialog başarılı şekilde üye eklediğinde proje üye listesini tekrar
     * backend'den getiriyoruz.
     */
    private fun configureMemberAddedResult() {

        childFragmentManager
            .setFragmentResultListener(
                AddProjectMemberDialogFragment.REQUEST_MEMBER_ADDED,
                viewLifecycleOwner
            ) { _, _ ->

                viewModel.refresh(
                    projectId = projectId
                )

                Snackbar.make(
                    binding.root,
                    R.string.add_member_success,
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }
    // =========================================================================
    // CHANGE ROLE DIALOG
    // =========================================================================

    /**
     * Kullanıcının proje rolünü değiştiren dialog.
     */
    private fun showChangeRoleDialog(
        member: ProjectMember
    ) {

        val roles =
            ProjectMemberRole.entries

        /*
         * UI üzerinde gösterilecek isimler.
         *
         * API değerini kullanıcıya direkt göstermek yerine Türkçe
         * karşılıklarını kullanıyoruz.
         */
        val roleLabels =
            roles.map { role ->

                when (role) {

                    ProjectMemberRole.MEMBER ->
                        getString(
                            R.string.project_member_role_member
                        )

                    ProjectMemberRole.CONTRIBUTOR ->
                        getString(
                            R.string.project_member_role_contributor
                        )

                    ProjectMemberRole.VIEWER ->
                        getString(
                            R.string.project_member_role_viewer
                        )
                }
            }
                .toTypedArray()

        /*
         * Mevcut rolün dialog'daki index'i.
         */
        val currentRole =
            ProjectMemberRole.fromApiValue(
                member.projectRole
            )

        val currentIndex =
            roles.indexOf(
                currentRole
            )
                .takeIf { it >= 0 }
                ?: 0

        var selectedIndex =
            currentIndex

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setTitle(
                    getString(
                        R.string.project_member_change_role_dialog_title,
                        member.fullName
                    )
                )
                .setSingleChoiceItems(
                    roleLabels,
                    currentIndex
                ) { _, which ->
                    selectedIndex =
                        which
                }
                .setNegativeButton(
                    R.string.action_cancel,
                    null
                )
                .setPositiveButton(
                    R.string.action_save,
                    null
                )
                .create()

        /*
         * Pozitif butona kendi listener'ımızı set ediyoruz.
         *
         * Bunun avantajı:
         *
         * Validation başarısız olduğunda dialog otomatik kapanmaz.
         */
        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            )
                .setOnClickListener {

                    val selectedRole =
                        roles[selectedIndex]

                    if (
                        selectedRole ==
                        currentRole
                    ) {

                        Snackbar.make(
                            binding.root,
                            R.string.project_member_role_not_changed,
                            Snackbar.LENGTH_SHORT
                        ).show()

                        return@setOnClickListener
                    }

                    viewModel.updateMemberRole(
                        projectId = projectId,
                        member = member,
                        newRole = selectedRole
                    )

                    dialog.dismiss()
                }
        }

        dialog.show()
    }

    // =========================================================================
    // REMOVE DIALOG
    // =========================================================================

    private fun showRemoveMemberConfirmation(
        member: ProjectMember
    ) {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                R.string.project_member_remove_dialog_title
            )
            .setMessage(
                getString(
                    R.string.project_member_remove_dialog_message,
                    member.fullName
                )
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.project_member_remove
            ) { _, _ ->

                viewModel.removeMember(
                    projectId = projectId,
                    member = member
                )
            }
            .show()
    }

    // =========================================================================
    // OBSERVERS
    // =========================================================================

    private fun observeUiState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.uiState.collect { state ->

                    latestUiState =
                        state

                    renderUiState(
                        state = state
                    )
                }
            }
        }
    }

    private fun observeUiEvents() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.events
                            .collect { event ->

                                when (
                                    event
                                ) {

                                    // =================================================
                                    // NORMAL MESSAGE
                                    // =================================================

                                    is ProjectDetailUiEvent.ShowMessage -> {

                                        Snackbar.make(
                                            binding.root,
                                            event.message,
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }

                                    // =================================================
                                    // PROJECT CHANGED
                                    // =================================================

                                    is ProjectDetailUiEvent.ProjectChanged -> {

                                        /*
                                         * ProjectsFragment'a:
                                         *
                                         * "Bu proje değişti, listeyi refresh et."
                                         *
                                         * sonucunu bırakıyoruz.
                                         */
                                        sendProjectChangedResult()

                                        Snackbar.make(
                                            binding.root,
                                            event.message,
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }

                                    // =================================================
                                    // PROJECT DELETED
                                    // =================================================

                                    is ProjectDetailUiEvent.ProjectDeleted -> {

                                        sendProjectChangedResult()

                                        Snackbar.make(
                                            binding.root,
                                            event.message,
                                            Snackbar.LENGTH_SHORT
                                        ).show()

                                        findNavController()
                                            .navigateUp()
                                    }
                                }
                            }
                    }
            }
    }
    // =========================================================================
// DELETE PROJECT
// =========================================================================

    private fun showDeleteProjectConfirmation(
        project: Project
    ) {

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                R.string.project_delete_confirmation_title
            )
            .setMessage(
                getString(
                    R.string.project_delete_confirmation_message,
                    project.name
                )
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                R.string.project_delete
            ) { _, _ ->

                viewModel.deleteProject(
                    projectId = projectId
                )
            }
            .show()
    }
// =========================================================================
// PROJECT ARCHIVE
// =========================================================================

    private fun showArchiveConfirmation(
        project: Project
    ) {

        val willArchive =
            !project.isArchived

        val title =
            if (
                willArchive
            ) {
                getString(
                    R.string.project_archive_confirmation_title
                )
            } else {
                getString(
                    R.string.project_unarchive_confirmation_title
                )
            }

        val message =
            if (
                willArchive
            ) {

                getString(
                    R.string.project_archive_confirmation_message,
                    project.name
                )

            } else {

                getString(
                    R.string.project_unarchive_confirmation_message,
                    project.name
                )
            }

        val positiveText =
            if (
                willArchive
            ) {
                getString(
                    R.string.project_archive
                )
            } else {
                getString(
                    R.string.project_unarchive
                )
            }

        AlertDialog.Builder(
            requireContext()
        )
            .setTitle(
                title
            )
            .setMessage(
                message
            )
            .setNegativeButton(
                R.string.action_cancel,
                null
            )
            .setPositiveButton(
                positiveText
            ) { _, _ ->

                viewModel.updateArchiveStatus(
                    projectId = projectId,
                    isArchived = willArchive
                )
            }
            .show()
    }
    // =========================================================================
    // RENDER
    // =========================================================================

    private fun renderUiState(
        state: ProjectDetailUiState
    ) {

        // -------------------------------------------------------------------------
        // MAIN LOADING
        // -------------------------------------------------------------------------

        binding.progressIndicatorProjectDetail
            .isVisible =
            (
                    state.isLoading &&
                            !state.hasContent
                    ) ||
                    state.isProjectOperationInProgress

        // -------------------------------------------------------------------------
        // REFRESH
        // -------------------------------------------------------------------------

        binding.swipeRefreshProjectDetail
            .isRefreshing =
            state.isRefreshing

        // -------------------------------------------------------------------------
        // CONTENT
        // -------------------------------------------------------------------------

        binding.layoutProjectDetailContent
            .isVisible =
            state.hasContent

        // -------------------------------------------------------------------------
        // ERROR
        // -------------------------------------------------------------------------

        binding.layoutProjectDetailError
            .isVisible =
            !state.errorMessage
                .isNullOrBlank() &&
                    !state.hasContent

        binding.textViewProjectDetailError
            .text =
            state.errorMessage
                .orEmpty()

        // -------------------------------------------------------------------------
        // PROJECT
        // -------------------------------------------------------------------------

        state.project?.let { project ->

            renderProject(
                project = project
            )
        }

        // -------------------------------------------------------------------------
        // PROJECT MANAGEMENT
        // -------------------------------------------------------------------------

        binding.cardProjectManagement
            .isVisible =
            state.canManageProject

        // -------------------------------------------------------------------------
        // TASK MANAGEMENT
        // -------------------------------------------------------------------------

        binding.buttonCreateProjectTask
            .isVisible =
            state.canCreateTask

        binding.buttonCreateProjectTask
            .isEnabled =
            state.canCreateTask

        /*
         * Edit ekranını sonraki adımda bağlayacağız.
         */
        binding.buttonEditProject
            .isVisible =
            state.canEditProject

        binding.buttonEditProject
            .isEnabled =
            state.canEditProject &&
                    !state.isProjectOperationInProgress

        binding.buttonArchiveProject
            .isEnabled =
            state.canArchiveProject

        binding.buttonDeleteProject
            .isEnabled =
            state.canDeleteProject


        // -------------------------------------------------------------------------
        // MEMBER MANAGEMENT
        // -------------------------------------------------------------------------

        binding.buttonAddProjectMember.isVisible =
            state.canAddMembers

        projectMemberAdapter.submitList(
            state.members
        )

        projectMemberAdapter.refreshUiState()

        binding.textViewProjectMembersEmpty
            .isVisible =
            state.hasContent &&
                    state.isMembersEmpty

        // -------------------------------------------------------------------------
        // INTERACTION LOCK
        // -------------------------------------------------------------------------

        binding.swipeRefreshProjectDetail
            .isEnabled =
            !state.isMemberOperationInProgress &&
                    !state.isProjectOperationInProgress
    }

    private fun renderProject(
        project: Project
    ) {

        binding.textViewProjectDetailName.text =
            project.name

        binding.textViewProjectDetailStatus.text =
            project.status

        binding.textViewProjectDetailDescription.text =
            project.description
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: getString(
                    R.string.projects_no_description
                )

        binding.textViewProjectDetailOwner.text =
            getString(
                R.string.project_detail_owner_format,
                project.ownerFullName
            )

        binding.textViewProjectDetailOwnerEmail.text =
            project.ownerEmail

        binding.textViewProjectDetailDates.text =
            getString(
                R.string.project_detail_dates_format,

                dateTimeFormatter.formatUtcDateTime(
                    project.startDateUtc
                ),

                project.endDateUtc
                    ?.let { date ->
                        dateTimeFormatter.formatUtcDateTime(
                            date
                        )
                    }
                    ?: getString(
                        R.string.project_detail_no_end_date
                    )
            )

        binding.textViewProjectDetailCounts.text =
            getString(
                R.string.project_detail_counts_format,
                project.memberCount,
                project.taskCount
            )

        binding.textViewProjectDetailArchived.isVisible =
            project.isArchived
        binding.buttonArchiveProject.text =
            if (
                project.isArchived
            ) {

                getString(
                    R.string.project_unarchive
                )

            } else {

                getString(
                    R.string.project_archive
                )
            }
    }

    private fun configureTaskCreatedResult() {

        childFragmentManager
            .setFragmentResultListener(
                TaskFormDialogFragment.REQUEST_TASK_SAVED,
                viewLifecycleOwner
            ) { _, _ ->

                /*
                 * ProjectResponseDto içerisinde taskCount bulunduğu için
                 * proje detayını tekrar çekiyoruz.
                 */
                viewModel.refresh(
                    projectId
                )

                Snackbar.make(
                    binding.root,
                    R.string.task_created_success,
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun sendProjectChangedResult() {

        findNavController()
            .previousBackStackEntry
            ?.savedStateHandle
            ?.set(
                RESULT_PROJECT_CHANGED,
                true
            )
    }
    private fun configureProjectUpdatedResult() {

        val handle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?: return

        handle
            .getLiveData<Boolean>(
                EditProjectFragment
                    .RESULT_PROJECT_UPDATED
            )
            .observe(
                viewLifecycleOwner
            ) { updated ->

                if (
                    updated == true
                ) {

                    /*
                     * Güncel owner / status / tarih / isim gibi bütün bilgileri
                     * backend'den tekrar alıyoruz.
                     *
                     * Aynı zamanda members de tekrar yüklenecek.
                     *
                     * Owner değiştiyse backend yeni owner'ı member listesine
                     * Contributor olarak eklemiş olabilir.
                     */
                    viewModel.refresh(
                        projectId = projectId
                    )

                    /*
                     * ProjectsFragment listesini de daha sonra refresh ettireceğiz.
                     */
                    sendProjectChangedResult()

                    handle.remove<Boolean>(
                        EditProjectFragment
                            .RESULT_PROJECT_UPDATED
                    )
                }
            }
    }
    override fun onDestroyView() {

        binding.recyclerViewProjectMembers.adapter =
            null

        _binding =
            null

        super.onDestroyView()
    }

    companion object {

        const val ARG_PROJECT_ID =
            "projectId"

        private const val INVALID_PROJECT_ID =
            -1
        const val RESULT_PROJECT_CHANGED =
            "result_project_changed"
        private const val CREATE_TASK_DIALOG_TAG =
            "CreateTaskDialog"
    }
}