package com.example.isletimsistemicalismasijetpackcompose.giris_ekrani

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


@Composable
fun GirisEkraniGraph(){
    val navController = rememberNavController()
    val context = LocalContext.current
    NavHost(navController, startDestination = Screens.GirisYap.name){
        composable(Screens.GirisYap.name) {
            GirisYapScreen(navController = navController)
        }
        composable(Screens.KayitOlustur.name) {
            KayitOlusturScreen(context = context, navController = navController)
        }
        composable(Screens.GuvenlikSorusuSec.name) {
            GuvenlikSorusuSecScreen(context = context, navController = navController)
        }
        composable(Screens.SifremiUnuttum.name) {
            SifremiUnuttumScreen(context = context, navController = navController)
        }
    }
}

enum class Screens{
    GirisYap,KayitOlustur,GuvenlikSorusuSec,SifremiUnuttum
}