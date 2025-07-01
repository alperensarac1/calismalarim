import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

@Composable
fun MasaAdisyonScreen(
    masaId: Int,
    viewModel: MasaDetayViewModel = viewModel(factory = MasaDetayViewModel.provideFactory(masaId))
) {
    val context = LocalContext.current

    // LiveData’ları observeAsState ile izliyoruz:
    val masaUrunleri by viewModel.urunler.observeAsState(initial = emptyList())
    val toplamFiyat by viewModel.toplamFiyat.observeAsState(initial = 0f)

    // Başlangıçta verileri yükle
    LaunchedEffect(masaId) {
        viewModel.yukleTumVeriler()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1) Ürün listesini gösteren LazyColumn
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(masaUrunleri) { urun ->
                    Text(
                        text = "${urun.urun_ad} (Adet: ${urun.adet})",
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2) Toplam fiyat ve "Ödeme Al" butonu
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Toplam: ${String.format("%.2f", toplamFiyat)} TL",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.odemeAl {
                            Toast.makeText(context, "Ödeme alındı", Toast.LENGTH_SHORT).show()
                            // Ödeme sonrası listeyi yeniden yükle
                            viewModel.yukleTumVeriler()
                        }
                    }
                ) {
                    Text(text = "Ödeme Al")
                }
            }
        }
    }
}
