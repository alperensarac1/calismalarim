package com.example.qrkodolusturucujetpack.view

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.qrkodolusturucujetpack.R
import com.example.qrkodolusturucujetpack.model.Urun
import com.example.qrkodolusturucujetpack.usecases.indirimYuzdeHesapla
import com.example.qrkodolusturucujetpack.viewmodel.UrunlerimizVM
import com.google.gson.Gson

@Composable
fun UrunlerimizScreen(navController: NavController, viewModel: UrunlerimizVM = hiltViewModel()) {
    val icecekler = viewModel.icecekler
    val atistirmaliklar = viewModel.atistirmaliklar
    val kampanyalar = viewModel.kampanyalar

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { UrunKategoriListesi("Kampanyalar", kampanyalar, navController) }
        item { UrunKategoriListesi("İçecekler", icecekler, navController) }
        item { UrunKategoriListesi("Atıştırmalıklar", atistirmaliklar, navController) }
    }
}

@Composable
fun UrunKategoriListesi(kategoriAdi: String, urunler: List<Urun>, navController: NavController) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = kategoriAdi,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        LazyRow {
            items(urunler) { urun ->
                UrunCard(urun = urun, onClick = {
                    val encodedUrun = Uri.encode(Gson().toJson(urun))
                    navController.navigate("urunDetay/$encodedUrun")

                })
            }
        }//:LazyRow
    }//:Column
}//:Function End

@Composable
fun UrunCard(urun: Urun, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(width = 150.dp, height = 220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(){
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(urun.urunResim)
                        .placeholder(R.drawable.kahve)
                        .error(R.drawable.kahve)
                        .crossfade(true)
                        .build(),
                    contentDescription = urun.urunAd,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
                if (urun.urunIndirim == 1) {
                    val yuzde = indirimYuzdeHesapla(urun.urunFiyat, urun.urunIndirimliFiyat)
                    Text(
                        text = "%-$yuzde",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.BottomEnd).background(color = colorResource(R.color.textColor))
                    )
                }
            }//:Box

            Row {
                Text(
                    text = urun.urunAd,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(20.dp))
                if (urun.urunIndirim == 1) {
                    val yuzde = indirimYuzdeHesapla(urun.urunFiyat, urun.urunIndirimliFiyat)
                    Column {
                        Text(
                            text = "${urun.urunFiyat.toInt()} TL",
                            style = TextStyle(textDecoration = TextDecoration.LineThrough, fontSize = 12.sp),
                            color = Color.Gray
                        )
                        Row {
                            Text(
                                text = "${urun.urunIndirimliFiyat.toInt()}",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "TL",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }//:Row

                    }//:Column


                } else {
                    Text(
                        text = "${urun.urunFiyat.toInt()} TL",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }//:Row

        }//:Column
    }//:Card
}//:Function End


