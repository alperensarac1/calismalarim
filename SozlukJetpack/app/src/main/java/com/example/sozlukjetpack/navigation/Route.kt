package com.example.sozlukjetpack.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sozlukjetpack.util.SessionManager
import com.example.sozlukjetpack.view.AnasayfaScreen
import com.example.sozlukjetpack.view.BugunScreen
import com.example.sozlukjetpack.view.EntryDetayScreen
import com.example.sozlukjetpack.view.EntryEkleScreen
import com.example.sozlukjetpack.view.LoginScreen
import com.example.sozlukjetpack.view.ProfilScreen
import com.example.sozlukjetpack.view.RegisterScreen
import com.example.sozlukjetpack.viewmodel.AnaSayfaViewModel
import com.example.sozlukjetpack.viewmodel.BugunViewModel
import com.example.sozlukjetpack.viewmodel.EntryDetayViewModel
import com.example.sozlukjetpack.viewmodel.EntryEkleViewModel
import com.example.sozlukjetpack.viewmodel.GirisViewModel
import com.example.sozlukjetpack.viewmodel.KayitViewModel
import com.example.sozlukjetpack.viewmodel.ProfilViewModel


object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val HOME_GUNDEM = "home/gundem"
    const val HOME_BUGUN  = "home/bugun"
    const val HOME_PROFIL = "home/profil"

    const val ENTRY_ADD    = "entry/add"
    const val ENTRY_DETAIL = "entry/detail"
    const val ARG_ID = "id"

    fun entryDetail(id: Int) = "$ENTRY_DETAIL/$id"
    val entryDetailPattern = "$ENTRY_DETAIL/{$ARG_ID}"
}
@Composable
fun AppNav(session: SessionManager) {
    val start = if (session.isLoggedIn()) Routes.HOME_GUNDEM else Routes.LOGIN
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = start) {

        composable(Routes.LOGIN) {
            val vm: GirisViewModel = viewModel()
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = { userId, username ->
                    session.saveUserSession(userId, username)
                    nav.navigate(Routes.HOME_GUNDEM) {
                        popUpTo(Routes.LOGIN) { inclusive = true } // login’i yığından sil
                        launchSingleTop = true
                    }
                },
                onGoRegister = { nav.navigate(Routes.REGISTER) { launchSingleTop = true } }
            )
        }

        composable(Routes.REGISTER) {
            val vm: KayitViewModel = viewModel()
            RegisterScreen(
                viewModel = vm,
                onRegisterSuccess = { nav.popBackStack() }, // geri login’e
                onGoLogin = { nav.popBackStack() }
            )
        }

        // ——— Sekmeler
        composable(Routes.HOME_GUNDEM) {
            val vm: AnaSayfaViewModel = viewModel()
            AnasayfaScreen(
                vm = vm,
                onNavigateEntryEkle   = { nav.navigate(Routes.ENTRY_ADD) },
                onNavigateEntryDetay  = { id -> nav.navigate(Routes.entryDetail(id)) },
                onNavigateBugun       = { nav.navigate(Routes.HOME_BUGUN) {
                    launchSingleTop = true
                    popUpTo(Routes.HOME_GUNDEM) { saveState = true }
                    restoreState = true
                }},
                onNavigateProfil      = { nav.navigate(Routes.HOME_PROFIL) {
                    launchSingleTop = true
                    popUpTo(Routes.HOME_GUNDEM) { saveState = true }
                    restoreState = true
                }}
            )
        }

        composable(Routes.HOME_BUGUN) {
            val vm: BugunViewModel = viewModel()
            BugunScreen(
                vm = vm,
                onNavigateGundem = { nav.navigate(Routes.HOME_GUNDEM) {
                    launchSingleTop = true
                    popUpTo(Routes.HOME_GUNDEM) { saveState = true }
                    restoreState = true
                }},
                onNavigateProfil = { nav.navigate(Routes.HOME_PROFIL) {
                    launchSingleTop = true
                    popUpTo(Routes.HOME_GUNDEM) { saveState = true }
                    restoreState = true
                }},
                onNavigateEntryDetay = { id -> nav.navigate(Routes.entryDetail(id)) }
            )
        }

        composable(Routes.HOME_PROFIL) {
            val vm: ProfilViewModel = viewModel()
            ProfilScreen(
                userId = session.getUserId(),
                session = session,
                vm = vm,
                onNavigateGundem = { nav.navigate(Routes.HOME_GUNDEM) {
                    launchSingleTop = true
                    popUpTo(Routes.HOME_GUNDEM) { saveState = true }
                    restoreState = true
                }},
                onNavigateBugun = { nav.navigate(Routes.HOME_BUGUN) {
                    launchSingleTop = true
                    popUpTo(Routes.HOME_GUNDEM) { saveState = true }
                    restoreState = true
                }},
                onNavigateEntryDetay = { id -> nav.navigate(Routes.entryDetail(id)) },
                onLoggedOut = {
                    session.clearSession()
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(0) // tüm backstack temiz (Compose 2.7+ route string popUpTo kabul eder; değilse 0 hilesi çalışmazsa start'a ver)
                        launchSingleTop = true
                    }
                }
            )
        }

        // ——— İşlemler
        composable(Routes.ENTRY_ADD) {
            val vm: EntryEkleViewModel = viewModel()
            EntryEkleScreen(
                session = session,
                vm = vm,
                onSaved = { nav.popBackStack() },
                onBack  = { nav.popBackStack() }
            )
        }

        composable(
            route = Routes.entryDetailPattern,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val vm: EntryDetayViewModel = viewModel()
            val id = backStackEntry.arguments?.getInt(Routes.ARG_ID) ?: -1
            EntryDetayScreen(
                entryId = id,
                session = session,
                vm = vm,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
