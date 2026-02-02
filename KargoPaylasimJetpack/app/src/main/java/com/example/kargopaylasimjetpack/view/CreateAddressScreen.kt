package com.example.kargopaylasimjetpack.view


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kargopaylasimjetpack.model.AddressCreateReq
import com.example.kargopaylasimjetpack.util.UiState
import com.example.kargopaylasimjetpack.viewmodel.AddressCreateVM


@Composable
fun CreateAddressScreen(
    vm: AddressCreateVM,
    onDone: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var neigh by remember { mutableStateOf("") }
    var line by remember { mutableStateOf("") }
    var postal by remember { mutableStateOf("") }

    val st by vm.state.collectAsState()

    LaunchedEffect(st) {
        if (st is UiState.Success) onDone()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        if (st is UiState.Error) {
            Text((st as UiState.Error).message, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(title, { title = it }, label = { Text("Adres başlığı") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(city, { city = it }, label = { Text("Şehir") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(district, { district = it }, label = { Text("İlçe") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(neigh, { neigh = it }, label = { Text("Mahalle (ops.)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(line, { line = it }, label = { Text("Açık adres") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedTextField(postal, { postal = it }, label = { Text("Posta kodu (ops.)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                vm.save(
                    AddressCreateReq(
                        title = title.trim(),
                        city = city.trim(),
                        district = district.trim(),
                        neighborhood = neigh.trim(),
                        address_line = line.trim(),
                        postal_code = postal.trim()
                    )
                )
            },
            enabled = st !is UiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (st is UiState.Loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Kaydet")
        }
    }
}
