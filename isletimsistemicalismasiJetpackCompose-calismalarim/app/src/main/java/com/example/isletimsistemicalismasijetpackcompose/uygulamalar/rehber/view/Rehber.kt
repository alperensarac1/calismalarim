package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.view

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.UygulamaGecisi
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity.RehberDao
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.entity.RehberVeritabaniYardimcisi
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.rehber.model.RehberKisiler
import com.google.gson.Gson

@Composable
fun RehberEkrani(navController: NavController, vt: RehberDao) {
    val context = LocalContext.current
    var kisiler by remember { mutableStateOf(vt.tumKisiler()) }
    val callLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Çağrı izni gerekli!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(UygulamaGecisi.toRehberEkle.name) }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            kisiler.forEach { kisi ->
                KisiItem(vt,kisi, context,navController, callLauncher) {
                    kisiler = vt.tumKisiler()
                }
            }
        }
    }
}

@Composable
fun KisiItem(vt: RehberDao,kisi: RehberKisiler, context: Context,navController: NavController, callLauncher: ManagedActivityResultLauncher<String, Boolean>, onRefresh: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(8.dp).clickable {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${kisi.kisi_numara}"))
            context.startActivity(callIntent)
        } else {
            callLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }) {
        BasicText(text = kisi.kisi_isim, modifier = Modifier.weight(1f))
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Düzenle")
        }
    }

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(onClick = {
            navController.navigateWithObject(UygulamaGecisi.toRehberDetay.name,kisi)
            showMenu = false
        }, text = { Text("Düzenle") })
        DropdownMenuItem(onClick = {
            // Silme işlemi
            vt.kisiSil(kisi.kisi_id)
            onRefresh()
            showMenu = false
        }, text = { Text("Sil") })
    }
}
fun NavController.navigateWithObject(route: String, obj: Any) {
    val json = Gson().toJson(obj)
    this.navigate("$route/$json")
}