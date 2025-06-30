package com.example.isletimsistemicalismasijetpackcompose.uygulamalar

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.isletimsistemicalismasijetpackcompose.anaekran.UygulamaEkraniScreen
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.GirisYapScreen
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.GuvenlikSorusuSecScreen
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.KayitOlusturScreen
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.SifremiUnuttumScreen
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.alarm.Alarm
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.copkutusu.CopKutusu
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.galeri.Galeri
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.hesapmakinesi.HesapMakinesiScreen
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.kamera.Kamera
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.mesajlasma.Mesajlasma
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.muzikcalar.MuzikCalar
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri.NotDefteriAnasayfa
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri.NotDefteriDetaySayfa
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri.NotDefteriEkleSayfa
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri.NotDefteriViewModel
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.qrkodokuyucu.QrOkuyucuScreen
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity.RehberDao
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.view.RehberDetay
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.view.RehberEkrani
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.takvim.Takvim
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.tarayici.Tarayici
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.telefon.Telefon

@Composable
fun UygulamalarGraph(){
    val navController = rememberNavController()
    val modifier : Modifier = Modifier
    val context = LocalContext.current
    val rehberVeritabaniYardimcisi = RehberVeritabaniYardimcisi(context)
    val rehberDao = RehberDao(rehberVeritabaniYardimcisi)
    val notDefteriViewModel = NotDefteriViewModel(context)
    NavHost(navController, startDestination = UygulamaGecisi.toUygulamaEkrani.name){
        composable(UygulamaGecisi.toUygulamaEkrani.name) {
            UygulamaEkraniScreen(modifier = modifier,navController = navController)
        }
        composable(UygulamaGecisi.toCopKutusu.name) {
            CopKutusu(modifier = modifier)
        }
        composable(UygulamaGecisi.toRehber.name) {
            RehberEkrani(navController = navController,vt = rehberDao)
        }
        composable("${UygulamaGecisi.toRehberDetay.name}/{kisi}") { backStackEntry ->
            val kisiJson = backStackEntry.arguments?.getString("kisi") ?: ""
            RehberDetay(modifier = modifier,kisiJson = kisiJson, vt = rehberDao, navController = navController)
        }
        composable(UygulamaGecisi.toMesajlasma.name) {
            Mesajlasma(modifier)
        }
        composable(UygulamaGecisi.toHesapMakinesi.name) {
            HesapMakinesiScreen(modifier)
        }
        composable(UygulamaGecisi.toTelefon.name) {
            Telefon(modifier,context)
        }
        composable(UygulamaGecisi.toTarayici.name) {
            Tarayici(modifier = modifier)
        }
        composable(UygulamaGecisi.toAlarm.name) {
            Alarm(context)
        }
        composable(UygulamaGecisi.toAlarm.name) {
            Alarm(context)
        }
        composable(UygulamaGecisi.toMuzikCalar.name) {
            MuzikCalar(modifier = modifier)
        }
        composable(UygulamaGecisi.toKamera.name) {
            Kamera(modifier = modifier)
        }
        composable(UygulamaGecisi.toGaleri.name) {
            Galeri(modifier = modifier)
        }
        composable(UygulamaGecisi.toTakvim.name) {
            Takvim(modifier = modifier)
        }
        composable(UygulamaGecisi.toQRKodOkuyucu.name) {
            QrOkuyucuScreen(modifier = modifier)
        }
        composable(UygulamaGecisi.toNotDefteri.name) {
            NotDefteriAnasayfa(modifier = modifier, navController = navController, viewModel = notDefteriViewModel)
        }
        composable("${UygulamaGecisi.toNotDefteriDetay.name}/{notId}") { backStackEntry ->
            val notId = backStackEntry.arguments?.getString("notId")?.toIntOrNull()
            NotDefteriDetaySayfa(modifier = modifier, navController = navController, notId =  notId, viewModel = notDefteriViewModel)
        }
        composable(UygulamaGecisi.toNotDefteriEkle.name) {
            NotDefteriEkleSayfa(modifier = modifier, navController = navController, viewModel = notDefteriViewModel)
        }
    }
}

enum class UygulamaGecisi{
    toUygulamaEkrani,toCopKutusu,toRehber,toRehberEkle,toRehberDetay,toHesapMakinesi,toMesajlasma,toTelefon,toTarayici,toAlarm,toMuzikCalar,toKamera,toGaleri,toTakvim,toQRKodOkuyucu,toNotDefteri,toNotDefteriEkle,toNotDefteriDetay
}