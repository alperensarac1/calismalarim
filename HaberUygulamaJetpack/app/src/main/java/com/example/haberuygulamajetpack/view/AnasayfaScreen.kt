package com.example.haberuygulamajetpack.view

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.haberuygulamajetpack.deo.HaberDao
import com.example.haberuygulamajetpack.model.HaberModel
import com.example.haberuygulamajetpack.navigation.Screen
import com.example.haberuygulamajetpack.viewmodel.HaberlerViewModel

@Composable
fun AnasayfaScreen(
    navController: NavHostController,
    haberdao: HaberDao
) {
    // ViewModel'i doğrudan başlatıyoruz
    val viewModel: HaberlerViewModel = viewModel()

    // StateFlow'lardan veri alıyoruz
    val sonDakikaHaberler by viewModel.sonDakikaHaberler.collectAsState()
    val gundemHaberler by viewModel.gundemHaberler.collectAsState()
    val kategoriler by viewModel.kategoriler.collectAsState()

    LaunchedEffect(Unit) {
        // Veri yükleme işlemleri
        viewModel.loadSonDakikaHaberler()
        viewModel.loadGundemHaberler()
        viewModel.loadKategoriler()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Son dakika haberleri başlığı
        Text("SON DAKİKA", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC65555))
        Spacer(modifier = Modifier.height(8.dp))

        // Son dakika haberleri
        if (sonDakikaHaberler.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sonDakikaHaberler) { haber ->
                    HaberCard(haber, onHaberClick = { navController.navigate(Screen.Detay.withArgs(haber)) }) // Detay sayfasına geçiş
                }
            }
        } else {
            Text("Son dakika haberleri yükleniyor...")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("GÜNDEM", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC65555))
        Spacer(modifier = Modifier.height(8.dp))

        // Gündem haberleri
        if (gundemHaberler.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(gundemHaberler) { haber ->
                    HaberCard(haber, onHaberClick = { navController.navigate(Screen.Detay.withArgs(haber)) }) // Detay sayfasına geçiş
                }
            }
        } else {
            Text("Gündem haberleri yükleniyor...")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("KATEGORİLER", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC65555))
        Spacer(modifier = Modifier.height(8.dp))

        // Kategoriler
        if (kategoriler.isNotEmpty()) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(kategoriler) { kategori ->
                    Text(
                        text = kategori.tur_adi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("kategori/${kategori.id}") } // Kategoriye gitmek için navController kullanıyoruz
                            .padding(12.dp),
                        fontSize = 18.sp
                    )
                }
            }
        } else {
            Text("Kategoriler yükleniyor...")
        }
    }
}


@Composable
fun HaberCard(haber: HaberModel, onHaberClick: (HaberModel) -> Unit) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(250.dp) // Yüksekliği artırdık, çünkü medya + metin daha fazla alan alacak
            .clickable { onHaberClick(haber) },  // Tıklama işlemi
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Medya (Video veya Resim)
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (haber.media_type == "video") {
                    val uri = remember(haber.media_url) { Uri.parse(haber.media_url) }
                    AndroidView(factory = {
                        VideoView(it).apply {
                            setVideoURI(uri)
                            start()
                        }
                    }, modifier = Modifier.fillMaxSize())
                } else {
                    AsyncImage(
                        model = haber.media_url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Text ve Devamını Oku kısmı
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = haber.baslik,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Devamını Oku->",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
