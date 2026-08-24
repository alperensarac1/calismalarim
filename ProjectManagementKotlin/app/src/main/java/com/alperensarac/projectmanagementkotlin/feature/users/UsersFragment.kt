package com.alperensarac.projectmanagementkotlin.feature.users

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
import com.alperensarac.projectmanagementkotlin.databinding.FragmentUsersBinding
import com.alperensarac.projectmanagementkotlin.domain.model.users.UserRole
import com.alperensarac.projectmanagementkotlin.feature.users.adapter.UserPagingAdapter
import com.alperensarac.projectmanagementkotlin.feature.users.create.CreateUserFragment
import com.alperensarac.projectmanagementkotlin.feature.users.detail.UserDetailFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Admin kullanıcı yönetim ekranıdır.
 */
@AndroidEntryPoint
class UsersFragment :
    Fragment() {

    private var _binding:
            FragmentUsersBinding? =
        null

    private val binding:
            FragmentUsersBinding
        get() =
            checkNotNull(_binding)

    private val viewModel:
            UsersViewModel
            by viewModels()

    private lateinit var userAdapter:
            UserPagingAdapter

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentUsersBinding.inflate(
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

        configureRoleFilter()

        configureActiveFilter()

        configureActions()

        configureToolbar()

        observeUsers()

        observeLoadState()

        observeCreateResult()

        observeUserDetailResult()
    }

    // =========================================================================
    // RECYCLER VIEW
    // =========================================================================

    private fun configureRecyclerView() {

        /*
         * UserPagingAdapter artık kullanıcı tıklama callback'i bekliyor.
         *
         * Kullanıcı kartına basıldığında UserDetailFragment açılır.
         */
        userAdapter =
            UserPagingAdapter(
                onUserClicked = { user ->

                    openUserDetail(
                        userId = user.id
                    )
                }
            )

        binding.recyclerViewUsers.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                userAdapter
        }
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    private fun configureSearch() {

        binding.editTextUserSearch
            .doAfterTextChanged { editable ->

                viewModel.onSearchChanged(
                    editable
                        ?.toString()
                        .orEmpty()
                )
            }
    }

    // =========================================================================
    // ROLE FILTER
    // =========================================================================

    private fun configureRoleFilter() {

        val roles =
            UserRole.entries

        val labels =
            listOf(
                getString(
                    R.string.users_role_all
                )
            ) +
                    roles.map {
                        it.displayName
                    }

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteUserRole
            .setAdapter(
                adapter
            )

        binding.autoCompleteUserRole
            .setText(
                getString(
                    R.string.users_role_all
                ),
                false
            )

        binding.autoCompleteUserRole
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                if (
                    position == 0
                ) {

                    viewModel.onRoleChanged(
                        null
                    )

                } else {

                    viewModel.onRoleChanged(
                        roles[
                            position - 1
                        ].apiValue
                    )
                }
            }
    }

    // =========================================================================
    // ACTIVE FILTER
    // =========================================================================

    private fun configureActiveFilter() {

        val labels =
            listOf(
                getString(
                    R.string.users_status_all
                ),

                getString(
                    R.string.users_status_active
                ),

                getString(
                    R.string.users_status_passive
                )
            )

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                labels
            )

        binding.autoCompleteUserActive
            .setAdapter(
                adapter
            )

        binding.autoCompleteUserActive
            .setText(
                getString(
                    R.string.users_status_all
                ),
                false
            )

        binding.autoCompleteUserActive
            .setOnItemClickListener {
                    _,
                    _,
                    position,
                    _ ->

                val filter =
                    when (
                        position
                    ) {

                        1 ->
                            UserActiveFilter.ACTIVE_ONLY

                        2 ->
                            UserActiveFilter.PASSIVE_ONLY

                        else ->
                            UserActiveFilter.ALL
                    }

                viewModel
                    .onActiveFilterChanged(
                        filter
                    )
            }
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================

    private fun configureActions() {

        binding.buttonAddUser
            .setOnClickListener {

                if (
                    findNavController()
                        .currentDestination
                        ?.id !=
                    R.id.usersFragment
                ) {
                    return@setOnClickListener
                }

                findNavController()
                    .navigate(
                        R.id.action_usersFragment_to_createUserFragment
                    )
            }

        binding.swipeRefreshUsers
            .setOnRefreshListener {

                userAdapter.refresh()
            }

        binding.buttonRetryUsers
            .setOnClickListener {

                userAdapter.retry()
            }
    }

    // =========================================================================
    // USERS
    // =========================================================================

    private fun observeUsers() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {

                        viewModel.users
                            .collectLatest { pagingData ->

                                userAdapter
                                    .submitData(
                                        pagingData
                                    )
                            }
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

                        userAdapter
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

                                binding.progressIndicatorUsers
                                    .isVisible =
                                    isLoading &&
                                            userAdapter
                                                .itemCount == 0 &&
                                            !binding
                                                .swipeRefreshUsers
                                                .isRefreshing

                                if (
                                    !isLoading
                                ) {

                                    binding.swipeRefreshUsers
                                        .isRefreshing =
                                        false
                                }

                                binding.textViewUsersEmpty
                                    .isVisible =
                                    refresh is
                                            LoadState.NotLoading &&
                                            userAdapter
                                                .itemCount == 0

                                binding.layoutUsersError
                                    .isVisible =
                                    isError &&
                                            userAdapter
                                                .itemCount == 0

                                binding.recyclerViewUsers
                                    .isVisible =
                                    userAdapter
                                        .itemCount > 0

                                if (
                                    refresh is
                                            LoadState.Error
                                ) {

                                    binding.textViewUsersError.text =
                                        refresh
                                            .error
                                            .message
                                            ?: getString(
                                                R.string.users_load_error
                                            )
                                }
                            }
                    }
            }
    }

    // =========================================================================
    // CREATE RESULT
    // =========================================================================

    private fun observeCreateResult() {

        val handle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?: return

        handle
            .getLiveData<Boolean>(
                CreateUserFragment
                    .RESULT_USER_CREATED
            )
            .observe(
                viewLifecycleOwner
            ) { created ->

                if (
                    created == true
                ) {

                    userAdapter.refresh()

                    handle.remove<Boolean>(
                        CreateUserFragment
                            .RESULT_USER_CREATED
                    )
                }
            }
    }
    private fun openUserDetail(
        userId: Int
    ) {

        /*
         * Double click veya navigation devam ederken ikinci kez navigate
         * çağrılmasını engelliyoruz.
         */
        if (
            findNavController()
                .currentDestination
                ?.id !=
            R.id.usersFragment
        ) {
            return
        }

        findNavController()
            .navigate(
                R.id.action_usersFragment_to_userDetailFragment,

                bundleOf(
                    UserDetailFragment.ARG_USER_ID
                            to userId
                )
            )
    }

    private fun observeUserDetailResult() {

        val handle =
            findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?: return

        handle
            .getLiveData<Boolean>(
                UserDetailFragment
                    .RESULT_USER_CHANGED
            )
            .observe(
                viewLifecycleOwner
            ) { changed ->

                if (
                    changed == true
                ) {

                    /*
                     * Detay ekranında:
                     *
                     * - update
                     * - aktif/pasif
                     * - delete
                     *
                     * işlemlerinden biri yapılmışsa backend listesini tekrar
                     * yükleriz.
                     */
                    userAdapter.refresh()

                    /*
                     * Event'i yalnızca bir kere tüketiyoruz.
                     */
                    handle.remove<Boolean>(
                        UserDetailFragment
                            .RESULT_USER_CHANGED
                    )
                }
            }
    }

    // =========================================================================
    // TOOLBAR
    // =========================================================================

    /**
     * Kullanıcı yönetimi ekranının üst toolbar'ını ayarlar.
     *
     * Geri butonuna basıldığında Navigation Component mevcut
     * back stack'teki bir önceki ekrana döner.
     *
     * Bu ekran ProfileFragment üzerinden açıldığı için
     * kullanıcı tekrar profil ekranına döner.
     */
    private fun configureToolbar() {

        binding.toolbarUsers
            .setNavigationOnClickListener {

                findNavController()
                    .navigateUp()
            }
    }

    // =========================================================================
    // DESTROY
    // =========================================================================



    override fun onDestroyView() {

        binding.recyclerViewUsers
            .adapter =
            null

        _binding =
            null

        super.onDestroyView()
    }
}