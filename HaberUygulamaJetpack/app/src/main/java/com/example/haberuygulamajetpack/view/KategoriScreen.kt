package com.example.haberuygulamajetpack.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haberuygulamajetpack.deo.HaberDao
import com.example.haberuygulamajetpack.model.HaberModel
import com.example.haberuygulamajetpack.servis.ApiClient
import com.example.haberuygulamajetpack.viewmodel.KategorilerViewModel

@Composable
fun KategoriScreen(
    kategoriId: Int,
    viewModel: KategorilerViewModel = remember { KategorilerViewModel(HaberDao(ApiClient.retrofit)) },
    onNavigateToDetay: (HaberModel) -> Unit
) {
    val haberListesi by viewModel.kategoriHaberleri.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(kategoriId) {
        viewModel.loadKategoriHaberleri(kategoriId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Kategori: $kategoriId", // Opsiyonel olarak kategori adını da çekebilirsin
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFC65555),
            modifier = Modifier.padding(start = 24.dp, top = 24.dp)
        )

        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            items(haberListesi) { haber ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onNavigateToDetay(haber) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = haber.baslik, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = haber.icerik.take(100) + "...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
