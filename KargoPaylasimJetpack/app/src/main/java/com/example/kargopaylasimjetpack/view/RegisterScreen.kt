package com.example.kargopaylasimjetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kargopaylasimjetpack.model.RegisterReq
import com.example.kargopaylasimjetpack.util.UiState
import com.example.kargopaylasimjetpack.viewmodel.AuthVM

@Composable
fun RegisterScreen(
    vm: AuthVM,
    onBack: () -> Unit
) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var tc by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    var addrTitle by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var neigh by remember { mutableStateOf("") }
    var addrLine by remember { mutableStateOf("") }
    var postal by remember { mutableStateOf("") }

    val st by vm.registerState.collectAsState()

    LaunchedEffect(st) {
        if (st is UiState.Success) onBack()
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (st is UiState.Error) {
            Text((st as UiState.Error).message, color = MaterialTheme.colorScheme.error)
        }

        Text("Kişisel Bilgiler", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(first, { first = it }, label = { Text("İsim") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(last, { last = it }, label = { Text("Soyisim") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, label = { Text("Telefon") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(tc, { tc = it }, label = { Text("TC (11)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            pass, { pass = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(6.dp))
        Text("Adres Bilgileri", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(addrTitle, { addrTitle = it }, label = { Text("Adres başlığı") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(city, { city = it }, label = { Text("Şehir") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(district, { district = it }, label = { Text("İlçe") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(neigh, { neigh = it }, label = { Text("Mahalle (ops.)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(addrLine, { addrLine = it }, label = { Text("Açık adres") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        OutlinedTextField(postal, { postal = it }, label = { Text("Posta kodu (ops.)") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                vm.register(
                    RegisterReq(
                        phone = phone.trim(),
                        first_name = first.trim(),
                        last_name = last.trim(),
                        tc_no = tc.trim(),
                        password = pass,

                        address_title = addrTitle.trim(),
                        city = city.trim(),
                        district = district.trim(),
                        neighborhood = neigh.trim(),
                        address_line = addrLine.trim(),
                        postal_code = postal.trim()
                    )
                )
            },
            enabled = st !is UiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (st is UiState.Loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Kayıt Ol")
        }

        TextButton(onClick = onBack) { Text("Geri") }
    }
}
