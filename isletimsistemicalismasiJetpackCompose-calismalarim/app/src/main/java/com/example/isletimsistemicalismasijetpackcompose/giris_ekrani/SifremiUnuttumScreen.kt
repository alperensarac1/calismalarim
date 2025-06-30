package com.example.isletimsistemicalismasijetpackcompose.giris_ekrani

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.isletimsistemlericalismasikotlin.giris_ekrani.repository.KullaniciBilgileri

@Composable
fun SifremiUnuttumScreen(modifier: Modifier = Modifier,context: Context,navController: NavController){

    val kullaniciBilgileri = KullaniciBilgileri(context)
    val tfGuvenlikSorusuCevap = remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = kullaniciBilgileri.guvenlikSorusuGetir())
        TextField(tfGuvenlikSorusuCevap.value, onValueChange = {tfGuvenlikSorusuCevap.value = it})
        Button(onClick = {
            if (kullaniciBilgileri.guvenlikSorusuDogrula(tfGuvenlikSorusuCevap.value)){
                navController.navigate(Screens.KayitOlustur.name)
            }else{
                Toast.makeText(context,"Güvenlik sorusu cevabı eşleşmiyor!",Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Onayla")
        }
    }
}