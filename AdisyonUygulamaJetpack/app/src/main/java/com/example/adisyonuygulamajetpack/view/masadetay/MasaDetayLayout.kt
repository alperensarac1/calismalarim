package com.example.adisyonuygulamajetpack.view.masadetay

// build.gradle (app) içinde asgari olarak aşağıdaki dependency’lerin ekli olduğundan emin olun:
//
// implementation "androidx.compose.foundation:foundation:1.4.3"
// implementation "androidx.compose.material3:material3:1.1.0"
// implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1"
// implementation "io.coil-kt:coil-compose:2.2.2"
// implementation "androidx.activity:activity-compose:1.8.0"
//

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.adisyonuygulamajetpack.viewmodel.UrunViewModel
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel


@Composable
fun MasaDetayScreen(
    masaId: Int,
    masaDetailVM: MasaDetayViewModel = viewModel(
        factory = MasaDetayViewModel.provideFactory(masaId)
    ),
    urunVM: UrunViewModel = viewModel()
) {
    val context = LocalContext.current


    val masaState by masaDetailVM.masa.observeAsState(initial = null)

    // Geri kalan LiveData’lar için de aynı şekilde:
    val masaUrunler by masaDetailVM.urunler.observeAsState(emptyList())
    val toplamFiyat by masaDetailVM.toplamFiyat.observeAsState(0f)
    val tumUrunler by masaDetailVM.tumUrunler.observeAsState(emptyList())
    val kategoriler by masaDetailVM.kategoriler.observeAsState(emptyList())

    // 2) Seçili kategori index’i, filtreli liste vb.
    var seciliKategoriIndex by remember { mutableStateOf(0) }
    val filtreliUrunler: List<Urun> = remember(tumUrunler, seciliKategoriIndex, kategoriler) {
        if (seciliKategoriIndex == 0) {
            tumUrunler
        } else {
            val secilenKat = kategoriler.getOrNull(seciliKategoriIndex - 1)
            tumUrunler.filter { it.urunKategori.id == secilenKat?.id }
        }
    }

    // 3) İlk yükleme istekleri
    LaunchedEffect(Unit) {
        masaDetailVM.yukleTumVeriler()
        urunVM.urunleriYukle()
        urunVM.kategorileriYukle()
    }

    // 4) Root Column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFBABA))
    ) {
        // === HEADER: 60dp ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable { /* Bir önceki ekrana dönüş */ }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = masaState?.let { "Masa ${it.id}" } ?: "Masa",
                fontSize = 20.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // === BODY ===
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // --- SOLDAN: Masa Adisyonu (200dp) ---
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
                            items(masaUrunler) { mu: MasaUrun ->
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
                        text = "Toplam: ${String.format("%.2f", toplamFiyat)} TL",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            masaDetailVM.odemeAl {
                                Toast
                                    .makeText(context, "Ödeme tamamlandı", Toast.LENGTH_SHORT)
                                    .show()
                                masaDetailVM.yukleTumVeriler()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Ödeme Al")
                    }
                }
            }

            // --- SAĞDAN: Ürünler (weight=1) ---
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                // 1) Kategori Seçimi
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        Text(
                            text = "Tümü",
                            fontSize = 16.sp,
                            color = if (seciliKategoriIndex == 0) Color.Red else Color.Black,
                            modifier = Modifier
                                .clickable { seciliKategoriIndex = 0 }
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    items(kategoriler) { kat: Kategori ->
                        val idx = kategoriler.indexOf(kat) + 1
                        Text(
                            text = kat.kategori_ad,
                            fontSize = 16.sp,
                            color = if (seciliKategoriIndex == idx) Color.Red else Color.Black,
                            modifier = Modifier
                                .clickable { seciliKategoriIndex = idx }
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2) Filtrelenmiş Ürün Izgarası veya “Boş” Mesajı
                if (filtreliUrunler.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bu kategoride ürün bulunamadı.",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtreliUrunler) { urun: Urun ->
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    // Ürün resmi
                                    AsyncImage(
                                        model = urun.urun_resim,
                                        contentDescription = urun.urun_ad,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Ürün adı
                                    Text(
                                        text = urun.urun_ad,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Ürün fiyatı
                                    Text(
                                        text = "${String.format("%.2f", urun.urun_fiyat)} TL",
                                        fontSize = 14.sp,
                                        color = Color.DarkGray
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // + / adet / – butonları
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Button(
                                            onClick = {
                                                masaDetailVM.urunEkle(urun.id)
                                                masaDetailVM.yukleTumVeriler()
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

                                        Button(
                                            onClick = {
                                                if (urun.urun_adet > 0) {
                                                    masaDetailVM.urunCikar(urun.id)
                                                    masaDetailVM.yukleTumVeriler()
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

// İhtiyaç duyarsanız bu sabit yüksekliği bir değere atayabilirsiniz.
private val FiftySixDp = 56.dp
