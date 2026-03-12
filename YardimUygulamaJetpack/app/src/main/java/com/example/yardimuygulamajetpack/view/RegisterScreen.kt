package com.example.yardimuygulamajetpack.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.yardimuygulamajetpack.entity.Session
import com.example.yardimuygulamajetpack.helper.LocationHelper
import com.example.yardimuygulamajetpack.model.RegisterBody
import com.example.yardimuygulamajetpack.navigation.Route
import com.example.yardimuygulamajetpack.service.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var ad by remember { mutableStateOf("") }
    var soyad by remember { mutableStateOf("") }
    var yas by remember { mutableStateOf("") }
    var telefon by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("HASTA") }

    var detected by remember { mutableStateOf("Konum: tespit edilmedi") }
    var city by remember { mutableStateOf<String?>(null) }
    var district by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(false) }
    var info by remember { mutableStateOf("") }

    fun detect() {
        scope.launch {
            detected = "Tespit ediliyor..."
            val ll = LocationHelper.getCurrentLatLng(ctx)
            if (ll == null) {
                detected = "Konum alınamadı (GPS açık mı?)"
                return@launch
            }
            val (c, d) = LocationHelper.reverseCityDistrict(ctx, ll.first, ll.second)
            city = c; district = d
            detected = if (!c.isNullOrBlank() && !d.isNullOrBlank()) "Tespit edilen: $c / $d"
            else "Şehir/ilçe tespit edilemedi"
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val ok = (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (ok) detect() else detected = "Konum izni verilmedi."
    }

    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) detect()
        else permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    fun doRegister() {
        info = ""
        if (ad.isBlank() || soyad.isBlank() || telefon.isBlank() || sifre.isBlank()) {
            info = "Ad, soyad, telefon, şifre zorunlu"
            return
        }
        val c = city; val d = district
        if (c.isNullOrBlank() || d.isNullOrBlank()) {
            info = "Şehir/ilçe tespit edilemedi."
            return
        }
        val yasInt = yas.trim().toIntOrNull()

        loading = true
        scope.launch {
            val res = runCatching {
                ApiClient.api.register(
                    RegisterBody(role, ad, soyad, yasInt, telefon, c, d, sifre)
                )
            }.getOrNull()

            loading = false
            if (res?.ok == true && res.user != null) {
                Session.save(ctx, res.user.id, res.user.role)
                if (res.user.role == "YARDIMCI") {
                    nav.navigate(Route.HelperOpen.r) { popUpTo(Route.Register.r) { inclusive = true } }
                } else {
                    nav.navigate(Route.Patient.r) { popUpTo(Route.Register.r) { inclusive = true } }
                }
            } else {
                info = res?.error ?: "Kayıt başarısız"
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Kayıt", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(selected = role == "HASTA", onClick = { role = "HASTA" }, label = { Text("Hasta") })
            FilterChip(selected = role == "YARDIMCI", onClick = { role = "YARDIMCI" }, label = { Text("Yardımcı") })
        }

        OutlinedTextField(ad, { ad = it }, label = { Text("Ad") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(soyad, { soyad = it }, label = { Text("Soyad") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(yas, { yas = it }, label = { Text("Yaş") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(telefon, { telefon = it }, label = { Text("Telefon") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(sifre, { sifre = it }, label = { Text("Şifre") }, modifier = Modifier.fillMaxWidth())

        Text(detected)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }) { Text("Konumdan Al") }

            Button(onClick = { doRegister() }, enabled = !loading) { Text(if (loading) "..." else "Kayıt Ol") }
        }

        if (info.isNotBlank()) Text(info, color = MaterialTheme.colorScheme.error)

        TextButton(onClick = { nav.navigate(Route.Login.r) }) { Text("Girişe dön") }
    }
}