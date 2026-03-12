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
import com.example.yardimuygulamajetpack.vm.HelperViewModel
import com.example.yardimuygulamajetpack.vm.UiState

@Composable
fun HelperHistoryScreen(nav: NavController, vm: HelperViewModel = viewModel()) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val helperId = remember { Session.userId(ctx) }

    val state by vm.history.collectAsState()

    LaunchedEffect(Unit) { vm.fetchHistory(helperId) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Geçmiş", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { nav.popBackStack() }) { Text("Geri") }
        }

        when (val s = state) {
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> Text(s.message, color = MaterialTheme.colorScheme.error)
            is UiState.Data -> {
                val items = s.value
                Text("Toplam: ${items.size}")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { it ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(it.patient_name ?: "-", style = MaterialTheme.typography.titleMedium)
                                Text("Telefon: ${it.patient_phone ?: "-"}")
                                Text("Servis: ${it.servis_adi ?: "-"}")
                                Text("Oda: ${it.oda_no ?: "-"}")
                                Text("Onay: ${it.confirmed_at ?: "-"}")
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}