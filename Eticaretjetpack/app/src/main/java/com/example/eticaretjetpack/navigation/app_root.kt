package com.example.eticaretjetpack.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eticaretjetpack.components.MainScaffold
import com.example.eticaretjetpack.view.LoginScreen
import com.example.eticaretjetpack.view.RegisterScreen

@Composable
fun AppRoot() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onGoRegister = { nav.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onGoLogin = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // Main (BottomBar + inner destinations)
        composable(Routes.HOME) {
            MainScaffold(rootNav = nav, startTab = Routes.HOME)
        }
        composable(Routes.CART) {
            MainScaffold(rootNav = nav, startTab = Routes.CART)
        }
        composable(Routes.ORDERS) {
            MainScaffold(rootNav = nav, startTab = Routes.ORDERS)
        }
        composable(Routes.SETTINGS) {
            MainScaffold(rootNav = nav, startTab = Routes.SETTINGS)
        }
    }
}
