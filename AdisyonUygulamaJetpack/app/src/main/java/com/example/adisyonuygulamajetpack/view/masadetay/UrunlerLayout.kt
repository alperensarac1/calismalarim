package com.example.adisyonuygulamajetpack.view.masadetay

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UrunlerScreen(
    // Host Activity veya FragmentActivity üzerinden sağlanan ViewModel
    masaDetayViewModel: MasaDetayViewModel = viewModel(
        factory = MasaDetayViewModel.provideFactory(masaId = /* kullanacağınız masaId */ 1)
    )
) {
    // 1) LiveData’ları Compose’un State’ine dönüştür
    val tumUrunler by masaDetayViewModel.tumUrunler.observeAsState(initial = emptyList())
    val kategoriler by masaDetayViewModel.kategoriler.observeAsState(initial = emptyList())

    // 2) Filtreleme için yerel state (0 = “Tümü” seçili)
    var seciliKategoriIndex by remember { mutableStateOf(0) }

    // 3) Her kategori değiştiğinde veya tüm ürünler değiştiğinde grid’i güncelle
    val guncelUrunListesi: List<Urun> = remember(tumUrunler, seciliKategoriIndex) {
        if (seciliKategoriIndex == 0) {
            tumUrunler
        } else {
            val kategori = kategoriler.getOrNull(seciliKategoriIndex - 1)
            tumUrunler.filter { it.urunKategori.id == kategori?.id }
        }
    }

    // 4) Bileşeni çiz
    Row(modifier = Modifier.fillMaxSize()) {
        // --- Sol taraf: Kategori Listesi ---
        LazyColumn(
            modifier = Modifier
                .width(150.dp)
                .fillMaxHeight()
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            // “Tümü” seçeneği
            item {
                Text(
                    text = "Tümü",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { seciliKategoriIndex = 0 }
                        .padding(vertical = 12.dp)
                        .background(
                            if (seciliKategoriIndex == 0) Color.LightGray else Color.Transparent
                        ),
                    textAlign = TextAlign.Center
                )
            }
            // Her kategoriye ait satır
            items(kategoriler) { kategori ->
                val idx = kategoriler.indexOf(kategori) + 1
                Text(
                    text = kategori.kategori_ad,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { seciliKategoriIndex = idx }
                        .padding(vertical = 12.dp)
                        .background(
                            if (seciliKategoriIndex == idx) Color.LightGray else Color.Transparent
                        ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- Sağ taraf: Ürün Izgarası (Grid) ve boş liste mesajı ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            if (guncelUrunListesi.isEmpty()) {
                // Boş mesaj
                Text(
                    text = "Seçilen kategoriye ait ürün bulunamadı.",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(guncelUrunListesi) { urun ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                AsyncImage(
                                    model = urun.urun_resim,
                                    contentDescription = urun.urun_ad,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                                    error = painterResource(id = android.R.drawable.ic_delete)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // 2) Ürün adı
                                Text(
                                    text = urun.urun_ad,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                // 3) Ürün fiyatı
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format("%.2f", urun.urun_fiyat)} TL",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )

                                // 4) Adet ve + / – Butonları
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = {
                                            // Ürün ekle, ardından listeyi yeniden yükle
                                            masaDetayViewModel.urunEkle(urun.id)
                                            masaDetayViewModel.yukleTumVeriler()
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("+")
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = "${urun.urun_adet}",
                                        fontSize = 16.sp,
                                        modifier = Modifier.width(28.dp),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Button(
                                        onClick = {
                                            if (urun.urun_adet > 0) {
                                                masaDetayViewModel.urunCikar(urun.id)
                                                masaDetayViewModel.yukleTumVeriler()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Text("−")
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
