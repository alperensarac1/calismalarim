package com.example.yardimuygulamajetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yardimuygulamajetpack.entity.Session
import com.example.yardimuygulamajetpack.util.TimeUtils
import com.example.yardimuygulamajetpack.vm.HelperViewModel
import com.example.yardimuygulamajetpack.vm.UiState

@Composable
fun HelperAcceptedScreen(nav: NavController, vm: HelperViewModel = viewModel()) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val helperId = remember { Session.userId(ctx) }

    val state by vm.accepted.collectAsState()

    LaunchedEffect(Unit) { vm.startAcceptedPolling(helperId); vm.fetchAccepted(helperId) }
    DisposableEffect(Unit) { onDispose { vm.stopAcceptedPolling() } }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Kabul Edilen", style = MaterialTheme.typography.titleLarge)

        when (val s = state) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Data -> {
                val a = s.value
                if (a == null) {
                    Text("Aktif kabul yok (hasta onaylamış veya süre dolmuş olabilir).")
                    Button(onClick = { nav.popBackStack() }) { Text("Geri Dön") }
                } else {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Hasta: ${a.patient_name ?: "-"} (${a.patient_age ?: "-"})", style = MaterialTheme.typography.titleMedium)
                            Text("Telefon: ${a.patient_phone ?: "-"}")
                            Text("Servis: ${a.servis_adi ?: "-"}")
                            Text("Oda: ${a.oda_no ?: "-"}")
                            val rem = a.remaining_seconds ?: 0
                            Text("Kalan süre: ${TimeUtils.formatRemainingSeconds(rem)}")
                            Text("Not: 5 dk içinde arayıp hasta onaylatmalı.")
                        }
                    }
                }
            }
            is UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            else -> {}
        }
    }
}