package com.example.chatjetpackcompose.view

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.util.AppConfig
import com.example.chatkotlin.util.PrefManager
import kotlinx.coroutines.launch

@Composable
fun RegistrationDialog(
    onKayitBasarili: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ad by remember { mutableStateOf("") }
    var numara by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var hata by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {}, // kapatmayı engelle
        confirmButton = {
            TextButton(
                onClick = {
                    if (ad.isBlank() || numara.isBlank()) {
                        hata = "Boş alan bırakma!"
                        return@TextButton
                    }

                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.kullaniciKayit(ad, numara)
                            if (response.success && response.id != null) {
                                AppConfig.kullaniciId = response.id
                                PrefManager(context).kaydetKullaniciId(response.id)
                                Toast.makeText(context, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                                onKayitBasarili(response.id)
                            } else {
                                hata = response.error ?: "Kayıt başarısız"
                            }
                        } catch (e: Exception) {
                            hata = "Hata: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
            ) {
                Text("Kayıt Ol")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
        title = { Text("Kayıt Ol") },
        text = {
            Column {
                OutlinedTextField(
                    value = ad,
                    onValueChange = { ad = it },
                    label = { Text("Ad") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = numara,
                    onValueChange = { numara = it },
                    label = { Text("Numara") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                hata?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }//:Column
        }
    )
}
