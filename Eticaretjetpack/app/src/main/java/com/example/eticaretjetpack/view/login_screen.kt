package com.example.eticaretjetpack.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eticaretjetpack.repo.AuthVMFactory
import com.example.eticaretjetpack.viewmodel.AuthViewModel


@Composable
fun LoginScreen(
    onGoRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthVMFactory(ctx))
    val st by vm.state.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    LaunchedEffect(st.loggedIn) {
        if (st.loggedIn) onLoginSuccess()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Giriş", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
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
            onClick = { vm.login(email.trim(), pass) },
            enabled = !st.inFlight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (st.inFlight) "Giriş yapılıyor..." else "Giriş Yap")
        }

        TextButton(onClick = onGoRegister) {
            Text("Hesabın yok mu? Kayıt ol")
        }

        st.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
