package com.example.onlinetaksijetpack.ui.auth


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinetaksijetpack.data.local.SessionManager
import com.example.onlinetaksijetpack.data.remote.api.ApiClient
import com.example.onlinetaksijetpack.data.repository.AuthRepository

@Composable
fun LoginScreen(
    onGoRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current

    val viewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            authRepository = AuthRepository(ApiClient.create(context)),
            sessionManager = SessionManager(context)
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    val phoneState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

    LaunchedEffect(uiState.successRole) {
        uiState.successRole?.let { role ->
            onLoginSuccess(role)
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            if (!uiState.isLoading) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "onlinetaksi Giriş",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phoneState.value,
            onValueChange = { phoneState.value = it },
            label = { Text("Telefon") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (phoneState.value.isBlank() || passwordState.value.isBlank()) {
                    Toast.makeText(context, "Telefon ve şifre zorunlu", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.login(
                        phone = phoneState.value.trim(),
                        password = passwordState.value.trim()
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Giriş Yap")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onGoRegister,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Hesabın yok mu? Kayıt ol")
        }
    }
}