package com.example.chatjetpackcompose.view

import android.widget.Toast
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.chatjetpackcompose.adapter.KonusulanKisiRow
import com.example.chatjetpackcompose.viewmodel.SohbetListesiViewModel
import com.example.chatkotlin.service.RetrofitClient
import com.example.chatkotlin.util.AppConfig
import kotlinx.coroutines.launch

@Composable
fun MesajlarScreen(
    viewModel: SohbetListesiViewModel = viewModel(),
    navController: NavController
) {
    val kisiler by viewModel.konusulanKisiler.collectAsState()
    val hata by viewModel.hataMesaji.collectAsState()
    var showYeniKisi by remember { mutableStateOf(false) }
    var secilenNumara by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.sohbetListesiniBaslat(AppConfig.kullaniciId)
    }

    LaunchedEffect(secilenNumara) {
        secilenNumara?.let { numara ->
            try {
                val resp = RetrofitClient.apiService.kullanicilariGetir()
                if (resp.success) {
                    val kisi = resp.kullanicilar.find { it.numara == numara }
                    if (kisi != null) {
                        navController.navigate("singleChat/${kisi.id}/${kisi.ad}")
                    } else {
                        Toast.makeText(context, "Bu numara kayıtlı değil", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Sunucu hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                secilenNumara = null // tekrar tetiklenmesini engelle
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showYeniKisi = true }) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Mesaj")
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(kisiler, key = { it.id }) { kisi ->
                KonusulanKisiRow(kisi) {
                    Toast.makeText(context, "${it.ad} seçildi", Toast.LENGTH_SHORT).show()
                    secilenNumara = it.numara
                }
            }
        }
    }

    if (hata != null) {
        LaunchedEffect(hata) {
            snackbarHostState.showSnackbar(hata!!)
        }
    }

    if (showYeniKisi) {
        YeniKisiDialog(
            onConfirm = { numara ->
                showYeniKisi = false
                secilenNumara = numara
            },
            onDismiss = { showYeniKisi = false }
        )
    }
}

@Composable
fun YeniKisiDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var numara by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (numara.isNotBlank()) onConfirm(numara)
                else Toast.makeText(
                    context,
                    "Numara girilmedi",
                    Toast.LENGTH_SHORT
                ).show()
            }) { Text("Gönder") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        },
        title = { Text("Yeni Mesaj") },
        text = {
            OutlinedTextField(
                value = numara,
                onValueChange = { numara = it },
                label = { Text("Alıcı numarası") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
        }
    )
}



