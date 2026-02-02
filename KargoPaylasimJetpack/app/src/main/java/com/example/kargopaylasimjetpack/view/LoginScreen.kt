package com.example.kargopaylasimjetpack.view


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.kargopaylasimjetpack.util.UiState
import com.example.kargopaylasimjetpack.viewmodel.AuthVM

@Composable
fun LoginScreen(
    vm: AuthVM,
    onGoRegister: () -> Unit,
    onLoggedIn: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val st by vm.loginState.collectAsState()

    LaunchedEffect(st) {
        if (st is UiState.Success) onLoggedIn()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        if (st is UiState.Error) {
            Text((st as UiState.Error).message, color = MaterialTheme.colorScheme.error)
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefon") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.login(phone.trim(), pass) },
            enabled = st !is UiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (st is UiState.Loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Giriş Yap")
        }

        TextButton(onClick = onGoRegister) {
            Text("Kayıt Ol")
        }
    }
}
