package com.example.onlinetaksijetpack.ui.navigation



import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.ui.auth.LoginScreen
import com.example.onlinetaksijetpack.ui.auth.RegisterScreen
import com.example.onlinetaksijetpack.ui.customer.CustomerHomeScreen
import com.example.onlinetaksijetpack.ui.driver.DriverHomeScreen
import com.example.onlinetaksijetpack.ui.splash.SplashScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateNext = {
                    if (sessionManager.isLoggedIn()) {
                        val role = sessionManager.getRole()
                        if (role == "driver") {
                            navController.navigate(Routes.DRIVER_HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.CUSTOMER_HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = { role ->
                    if (role == "driver") {
                        navController.navigate(Routes.DRIVER_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.CUSTOMER_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Routes.CUSTOMER_HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CUSTOMER_HOME) {
            CustomerHomeScreen()
        }

        composable(Routes.DRIVER_HOME) {
            DriverHomeScreen()
        }
    }
}