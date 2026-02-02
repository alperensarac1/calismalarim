package com.example.kargopaylasimjetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kargopaylasimjetpack.model.Address
import com.example.kargopaylasimjetpack.model.Shipment
import com.example.kargopaylasimjetpack.viewmodel.HomeVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: HomeVM,
    onGoCreateShipment: () -> Unit,
    onGoCreateAddress: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    TextButton(onClick = onGoCreateAddress) { Text("+ Adres") }
                    TextButton(onClick = onGoCreateShipment) { Text("+ Yeni") }
                }
            )
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ui.error?.let { err ->
                    item { Text(err, color = MaterialTheme.colorScheme.error) }
                }

                item { Text("Gönderiler", style = MaterialTheme.typography.titleMedium) }
                items(ui.shipments) { s -> ShipmentCard(s) }

                item { Spacer(Modifier.height(8.dp)) }
                item { Text("Adresler", style = MaterialTheme.typography.titleMedium) }
                items(ui.addresses) { a ->
                    AddressCard(
                        a = a,
                        onSetDefault = { vm.setDefaultAddress(a.id) },
                        onDelete = { vm.deleteAddress(a.id) }
                    )
                }
            }

            if (ui.loading) {
                CircularProgressIndicator(Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ShipmentCard(s: Shipment) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("ID: #${s.id} • ${s.status}", style = MaterialTheme.typography.titleSmall)
            Text("Kod: ${s.pickup_code}", style = MaterialTheme.typography.bodyMedium)
            if (!s.cargo_company_name.isNullOrBlank()) {
                Text("Kargo: ${s.cargo_company_name}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AddressCard(
    a: Address,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(a.title, style = MaterialTheme.typography.titleSmall)
                if (a.is_default == 1) AssistChip(onClick = {}, label = { Text("Varsayılan") })
            }
            Text("${a.district} / ${a.city}", style = MaterialTheme.typography.bodyMedium)
            Text(a.address_line, style = MaterialTheme.typography.bodySmall, maxLines = 2)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (a.is_default != 1) {
                    OutlinedButton(onClick = onSetDefault) { Text("Varsayılan") }
                }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = onDelete
                ) { Text("Sil") }
            }
        }
    }
}
