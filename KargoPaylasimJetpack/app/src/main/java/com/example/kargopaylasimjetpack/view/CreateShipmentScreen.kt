package com.example.kargopaylasimjetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kargopaylasimjetpack.util.UiState
import com.example.kargopaylasimjetpack.viewmodel.CreateShipmentVM

@Composable
fun CreateShipmentScreen(
    vm: CreateShipmentVM,
    onDone: () -> Unit
) {
    var phone by remember { mutableStateOf("") }

    val lookup by vm.lookup.collectAsState()
    val create by vm.create.collectAsState()

    var infoText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lookup) {
        when (lookup) {
            is UiState.Success -> {
                val d = (lookup as UiState.Success).data
                infoText = "Bulunan: ${d.masked_first_name} ${d.masked_last_name} • Onaylıyor musun?"
            }
            is UiState.Error -> infoText = (lookup as UiState.Error).message
            else -> {}
        }
    }

    LaunchedEffect(create) {
        if (create is UiState.Success) {
            val d = (create as UiState.Success).data
            // Basit dialog yerine direkt done da olabilir
            infoText = "Kod: ${d.pickup_code}\nSon geçerlilik: ${d.code_expires_at}"
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Alıcı telefon") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { vm.lookupReceiver(phone.trim()) },
                enabled = lookup !is UiState.Loading && create !is UiState.Loading
            ) { Text("Bul") }

            OutlinedButton(
                onClick = { vm.reset(); infoText = null },
                enabled = lookup !is UiState.Loading && create !is UiState.Loading
            ) { Text("İptal") }
        }

        infoText?.let { Text(it) }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { vm.createShipment() },
            enabled = lookup is UiState.Success && create !is UiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            when (create) {
                is UiState.Loading -> CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else -> Text("Onayla ve Oluştur")
            }
        }

        if (create is UiState.Success) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("Tamam")
            }
        }
    }
}
