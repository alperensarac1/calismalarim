package com.example.adisyonuygulamajetpack.view.anaekran



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel


@Composable
fun MainContent(
    masaListesi: List<Masa>,
    viewModel: MasaDetayViewModel,
    onMasaClick: (Masa) -> Unit,
    onMasaDetayClick: (Masa) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFBABA)) // #FFBABA
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            MasaOzetLayout(
                masaListesi = masaListesi,
                viewModel = viewModel,
                onMasaDetayTikla = onMasaDetayClick
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            MasalarLayout(
                masalar = masaListesi,
                onMasaClick = onMasaClick
            )
        }
    }
}
