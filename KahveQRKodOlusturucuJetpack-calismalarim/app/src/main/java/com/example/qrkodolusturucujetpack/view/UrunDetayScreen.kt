package com.example.qrkodolusturucujetpack.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.qrkodolusturucujetpack.R
import com.example.qrkodolusturucujetpack.model.Urun

@Composable
fun UrunDetayScreen(
    urun: Urun, // Navigation'dan gönderilen ürün
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Geri Butonu
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Geri Dön"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ürün Görseli
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(urun.urunResim)
                .crossfade(true)
                .error(R.drawable.kahve)
                .placeholder(R.drawable.kahve)
                .build(),
            contentDescription = urun.urunAd,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ürün Adı
        Text(text = urun.urunAd, style = MaterialTheme.typography.titleLarge)

        // Ürün Açıklaması
        Text(text = urun.urunAciklama, style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))

        // Fiyat Bilgisi
        if (urun.urunIndirim == 1) {
            Text(
                text = "${urun.urunFiyat.toInt()} TL",
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough)
            )
            Text(
                text = "${urun.urunIndirimliFiyat.toInt()} TL",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Red)
            )
        } else {
            Text(
                text = "${urun.urunFiyat.toInt()} TL",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
