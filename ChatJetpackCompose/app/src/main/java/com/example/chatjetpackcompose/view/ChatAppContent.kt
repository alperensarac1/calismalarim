package com.example.chatjetpackcompose.view

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatjetpackcompose.viewmodel.MesajlarViewModel
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.util.AppConfig
import com.example.chatkotlin.util.PrefManager

@Composable
fun ChatAppContent(pref: PrefManager,mesajlarVM:MesajlarViewModel) {
    var showRegisterDialog by remember { mutableStateOf(!pref.kullaniciVarMi()) }


    val navController = rememberNavController()

    if (showRegisterDialog) {
        RegistrationDialog(
            onKayitBasarili = { id ->
                AppConfig.kullaniciId = id
                pref.kaydetKullaniciId(id)
                showRegisterDialog = false
            }, onDismiss = {}
        )
    } else {
        NavHost(navController, startDestination = "mesajlar") {
            composable("mesajlar") {
                MesajlarScreen(navController = navController)
            }
            composable("singleChat/{aliciId}/{aliciAd}") { backStackEntry ->
                val aliciId = backStackEntry.arguments?.getString("aliciId")?.toIntOrNull() ?: -1
                val aliciAd = backStackEntry.arguments?.getString("aliciAd") ?: ""
                SingleChatScreen(aliciId, aliciAd,mesajlarVM,navController)
            }
        }
    }
}
