package com.example.memesharejetpack.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memesharejetpack.viewmodel.RegisterViewModel

@Composable
fun RegisterScreen(
    onNavigateLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val registerResult by viewModel.registerResult.observeAsState()

    LaunchedEffect(registerResult) {
        registerResult?.let { res ->
            if (res.success) {
                Toast.makeText(context, "Kayıt başarılı! Giriş yapabilirsiniz", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            } else if (res.message.isNotBlank()) {
                Toast.makeText(context, "Hata: ${res.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Kayıt Ol", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Kullanıcı adı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank()) {
                        viewModel.registerUser(username, password)
                    } else {
                        Toast.makeText(context, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Kayıt Ol") }

            TextButton(onClick = onNavigateLogin) {
                Text("Zaten hesabın var mı? Giriş yap")
            }
        }
    }
}
