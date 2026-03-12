package com.example.yardimuygulamajetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yardimuygulamajetpack.entity.Session
import com.example.yardimuygulamajetpack.navigation.Route
import com.example.yardimuygulamajetpack.vm.HelperViewModel
import com.example.yardimuygulamajetpack.vm.UiState

@Composable
fun HelperOpenScreen(nav: NavController, vm: HelperViewModel = viewModel()) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val helperId = remember { Session.userId(ctx) }

    val state by vm.open.collectAsState()
    var info by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.startOpenPolling(helperId); vm.fetchOpen(helperId) }
    DisposableEffect(Unit) { onDispose { vm.stopOpenPolling() } }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("İlçemde Yardım İsteyenler", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = {
                Session.clear(ctx)
                nav.navigate(Route.Login.r) { popUpTo(0) }
            }) { Text("Çıkış") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { nav.navigate(Route.HelperAccepted.r) }) { Text("Kabul") }
            OutlinedButton(onClick = { nav.navigate(Route.HelperHistory.r) }) { Text("Geçmiş") }
        }

        if (info.isNotBlank()) Text(info, color = MaterialTheme.colorScheme.error)

        when (val s = state) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is UiState.Data -> {
                val items = s.value
                Text("Bulunan: ${items.size}")

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { it ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(it.patient_name ?: "-", style = MaterialTheme.typography.titleMedium)
                                Text("Yaş: ${it.patient_age ?: "-"}")
                                Text("İstek: ${it.created_at ?: "-"}")

                                Button(onClick = {
                                    vm.accept(it.id, helperId) { msg ->
                                        info = msg
                                        if (msg == "Kabul edildi") nav.navigate(Route.HelperAccepted.r)
                                    }
                                }) { Text("Kabul Et") }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}