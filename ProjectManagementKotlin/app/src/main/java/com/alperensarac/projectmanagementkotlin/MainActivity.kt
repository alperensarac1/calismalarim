package com.alperensarac.projectmanagementkotlin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.alperensarac.projectmanagementkotlin.core.auth.session.SessionEvent
import com.alperensarac.projectmanagementkotlin.core.auth.session.SessionEventBus
import com.alperensarac.projectmanagementkotlin.core.theme.ThemeManager
import com.alperensarac.projectmanagementkotlin.databinding.ActivityMainBinding
import com.alperensarac.projectmanagementkotlin.feature.theme.ThemeViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Uygulamanın tek Activity sınıfıdır.
 *
 * Splash, Login ve Home container ana navigation graph içerisinde
 * yönetilir.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var sessionEventBus: SessionEventBus
    private val themeViewModel:
            ThemeViewModel
            by viewModels()

    @Inject
    lateinit var themeManager:
            ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeTheme()
        configureNavigation()
        observeSessionEvents()
    }

    private fun configureNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.mainNavHostFragment
            ) as? NavHostFragment
                ?: error(
                    "mainNavHostFragment bulunamadı. activity_main.xml dosyasını kontrol edin."
                )

        navController = navHostFragment.navController
    }

    /**
     * Authenticator veya profil ekranından yayınlanan global oturum
     * olaylarını dinler.
     */
    private fun observeSessionEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                sessionEventBus.events.collect { event ->
                    when (event) {
                        SessionEvent.SessionExpired -> {
                            navigateToLogin(
                                message = getString(
                                    R.string.session_expired_message
                                )
                            )
                        }

                        SessionEvent.UserLoggedOut -> {
                            navigateToLogin(
                                message = getString(
                                    R.string.logout_success_message
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Login ekranını ana navigation root'u yapar.
     *
     * Böylece kullanıcı geri tuşuyla Home veya korunan ekranlara dönemez.
     */
    private fun navigateToLogin(
        message: String?
    ) {
        if (!::navController.isInitialized) {
            return
        }

        if (navController.currentDestination?.id != R.id.loginFragment) {
            val navigationOptions =
                NavOptions.Builder()
                    .setPopUpTo(
                        destinationId = navController.graph.id,
                        inclusive = true
                    )
                    .setLaunchSingleTop(true)
                    .build()

            navController.navigate(
                R.id.loginFragment,
                null,
                navigationOptions
            )
        }

        message
            ?.takeIf { it.isNotBlank() }
            ?.let { snackbarMessage ->
                Snackbar.make(
                    binding.root,
                    snackbarMessage,
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }
    /**
     * DataStore üzerindeki tema tercihini gözlemler.
     *
     * Tema değiştiğinde:
     *
     * LIGHT
     *    ↓
     * MODE_NIGHT_NO
     *
     * DARK
     *    ↓
     * MODE_NIGHT_YES
     */
    private fun observeTheme() {

        lifecycleScope.launch {

            repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                themeViewModel
                    .themeMode
                    .collect { mode ->

                        themeManager
                            .applyTheme(
                                mode
                            )
                    }
            }
        }
    }
}