package com.example.memesharejetpack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.memesharejetpack.view.AnasayfaScreen
import com.example.memesharejetpack.view.LoginScreen
import com.example.memesharejetpack.view.OdaScreen
import com.example.memesharejetpack.view.RegisterScreen

// (Opsiyonel) Route sabitleri
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val HOME_ARG = "userId"
    const val ODA = "oda"
    const val ODA_ROOM_ARG = "roomId"
    const val ODA_USER_ARG = "userId"

    fun home(userId: Int) = "$HOME/$userId"
    fun oda(roomId: Int, userId: Int) = "$ODA/$roomId/$userId"
}

@Composable
fun NavGraph() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.LOGIN) {
        // --- LOGIN ---
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateRegister = { nav.navigate(Routes.REGISTER) },
                onLoginSuccess = { userId ->
                    nav.navigate(Routes.home(userId)) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // geri tuşunda login’e dönme
                    }
                }
            )
        }

        // --- REGISTER ---
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateLogin = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    // kayıt sonrası login’e dön
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // --- HOME (Anasayfa) ---
        composable(
            route = "${Routes.HOME}/{${Routes.HOME_ARG}}",
            arguments = listOf(navArgument(Routes.HOME_ARG) { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt(Routes.HOME_ARG) ?: 0

            // AnasayfaScreen: oda listesi + katıl/oluştur
            AnasayfaScreen(
                userId = userId,
                onOpenRoom = { roomId, uid ->
                    nav.navigate(Routes.oda(roomId, uid))
                }
            )
        }

        // --- ODA (oda detayı / paylaşım & listeleme) ---
        composable(
            route = "${Routes.ODA}/{${Routes.ODA_ROOM_ARG}}/{${Routes.ODA_USER_ARG}}",
            arguments = listOf(
                navArgument(Routes.ODA_ROOM_ARG) { type = NavType.IntType },
                navArgument(Routes.ODA_USER_ARG) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getInt(Routes.ODA_ROOM_ARG) ?: 0
            val userId = backStackEntry.arguments?.getInt(Routes.ODA_USER_ARG) ?: 0

            // FragmentOda eşleniği (VideoView = AndroidView)
            OdaScreen(
                roomId = roomId,
                userId = userId
            )
        }
    }
}
