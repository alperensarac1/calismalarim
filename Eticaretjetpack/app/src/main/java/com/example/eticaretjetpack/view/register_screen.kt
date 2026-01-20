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
fun RegisterScreen(
    onGoLogin: () -> Unit
) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(factory = AuthVMFactory(ctx))
    val st by vm.state.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    LaunchedEffect(st.registered) {
        if (st.registered) {
            vm.clearRegistered()
            onGoLogin()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Kayıt Ol", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(name, { name = it }, label = { Text("Ad Soyad") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("E-posta") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            pass, { pass = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { vm.register(name.trim(), email.trim(), pass) },
            enabled = !st.inFlight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (st.inFlight) "Kaydediliyor..." else "Kayıt Ol")
        }

        TextButton(onClick = onGoLogin) { Text("Zaten hesabım var") }

        st.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
