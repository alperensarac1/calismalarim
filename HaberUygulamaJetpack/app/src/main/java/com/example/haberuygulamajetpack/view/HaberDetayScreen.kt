package com.example.haberuygulamajetpack.view

import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.haberuygulamajetpack.R
import com.example.haberuygulamajetpack.deo.HaberDao
import com.example.haberuygulamajetpack.model.HaberModel
import com.example.haberuygulamajetpack.navigation.Screen
import com.example.haberuygulamajetpack.viewmodel.HaberDetayViewModel
import com.example.haberuygulamajetpack.viewmodel.HaberlerViewModel

@Composable
fun HaberDetayScreen(
    navController: NavHostController,
    haber: HaberModel
) {
    // Yorumları gözlemliyoruz
    val yorumViewModel: HaberDetayViewModel = viewModel()  // Parametresiz olarak viewModel oluşturuluyor
    val viewModel: HaberlerViewModel = viewModel()  // Parametresiz olarak viewModel oluşturuluyor

    // Yorumları ve son haberleri gözlemlemek
    val yorumlar by yorumViewModel.yorumlar.collectAsState()
    val context = LocalContext.current
    var rumuz by remember { mutableStateOf("") }
    var yorum by remember { mutableStateOf("") }

    // Yorumları ve son 3 haberi yüklemek için LaunchedEffect kullanıyoruz
    LaunchedEffect(haber.id) {
        yorumViewModel.loadYorumlar(haber.id)  // Yorumları yükle
        viewModel.loadSon3Haber()  // Son 3 haberi yükle
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            // Başlık, içerik ve medya gösterimi
            Text(
                text = haber.baslik,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF707070)
            )
        }

        item {
            // Medya içeriği (video veya resim)
            Box(modifier = Modifier.fillMaxWidth()) {
                if (haber.media_type == "video") {
                    val uri = remember(haber.media_url) { Uri.parse(haber.media_url) }
                    AndroidView(factory = {
                        VideoView(it).apply {
                            setVideoURI(uri)
                            start()
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp))
                } else {
                    AsyncImage(
                        model = haber.media_url,  // Resmin doğru URL'sini kullandığımızdan emin olun
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.resim),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    println(haber.media_url)
                }
            }
        }

        item {
            // Yazar ve yayın tarihi
            Text(
                text = "${haber.ad} ${haber.soyad} - ${haber.unvan}",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = haber.yayinlanma_tarihi,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            // İçerik
            Text(
                text = haber.icerik,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))  // Boşluk ekliyoruz
            Text(
                text = "Son Haberler",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFF707070)
            )
        }

        // Son haberler listesi (LazyColumn)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.sonHaberler.value) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                navController.navigate(Screen.Detay.withArgs(item))  // Detay sayfasına geçiş
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.baslik,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                            AsyncImage(
                                model = item.media_url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentScale = ContentScale.Crop
                            )
                            println(item.media_url)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))  // Boşluk ekliyoruz

            Text(
                text = "Yorum Yaz",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6640A3),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Rumuz ve yorum yazma alanları
            OutlinedTextField(
                value = rumuz,
                onValueChange = { rumuz = it },
                label = { Text("Rumuz") },
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
            )

            OutlinedTextField(
                value = yorum,
                onValueChange = { yorum = it },
                label = { Text("Yorumunuz") },
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )

            // Yorum gönderme butonu
            Button(
                onClick = {
                    if (rumuz.isNotBlank() && yorum.isNotBlank()) {
                        yorumViewModel.yorumEkle(haber.id, rumuz, yorum)
                        rumuz = ""
                        yorum = ""
                    } else {
                        Toast.makeText(context, "İlgili alanlar boş bırakılamaz", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text("GÖNDER")
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))  // Boşluk ekliyoruz

            Text(
                text = "Yorumlar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6640A3),
                modifier = Modifier.padding(16.dp)
            )
        }

        // Yorumları listeleme
        items(yorumlar) { yorum ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = yorum.yorum_metni)
                    Text(text = "— ${yorum.takma_ad}", fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

