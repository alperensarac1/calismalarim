package com.example.haberuygulamajetpack.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.haberuygulamajetpack.deo.HaberDao
import com.example.haberuygulamajetpack.model.HaberModel
import com.example.haberuygulamajetpack.view.AnasayfaScreen
import com.example.haberuygulamajetpack.view.HaberDetayScreen
import com.example.haberuygulamajetpack.view.KategoriScreen
import com.google.gson.Gson
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    haberDao: HaberDao
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Anasayfa.route
    ) {
        composable(Screen.Anasayfa.route) {
            AnasayfaScreen(
                navController = navController,
                haberdao = haberDao
            )
        }

        composable(
            route = "detay/{haberJson}",
            arguments = listOf(
                navArgument("haberJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val haberJson = backStackEntry.arguments?.getString("haberJson") ?: ""
            val haber = Gson().fromJson(haberJson, HaberModel::class.java)  // JSON'dan `HaberModel`'e dönüştürme

            if (haber != null) {
                HaberDetayScreen(haber = haber, navController = navController)
            } else {
                Text("Haber bulunamadı!")
            }
        }

        composable(
            route = "kategori/{kategoriId}",
            arguments = listOf(
                navArgument("kategoriId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val kategoriId = backStackEntry.arguments?.getInt("kategoriId") ?: 0
            //KategoriScreen(kategoriId = kategoriId)
        }
    }
}