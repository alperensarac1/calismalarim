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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yardimuygulamajetpack.entity.Session
import com.example.yardimuygulamajetpack.helper.LocationHelper
import com.example.yardimuygulamajetpack.util.TimeUtils
import com.example.yardimuygulamajetpack.vm.PatientViewModel
import com.example.yardimuygulamajetpack.vm.UiState
import kotlinx.coroutines.launch

@Composable
fun PatientScreen(nav: NavController, vm: PatientViewModel = viewModel()) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val patientId = remember { Session.userId(ctx) }
    var statusText by remember { mutableStateOf("") }

    var servis by remember { mutableStateOf("") }
    var oda by remember { mutableStateOf("") }

    var latLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locText by remember { mutableStateOf("Konum: alınmadı") }

    val activeState by vm.active.collectAsState()

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val ok = (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (ok) {
            scope.launch {
                val ll = LocationHelper.getCurrentLatLng(ctx)
                latLng = ll
                locText = if (ll != null) "Konum: ${ll.first}, ${ll.second}" else "Konum alınamadı"
            }
        } else {
            locText = "Konum izni verilmedi"
        }
    }

    fun ensureLocation(onReady: () -> Unit) {
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) {
            scope.launch {
                val ll = LocationHelper.getCurrentLatLng(ctx)
                latLng = ll
                locText = if (ll != null) "Konum: ${ll.first}, ${ll.second}" else "Konum alınamadı"
                if (ll != null) onReady()
            }
        } else {
            permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    LaunchedEffect(Unit) {
        vm.startPolling(patientId)
        vm.fetchActive(patientId)
        // ekran açılınca bir kez konum almayı dene
        ensureLocation { }
    }

    DisposableEffect(Unit) {
        onDispose { vm.stopPolling() }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Hasta", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = {
                Session.clear(ctx)
                nav.navigate(Route.Login.r) { popUpTo(0) }
            }) { Text("Çıkış") }
        }

        Text(locText)
        OutlinedButton(onClick = { ensureLocation { } }) { Text("Konumu Yenile") }

        Divider()

        OutlinedTextField(servis, { servis = it }, label = { Text("Servis") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(oda, { oda = it }, label = { Text("Oda No") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                ensureLocation {
                    val ll = latLng ?: return@ensureLocation
                    if (servis.isBlank() || oda.isBlank()) {
                        statusText = "Servis ve oda zorunlu"
                        return@ensureLocation
                    }
                    vm.createHelp(patientId, servis, oda, ll.first, ll.second) { msg -> statusText = msg }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Yardım İste") }

        if (statusText.isNotBlank()) {
            Text(statusText)
        }

        Divider()

        when (val s = activeState) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Data -> {
                val a = s.value
                if (a == null) {
                    Text("Durum: Aktif istek yok")
                } else {
                    val rem = a.remaining_seconds ?: 0
                    val extra = if (a.status == "ACCEPTED") " (Kalan: ${TimeUtils.formatRemainingSeconds(rem)})" else ""
                    Text("Durum: ${a.status}$extra")

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (a.status == "ACCEPTED") {
                            Button(onClick = {
                                vm.confirm(a.id, patientId) { msg -> statusText = msg }
                            }) { Text("Onayla") }
                        }
                        if (a.status == "OPEN" || a.status == "ACCEPTED") {
                            OutlinedButton(onClick = {
                                vm.cancel(a.id, patientId) { msg -> statusText = msg }
                            }) { Text("İptal") }
                        }
                    }
                }
            }
            is UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}