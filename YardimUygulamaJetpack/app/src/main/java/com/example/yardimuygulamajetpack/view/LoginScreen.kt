package com.example.yardimuygulamajetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yardimuygulamajetpack.navigation.Route
import com.example.yardimuygulamajetpack.vm.AuthViewModel
import com.example.yardimuygulamajetpack.vm.UiState

@Composable
fun LoginScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    val ctx = LocalContext.current
    val state by vm.authState.collectAsState()

    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Giriş", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(phone, { phone = it }, label = { Text("Telefon") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(pass, { pass = it }, label = { Text("Şifre") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                vm.login(ctx, phone, pass) { role ->
                    val dest = if (role == "YARDIMCI") Route.HelperOpen.r else Route.Patient.r
                    nav.navigate(dest) { popUpTo(Route.Login.r) { inclusive = true } }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is UiState.Loading
        ) {
            Text(if (state is UiState.Loading) "..." else "Giriş Yap")
        }

        if (state is UiState.Error) {
            Text((state as UiState.Error).message, color = MaterialTheme.colorScheme.error)
        }

        TextButton(onClick = { nav.navigate(Route.Register.r) }) {
            Text("Kayıt ol")
        }
    }
}