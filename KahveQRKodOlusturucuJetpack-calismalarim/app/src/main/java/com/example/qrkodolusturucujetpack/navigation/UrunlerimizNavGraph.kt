package com.example.qrkodolusturucujetpack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qrkodolusturucujetpack.model.Urun
import com.example.qrkodolusturucujetpack.view.UrunDetayScreen
import com.example.qrkodolusturucujetpack.view.UrunlerimizScreen
import com.google.gson.Gson

@Composable
fun UrunlerimizNavGraph(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "urunlerimiz"){


        composable(route = "urunlerimiz"){
            UrunlerimizScreen(navController)
        }

        composable(
            "urunDetay/{urunJson}",
            arguments = listOf(navArgument("urunJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val urunJson = backStackEntry.arguments?.getString("urunJson") ?: ""
            val urun = Gson().fromJson(urunJson, Urun::class.java)
            UrunDetayScreen(
                urun,
                onBackClick = {}
            )
        }
    }

}