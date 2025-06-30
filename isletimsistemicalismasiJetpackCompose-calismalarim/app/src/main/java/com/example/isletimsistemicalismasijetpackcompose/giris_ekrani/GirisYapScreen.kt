package com.example.isletimsistemicalismasijetpackcompose.giris_ekrani

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.packInts
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.example.isletimsistemicalismasijetpackcompose.R
import com.example.isletimsistemicalismasijetpackcompose.anaekran.UygulamaEkraniScreen
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.components.EtSifre
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.UygulamaEkraniActivity
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri

@Composable
fun GirisYapScreen(modifier: Modifier = Modifier,navController: NavController){

    val context = LocalContext.current
    val etSifre = remember { mutableStateOf("") }
    val kullaniciBilgileri = KullaniciBilgileri(context)

    LaunchedEffect(true) {
        if(!kullaniciBilgileri.sifreKaydiOlusturulmusMu()){
            navController.navigate(Screens.KayitOlustur.name)
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painter = painterResource(R.drawable.adios), contentDescription = "",modifier = modifier.scale(4f))
        Spacer(Modifier.height(100.dp))
        EtSifre(modifier = modifier,tf = etSifre)
        Spacer(Modifier.height(100.dp))
        Button(onClick = {
            if (kullaniciBilgileri.sifreSorgulama(etSifre.value)){

                var intent = Intent(context, UygulamaEkraniActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        }) {
            Text("Giriş Yap")
        }
        Spacer(modifier = Modifier.height(100.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = modifier.width(250.dp))
            Text("Şifremi Unuttum!", style = TextStyle(color = Color.Red), modifier = modifier.clickable {
                navController.navigate(Screens.SifremiUnuttum.name)
            })

        }
    }
}