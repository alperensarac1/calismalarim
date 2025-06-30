package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.view

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity.RehberDao
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.model.RehberKisiler
import com.google.gson.Gson

@Composable
fun RehberDetay(modifier: Modifier = Modifier,kisiJson:String, vt:RehberDao,navController: NavController) {
    val kisi = Gson().fromJson(kisiJson, RehberKisiler::class.java)
    var ad by remember { mutableStateOf(TextFieldValue(kisi.kisi_isim)) }
    var numara by remember { mutableStateOf(TextFieldValue(kisi.kisi_numara)) }
    val context = LocalContext.current

    Column(modifier = modifier.padding(16.dp)) {
        OutlinedTextField(
            value = ad,
            onValueChange = { ad = it },
            label = { Text("Ad") },
            modifier = modifier.fillMaxWidth()
        )

        Spacer(modifier = modifier.height(8.dp))

        OutlinedTextField(
            value = numara,
            onValueChange = { numara = it },
            label = { Text("Numara") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button (onClick = {
            val guncellenmisKisi = kisi.copy(kisi_isim = ad.text, kisi_numara = numara.text)
            vt.kisiGuncelle(kisi.kisi_id,guncellenmisKisi.kisi_isim,guncellenmisKisi.kisi_numara)
            Toast.makeText(context, "Kişi Güncellendi", Toast.LENGTH_SHORT).show()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Kaydet")
        }
    }
}
