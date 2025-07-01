package com.example.adisyonuygulamajetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adisyonuygulamajetpack.view.anaekran.MainScreen
import com.example.adisyonuygulamajetpack.view.masadetay.MasaDetayScreen
import com.example.adisyonuygulamajetpack.viewmodel.*
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }

    @Composable
    private fun AppContent() {
        // 1) MasalarViewModel’i factory’lerden alıyoruz
        val masalarVm: MasalarViewModel =
            viewModel(factory = MasalarViewModelFactory.Factory)

        // 2) UrunViewModel’i factory’lerden alıyoruz
        val urunVm: UrunViewModel =
            viewModel(factory = UrunViewModelFactory.Factory)

        // 3) “Hangi masanın detayı?” state’i
        var selectedMasaId by remember { mutableStateOf<Int?>(null) }

        if (selectedMasaId == null) {
            // Ana ekranda Masalar ve Ürünler listesi
            MainScreen(
                masalarViewModel = masalarVm,
                urunViewModel = urunVm,
                onNavigateToMasaDetay = { masaId ->
                    selectedMasaId = masaId
                }
            )
        } else {
            // Masa detay ekranını açıyoruz
            val masaId = selectedMasaId!!
            // Burada her masaId için ayrı bir ViewModel instance almak adına key= kullanıyoruz
            val masaDetayVm: MasaDetayViewModel =
                viewModel(
                    key = "MasaDetay_$masaId",
                    factory = MasaDetayViewModelFactory.provideFactory(masaId)
                )

            MasaDetayScreen(
                masaId = masaId,
                masaDetayViewModel = masaDetayVm,
                urunViewModel = urunVm,
                onNavigateBack = {
                    // Geri dönüldüğünde ana listeyi yeniliyoruz ve selectedMasaId’i null yapıyoruz
                    masalarVm.masalariYukle()
                    selectedMasaId = null
                }
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewMain() {
        AppContent()
    }
}
