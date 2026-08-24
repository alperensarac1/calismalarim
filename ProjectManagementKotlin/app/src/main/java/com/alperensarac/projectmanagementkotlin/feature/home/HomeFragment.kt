package com.alperensarac.projectmanagementkotlin.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alperensarac.projectmanagementkotlin.R
import com.alperensarac.projectmanagementkotlin.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Giriş yapıldıktan sonra açılan ana uygulama container ekranıdır.
 *
 * Bu Fragment:
 *
 * - BottomNavigationView bileşenini gösterir.
 * - Alt sekmelerin Navigation Graph yapısını barındırır.
 * - Dashboard, Projeler, Görevler, Mailbox ve Profil ekranları
 *   arasında geçiş yapılmasını sağlar.
 *
 * ÖNEMLİ:
 *
 * home_nav_graph içerisinde detay ekranlarımız:
 *
 * projectsFragment
 * projectDetailFragment
 * editProjectFragment
 *
 * gibi aynı graph seviyesinde bulunmaktadır.
 *
 * NavigationUI bu ekranların hangi bottom navigation sekmesine ait
 * olduğunu otomatik olarak anlayamaz.
 *
 * Bundan dolayı destination değiştiğinde hangi ana sekmenin
 * seçili görünmesi gerektiğini ayrıca belirliyoruz.
 */
@AndroidEntryPoint
class HomeFragment :
    Fragment() {

    // =========================================================================
    // BINDING
    // =========================================================================

    private var _binding:
            FragmentHomeBinding? =
        null

    private val binding:
            FragmentHomeBinding
        get() =
            checkNotNull(
                _binding
            ) {
                "FragmentHomeBinding yalnızca Fragment view yaşam döngüsü içerisinde kullanılabilir."
            }

    // =========================================================================
    // CHILD NAV CONTROLLER
    // =========================================================================

    /**
     * Home içindeki Dashboard / Projects / Tasks / Mailbox / Profile
     * navigation işlemlerini yöneten NavController.
     *
     * MainActivity'deki root NavController ile karıştırılmamalıdır.
     */
    private lateinit var childNavController:
            NavController

    // =========================================================================
    // DESTINATION LISTENER
    // =========================================================================

    /**
     * Detail ekranlarında BottomNavigationView'ın doğru sekmeyi seçili
     * göstermesini sağlar.
     *
     * Örneğin:
     *
     * projectDetailFragment
     *
     * açıldığında bottom navigation'da yine:
     *
     * Projects
     *
     * seçili görünmelidir.
     */
    private val destinationChangedListener =
        NavController.OnDestinationChangedListener {
                _,
                destination,
                _ ->

            syncBottomNavigationSelection(
                destinationId =
                destination.id
            )
        }

    // =========================================================================
    // LIFECYCLE
    // =========================================================================

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentHomeBinding.inflate(
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

        configureBottomNavigation()
    }

    // =========================================================================
    // BOTTOM NAVIGATION
    // =========================================================================

    /**
     * Child NavHostFragment'i bulur ve BottomNavigationView ile bağlar.
     */
    private fun configureBottomNavigation() {

        val childNavHostFragment =
            childFragmentManager
                .findFragmentById(
                    R.id.homeNavHostFragment
                ) as? NavHostFragment
                ?: error(
                    "homeNavHostFragment bulunamadı. fragment_home.xml dosyasını kontrol edin."
                )

        childNavController =
            childNavHostFragment
                .navController

        /*
         * Navigation Component'in standart bottom navigation desteği.
         *
         * Ana 5 sekmeye tıklama işlemlerini bu yapı yönetir.
         *
         * Ayrıca burada ayrı bir setOnItemSelectedListener
         * TANIMLAMIYORUZ.
         *
         * Çünkü setupWithNavController kendi listener'ını ekler.
         */
        binding.bottomNavigationView
            .setupWithNavController(
                childNavController
            )

        /*
         * Detail destination'larda seçili sekmenin kaybolmaması için
         * destination listener ekliyoruz.
         */
        childNavController
            .addOnDestinationChangedListener(
                destinationChangedListener
            )

        /*
         * Fragment restore edilmiş olabilir.
         *
         * O an açık olan destination'a göre ilk görünümü de
         * senkronize ediyoruz.
         */
        childNavController
            .currentDestination
            ?.id
            ?.let { destinationId ->

                syncBottomNavigationSelection(
                    destinationId =
                    destinationId
                )
            }
    }

    // =========================================================================
    // SELECTION SYNCHRONIZATION
    // =========================================================================

    /**
     * Açık olan ekranın hangi BottomNavigation sekmesine ait olduğunu
     * belirler.
     *
     * Burada selectedItemId kullanmıyoruz.
     *
     * Neden?
     *
     * selectedItemId:
     *
     * bottom navigation item click işlemini tetikleyerek navigation
     * yapmaya çalışabilir.
     *
     * Biz yalnızca görsel checked state'i değiştirmek istiyoruz.
     *
     * Bundan dolayı doğrudan:
     *
     * menu.findItem(...).isChecked = true
     *
     * kullanıyoruz.
     */
    private fun syncBottomNavigationSelection(
        destinationId: Int
    ) {

        val bottomNavigationItemId =
            when (
                destinationId
            ) {

                // =============================================================
                // DASHBOARD
                // =============================================================

                R.id.dashboardFragment -> {

                    R.id.dashboardFragment
                }

                // =============================================================
                // PROJECTS
                // =============================================================

                R.id.projectsFragment,
                R.id.projectDetailFragment,
                R.id.createProjectFragment,
                R.id.editProjectFragment -> {

                    R.id.projectsFragment
                }

                // =============================================================
                // TASKS
                // =============================================================

                R.id.tasksFragment,
                R.id.taskDetailFragment -> {

                    R.id.tasksFragment
                }

                // =============================================================
                // MAILBOX
                // =============================================================

                R.id.mailboxFragment,
                R.id.mailboxDetailFragment,
                R.id.mailboxComposeFragment -> {

                    R.id.mailboxFragment
                }

                // =============================================================
                // PROFILE
                // =============================================================

                R.id.profileFragment,
                R.id.usersFragment,
                R.id.userDetailFragment,
                R.id.createUserFragment -> {

                    R.id.profileFragment
                }

                // =============================================================
                // UNKNOWN
                // =============================================================

                else -> {

                    null
                }
            }

        /*
         * Bu destination bottom navigation altında bilinen bir ekrana
         * ait değilse mevcut seçimi bozmuyoruz.
         */
        bottomNavigationItemId
            ?: return

        /*
         * Burada navigation yapılmaz.
         *
         * Yalnızca ilgili item'ın checked görünümü değiştirilir.
         */
        binding.bottomNavigationView
            .menu
            .findItem(
                bottomNavigationItemId
            )
            ?.isChecked =
            true
    }

    // =========================================================================
    // DESTROY
    // =========================================================================

    override fun onDestroyView() {

        /*
         * Listener HomeFragment'in view lifecycle'ından daha uzun
         * yaşamamalıdır.
         *
         * Aksi durumda yeni view oluşturulduğunda eski listener da
         * çalışmaya devam edebilir.
         */
        if (
            ::childNavController.isInitialized
        ) {

            childNavController
                .removeOnDestinationChangedListener(
                    destinationChangedListener
                )
        }

        _binding =
            null

        super.onDestroyView()
    }
}