package com.example.isletimsistemicalismasijetpackcompose.giris_ekrani

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri

@Composable
fun GuvenlikSorusuSecScreen(modifier: Modifier = Modifier, context: Context,navController: NavController) {

    val kullaniciBilgileri = KullaniciBilgileri(context)

    val radio1 = remember { mutableStateOf(false) }
    val radio2 = remember { mutableStateOf(false) }
    val radio3 = remember { mutableStateOf(false) }

    val tfEnSevilenMuzikGrubu = remember { mutableStateOf("") }
    val tfEnSevilenHayvan = remember { mutableStateOf("") }
    val tfEnSevilenRenk = remember { mutableStateOf("") }

    val tvEnSevilenMuzikGrubu = "En sevdiğiniz müzik grubu nedir?"
    val tvEnSevilenRenk = "En sevdiğiniz renk nedir?"
    val tvEnSevilenHayvan = "En sevdiğiniz hayvan nedir?"

    Column(

        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "Lütfen güvenlik sorularından birisini seçerek cevaplayınız",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
        )
        Column(modifier = modifier.background(Color.Cyan)) {
            Row() {
                RadioButton(selected = radio1.value, onClick = {
                    radio2.value = false
                    radio3.value = false
                })
                Text(tvEnSevilenMuzikGrubu, modifier = modifier.padding(top = 15.dp))
            }
            if (radio1.value){
                TextField(tfEnSevilenMuzikGrubu.value, onValueChange = {tfEnSevilenMuzikGrubu.value = it})
                Button(onClick = {
                    kullaniciBilgileri.guvenlikSorusuKaydet(tvEnSevilenMuzikGrubu)
                    kullaniciBilgileri.guvenlikSorusuCevapKaydet(tfEnSevilenMuzikGrubu.value)
                    navController.navigate(Screens.KayitOlustur.name)
                }) {
                    Text("Onayla")
                }
            }
        }
        Spacer(modifier.height(20.dp))
        Column(modifier = modifier.background(Color.Cyan)) {
            Row() {
                RadioButton(selected = radio2.value, onClick = {
                    radio1.value = false
                    radio3.value = false
                })
                Text(tvEnSevilenRenk, modifier = modifier.padding(top = 15.dp))
            }
            if (radio2.value){
                TextField(tfEnSevilenRenk.value, onValueChange = {tfEnSevilenRenk.value = it})
                Button(onClick = {
                    kullaniciBilgileri.guvenlikSorusuKaydet(tvEnSevilenRenk)
                    kullaniciBilgileri.guvenlikSorusuCevapKaydet(tfEnSevilenRenk.value)
                    navController.navigate(Screens.KayitOlustur.name)
                }) {
                    Text("Onayla")
                }
            }
        }
        Spacer(modifier.height(20.dp))
        Column(modifier = modifier.background(Color.Cyan)) {
            Row() {
                RadioButton(selected = radio3.value, onClick = {
                    radio2.value = false
                    radio1.value = false
                })
                Text(tvEnSevilenHayvan, modifier = modifier.padding(top = 15.dp))
            }
            if (radio3.value){
                TextField(tfEnSevilenMuzikGrubu.value, onValueChange = {tfEnSevilenMuzikGrubu.value = it})
                Button(onClick = {
                    kullaniciBilgileri.guvenlikSorusuKaydet(tvEnSevilenHayvan)
                    kullaniciBilgileri.guvenlikSorusuCevapKaydet(tfEnSevilenHayvan.value)
                    navController.navigate(Screens.KayitOlustur.name)
                }) {
                    Text("Onayla")
                }
            }
        }
    }
}

