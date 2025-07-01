package com.example.adisyonuygulamajetpack.view.anaekran


import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

@Composable
fun MasaOzetLayout(
    masaListesi: List<Masa>,
    viewModel: MasaDetayViewModel,
    onMasaDetayTikla: (Masa) -> Unit
) {
    val context = LocalContext.current
    var seciliMasa by remember { mutableStateOf<Masa?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Açık masa sayısı
        val acikMasaSayisi = masaListesi.count { it.acikMi == 1 }
        Text(
            text = "$acikMasaSayisi adet masa açık.",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Red,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        // Açık masalar listesi
        val acikMasalar = masaListesi.filter { it.acikMi == 1 }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(acikMasalar) { masa ->
                MasaOzetItem(
                    masa = masa,
                    onClick = { seciliMasa = masa }
                )
            }
        }
    }

    seciliMasa?.let { masa ->
        AlertDialog(
            onDismissRequest = { seciliMasa = null },
            title = {
                Text(
                    text = "Masa ${masa.id}",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onMasaDetayTikla(masa)
                            seciliMasa = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Ürün Ekle", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            viewModel.odemeAl {
                                Toast.makeText(context, "Ödeme alındı", Toast.LENGTH_SHORT).show()
                            }
                            seciliMasa = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Ödeme Al", color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
private fun MasaOzetItem(
    masa: Masa,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (masa.acikMi == 1) Color.Cyan else Color.LightGray
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Masa ${masa.id}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tutar: ${String.format("%.2f ₺", masa.toplam_fiyat)}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }
}
