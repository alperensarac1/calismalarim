package com.example.isletimsistemicalismasijetpackcompose.giris_ekrani

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isletimsistemicalismasijetpackcompose.R
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.components.EtSifre
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri


@Composable
fun KayitOlusturScreen(modifier: Modifier = Modifier,context: Context,navController: NavController){

    val kullaniciBilgileri = KullaniciBilgileri(context)
    val tf1 = remember { mutableStateOf("") }
    val tf2 = remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

        Image(painter = painterResource(R.drawable.adios), contentDescription = "",modifier = modifier.scale(4f))
        Spacer(modifier.height(100.dp))
    EtSifre(tf = tf1, modifier = modifier)
        Spacer(modifier.height(50.dp))
    EtSifre(tf = tf2, modifier = modifier)
        Spacer(modifier.height(100.dp))
        Button(onClick = {
            if (tf1.value.isNotEmpty() || tf1.value.isNotEmpty()){
                if (tf1.value.equals(tf2.value)){
                    kullaniciBilgileri.sifreKaydi(tf1.value)
                    navController.navigate(Screens.GirisYap.name)
                }
            }
        }) {
            Text("Kaydet")
        }


    }
}