package com.example.adisyonuygulamajetpack.view.masadetay

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.adisyonuygulamajetpack.viewmodel.UrunViewModel
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

@Composable
fun MasaDetayScreen(
    masaId: Int,
    masaDetayViewModel: MasaDetayViewModel,
    urunViewModel: UrunViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // 1) LiveData → State dönüşümleri
    val masaState by masaDetayViewModel.masa.observeAsState(initial = null)
    val masaUrunler by masaDetayViewModel.urunler.observeAsState(initial = emptyList())
    val toplamFiyat by masaDetayViewModel.toplamFiyat.observeAsState(initial = 0f)
    val tumUrunler by masaDetayViewModel.tumUrunler.observeAsState(initial = emptyList())
    val kategoriler by masaDetayViewModel.kategoriler.observeAsState(initial = emptyList())
    val odemeTamamlandi by masaDetayViewModel.odemeTamamlandi.observeAsState(initial = false)

    // 2) İlk yükleme tetikleniyor
    LaunchedEffect(Unit) {
        masaDetayViewModel.yukleTumVeriler()
        urunViewModel.urunleriYukle()
        urunViewModel.kategorileriYukle()
    }

    // 3) “odemeTamamlandi” true olduğunda ana ekrana dön
    LaunchedEffect(odemeTamamlandi) {
        if (odemeTamamlandi) {
            masaDetayViewModel.odemeTamamlandi.value = false
            onNavigateBack()
        }
    }

    // 4) Kategori filtresi
    var seciliKategoriIndex by remember { mutableStateOf(0) }
    val filtreliUrunler: List<Urun> = remember(tumUrunler, seciliKategoriIndex, kategoriler) {
        if (seciliKategoriIndex == 0) tumUrunler
        else {
            val secKat = kategoriler.getOrNull(seciliKategoriIndex - 1)
            tumUrunler.filter { it.urunKategori.id == secKat?.id }
        }
    }

    // 5) Resim seçici launcher
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                selectedImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                Toast.makeText(context, "Resim seçildi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 6) Ekranın tamamı
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFBABA))
    ) {
        // === Başlık (Header) ===
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                modifier = Modifier
                    .clickable { onNavigateBack() }
                    .padding(end = 16.dp)
            )
            Text(
                text = masaState?.let { "Masa ${it.id}" } ?: "Masa",
                fontSize = 20.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // === İçerik Satırı ===
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // — Soldan: Adisyon (200dp)
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Masa Ürünleri",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (masaUrunler.isEmpty()) {
                        Text(
                            text = "Henüz ürün eklenmemiş.",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(masaUrunler) { mu ->
                                Text(
                                    text = "${mu.urun_ad}  (adet: ${mu.adet})",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Toplam: ${"%.2f".format(toplamFiyat)} TL",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            masaDetayViewModel.odemeAl {
                                Toast
                                    .makeText(context, "Ödeme alındı", Toast.LENGTH_SHORT)
                                    .show()
                                masaDetayViewModel.yukleTumVeriler()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Ödeme Al")
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // — Sağdan: Ürünler ve Kategoriler —
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                // ● Kategori Satırı
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        Text(
                            text = "Tümü",
                            fontSize = 16.sp,
                            color = if (seciliKategoriIndex == 0) Color.Red else Color.Black,
                            modifier = Modifier
                                .clickable { seciliKategoriIndex = 0 }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    items(kategoriler) { kat ->
                        val idx = kategoriler.indexOf(kat) + 1
                        Text(
                            text = kat.kategori_ad,
                            fontSize = 16.sp,
                            color = if (seciliKategoriIndex == idx) Color.Red else Color.Black,
                            modifier = Modifier
                                .clickable { seciliKategoriIndex = idx }
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ● Filtrelenmiş Ürün Izgarası veya Boş Mesaj
                if (filtreliUrunler.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bu kategoride ürün yok.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtreliUrunler) { urun ->
                            Card(
                                elevation = CardDefaults.cardElevation(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    AsyncImage(
                                        model = urun.urun_resim,
                                        contentDescription = urun.urun_ad,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = urun.urun_ad,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${"%.2f".format(urun.urun_fiyat)} TL",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // “+” Butonu
                                        Button(
                                            onClick = {
                                                masaDetayViewModel.urunEkle(urun.id)
                                                masaDetayViewModel.yukleTumVeriler()
                                            },
                                            modifier = Modifier.size(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = "+")
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = "${urun.urun_adet}",
                                            fontSize = 16.sp,
                                            modifier = Modifier.width(24.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // “−” Butonu
                                        Button(
                                            onClick = {
                                                if (urun.urun_adet > 0) {
                                                    masaDetayViewModel.urunCikar(urun.id)
                                                    masaDetayViewModel.yukleTumVeriler()
                                                }
                                            },
                                            modifier = Modifier.size(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = "−")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
