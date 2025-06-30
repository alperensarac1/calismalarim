package com.example.qrkodolusturucujetpack.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrkodolusturucujetpack.viewmodel.QRKodOlusturVM
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.qrkodolusturucujetpack.R
import com.example.qrkodolusturucujetpack.components.BeyazButton

@Composable
fun QRKodOlusturScreen(
    viewModel: QRKodOlusturVM = hiltViewModel()
) {
    val bitmap = viewModel.qrKodBitmap
    val kalanSure = viewModel.kalanSure
    val kod = viewModel.dogrulamaKodu

    LaunchedEffect(kod) {
        if (kod.isNotEmpty()) {
            viewModel.QRKodOlustur(kod)
            viewModel.koduOlustur(kod)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize().background(color = colorResource(R.color.background)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "QR Kod",
                modifier = Modifier.size(350.dp).clip(
                    RoundedCornerShape(30.dp)
                )
            )
        } ?: CircularProgressIndicator()
        Text(text = "Yenileme için kalan süre: $kalanSure")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Her 5 kahve alımınızda 1 kahve bizden !!", style = TextStyle(fontSize = 20.sp, color = colorResource(R.color.textColor)))
        Row {
            BeyazButton("Şubelerimiz")
            Spacer(modifier = Modifier.width(20.dp))
            BeyazButton("Bizi Puanlayın")
        }//:Row
        Spacer(modifier =  Modifier.height(20.dp))
        BeyazButton("Rastgele Ürün Getir")

    }//:Column
}

