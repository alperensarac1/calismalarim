package com.example.adisyonuygulamajetpack.view.anaekran

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adisyonuygulamajetpack.viewmodel.MasalarViewModel
import com.example.adisyonuygulamajetpack.viewmodel.MasalarViewModelFactory
import com.example.adisyonuygulamajetpack.viewmodel.UrunViewModel
import com.example.adisyonuygulamajetpack.viewmodel.UrunViewModelFactory
import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.viewmodel.MasaDetayViewModel

@Composable
fun MainScreen(
    masalarViewModel: MasalarViewModel = viewModel(factory = MasalarViewModelFactory.Factory),
    urunViewModel: UrunViewModel = viewModel(factory = UrunViewModelFactory.Factory),
    onNavigateToMasaDetay: (Int) -> Unit   // Bu lambda, tıklanan masa ID'sini aktarıp detaya geçecek
) {
    val context = LocalContext.current

    // LiveData → State dönüşümleri
    val masaList by masalarViewModel.masalar.observeAsState(emptyList())
    val kategoriList by urunViewModel.kategoriler.observeAsState(emptyList())

    // 1) Başlangıçta verileri yükle
    LaunchedEffect(Unit) {
        masalarViewModel.masalariYukle()
        urunViewModel.kategorileriYukle()
    }

    // Dialog’ları kontrol eden state’ler
    var showMasaDialog by remember { mutableStateOf(false) }
    var showUrunDialog by remember { mutableStateOf(false) }
    var showBirlestirDialog by remember { mutableStateOf(false) }
    var showKategoriDialog by remember { mutableStateOf(false) }

    // Resim seçici için
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                selectedImageBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                Toast.makeText(context, "Resim seçildi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFBABA))
    ) {
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                MasaOzetLayout(
                    masaListesi = masaList,
                    onUrunEkleClick = { masa ->
                        // Burada detaya geçiş lambda"sını çağırıyoruz
                        onNavigateToMasaDetay(masa.id)
                    },
                    onOdemeAlClick = { masa ->
                        val mvm = MasaDetayViewModel(masa.id) // ihtiyaca göre kullanılabilir
                        mvm.odemeAl {
                            Toast.makeText(context, "Ödeme alındı: Masa ${masa.id}", Toast.LENGTH_SHORT).show()
                            masalarViewModel.masalariYukle()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sağ sütun: Masalar Grid
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                MasalarLayout(
                    masalar = masaList,
                    onMasaClick = { masa ->
                        // Masaya tıklayınca detaya geç
                        onNavigateToMasaDetay(masa.id)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // —–––––– Alt Buton Satırı ––––––—
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { showMasaDialog = true }, modifier = Modifier.weight(1f)) {
                Text("Masa İşlemleri")
            }
            Button(onClick = { showUrunDialog = true }, modifier = Modifier.weight(1f)) {
                Text("Ürün İşlemleri")
            }
            Button(onClick = { showBirlestirDialog = true }, modifier = Modifier.weight(1f)) {
                Text("Masa Birleştir")
            }
            Button(onClick = { showKategoriDialog = true }, modifier = Modifier.weight(1f)) {
                Text("Kategori İşlemleri")
            }
        }
    }

    // Masa Ekle/Çıkar Dialog
    if (showMasaDialog) {
        MasaEkleCikarDialog(
            onDismiss = { showMasaDialog = false },
            onEkle = {
                masalarViewModel.masaEkle {
                    Toast.makeText(context, "Masa eklendi", Toast.LENGTH_SHORT).show()
                    masalarViewModel.masalariYukle()
                }
            },
            onSil = { id ->
                masalarViewModel.masaSil(id)
                Toast.makeText(context, "Masa $id silme isteği gönderildi", Toast.LENGTH_SHORT).show()
                masalarViewModel.masalariYukle()
            }
        )
    }

    // Ürün Ekle/Sil Dialog
    if (showUrunDialog) {
        UrunEkleVeSilDialog(
            onDismiss = { showUrunDialog = false },
            kategoriList = kategoriList,
            onResimSec = { imagePickerLauncher.launch("image/*") },
            onEkle = { ad, fiyat, kategoriId, base64 ->
                urunViewModel.urunEkle(ad, fiyat, kategoriId, base64)
                Toast.makeText(context, "Ürün ekleme isteği gönderildi", Toast.LENGTH_SHORT).show()
                urunViewModel.urunleriYukle()
                showUrunDialog = false
            },
            onSil = { urunAd ->
                urunViewModel.urunSil(urunAd)
                Toast.makeText(context, "\"$urunAd\" silme isteği gönderildi", Toast.LENGTH_SHORT).show()
                urunViewModel.urunleriYukle()
                showUrunDialog = false
            }
        )
    }

    // Masa Birleştir Dialog
    if (showBirlestirDialog) {
        MasaBirlestirDialog(
            onDismiss = { showBirlestirDialog = false },
            onBirleştir = { anaId, bId ->
                masalarViewModel.masaBirlestir(anaId, bId)
                Toast.makeText(context, "Masa $anaId ve Masa $bId birleştiriliyor", Toast.LENGTH_SHORT).show()
                masalarViewModel.masalariYukle()
                showBirlestirDialog = false
            }
        )
    }

    // Kategori Ekle/Sil Dialog
    if (showKategoriDialog) {
        KategoriEkleVeSilDialog(
            onDismiss = { showKategoriDialog = false },
            kategoriList = kategoriList,
            onEkle = { yeniAd ->
                urunViewModel.kategoriEkle(yeniAd)
                urunViewModel.kategorileriYukle()
                Toast.makeText(context, "Kategori '$yeniAd' eklendi", Toast.LENGTH_SHORT).show()
                showKategoriDialog = false
            },
            onSil = { kategoriId ->
                urunViewModel.kategoriSil(kategoriId)
                urunViewModel.kategorileriYukle()
                Toast.makeText(context, "Kategori $kategoriId silme isteği gönderildi", Toast.LENGTH_SHORT).show()
                showKategoriDialog = false
            }
        )
    }
}


/**
 *  “Masa Özeti” Bölümü
 */
@Composable
private fun MasaOzetLayout(
    masaListesi: List<Masa>,
    onUrunEkleClick: (Masa) -> Unit,
    onOdemeAlClick: (Masa) -> Unit
) {
    var openDialogFor by remember { mutableStateOf<Masa?>(null) }
    val acikMasalar = masaListesi.filter { it.acikMi == 1 }
    val acikMasaSayisi = acikMasalar.size

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$acikMasaSayisi adet masa açık",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Red,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(acikMasalar) { masa ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openDialogFor = masa }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Masa ${masa.id}", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tutar: %.2f ₺".format(masa.toplam_fiyat),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (openDialogFor != null) {
        val masa = openDialogFor!!
        AlertDialog(
            onDismissRequest = { openDialogFor = null },
            title = { Text("Masa ${masa.id}") },
            text = { Text("Seçenekler:") },
            confirmButton = {
                TextButton(onClick = {
                    onUrunEkleClick(masa)
                    openDialogFor = null
                }) { Text("Ürün Ekle") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onOdemeAlClick(masa)
                    openDialogFor = null
                }) { Text("Ödeme Al") }
            }
        )
    }
}

/**
 *  MasaEkle/Çıkar Dialog
 */
@Composable
private fun MasaEkleCikarDialog(
    onDismiss: () -> Unit,
    onEkle: () -> Unit,
    onSil: (Int) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Masa İşlemleri") },
        text = {
            Column {
                Text("Yeni masa ekleyebilir veya masa ID girerek masa silebilirsiniz.")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Silinecek Masa ID (sayı)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onEkle()
                onDismiss()
            }) {
                Text("Masa Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                val id = inputText.toIntOrNull()
                if (id != null) {
                    onSil(id)
                }
                onDismiss()
            }) {
                Text("Masa Sil")
            }
        }
    )
}

/**
 *  UrunEkle/Sil Dialog
 */
@Composable
private fun UrunEkleVeSilDialog(
    onDismiss: () -> Unit,
    kategoriList: List<Kategori>,
    onResimSec: () -> Unit,
    onEkle: (String, Float, Int, String) -> Unit,
    onSil: (String) -> Unit
) {
    var urunAd by remember { mutableStateOf("") }
    var urunFiyatText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedKategoriIndex by remember { mutableStateOf(0) }
    var silUrunAd by remember { mutableStateOf("") }
    var base64 by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ürün İşlemleri") },
        text = {
            Column {
                Text("— ÜRÜN EKLEME —", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = urunAd,
                    onValueChange = { urunAd = it },
                    label = { Text("Ürün Adı") }
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = urunFiyatText,
                    onValueChange = { urunFiyatText = it },
                    label = { Text("Ürün Fiyatı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    OutlinedTextField(
                        value = if (kategoriList.isNotEmpty()) kategoriList[selectedKategoriIndex].kategori_ad else "",
                        onValueChange = {},
                        label = { Text("Kategori Seç") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Open dropdown")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        kategoriList.forEachIndexed { idx, k ->
                            DropdownMenuItem(
                                text = { Text(k.kategori_ad) },
                                onClick = {
                                    selectedKategoriIndex = idx
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = { onResimSec() }) {
                    Text("Resim Seç")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("— ÜRÜN SİLME —", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = silUrunAd,
                    onValueChange = { silUrunAd = it },
                    label = { Text("Silinecek Ürün Adı") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val fiyat = urunFiyatText.toFloatOrNull() ?: 0f
                val katId = if (kategoriList.isNotEmpty()) kategoriList[selectedKategoriIndex].id else 0
                if (urunAd.isNotBlank() && base64 != null && katId != 0) {
                    onEkle(urunAd, fiyat, katId, base64!!)
                    onDismiss()
                }
            }) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (silUrunAd.isNotBlank()) {
                    onSil(silUrunAd)
                }
                onDismiss()
            }) {
                Text("Sil")
            }
        }
    )
}

/**
 *  Masa Birleştir Dialog
 */
@Composable
private fun MasaBirlestirDialog(
    onDismiss: () -> Unit,
    onBirleştir: (Int, Int) -> Unit
) {
    var anaMasaIdText by remember { mutableStateOf("") }
    var bMasaIdText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Masa Birleştir") },
        text = {
            Column {
                OutlinedTextField(
                    value = anaMasaIdText,
                    onValueChange = { anaMasaIdText = it },
                    label = { Text("Ana Masa ID") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = bMasaIdText,
                    onValueChange = { bMasaIdText = it },
                    label = { Text("Birleştirilecek Masa ID") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val a = anaMasaIdText.toIntOrNull()
                val b = bMasaIdText.toIntOrNull()
                if (a != null && b != null) {
                    onBirleştir(a, b)
                    onDismiss()
                }
            }) {
                Text("Birleştir")
            }
        }
    )
}

/**
 *  Kategori Ekle/Sil Dialog
 */
@Composable
private fun KategoriEkleVeSilDialog(
    onDismiss: () -> Unit,
    kategoriList: List<Kategori>,
    onEkle: (String) -> Unit,
    onSil: (Int) -> Unit
) {
    var yeniKategoriAd by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kategori İşlemleri") },
        text = {
            Column {
                OutlinedTextField(
                    value = yeniKategoriAd,
                    onValueChange = { yeniKategoriAd = it },
                    label = { Text("Yeni Kategori Adı") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box {
                    OutlinedTextField(
                        value = if (kategoriList.isNotEmpty()) kategoriList[selectedIndex].kategori_ad else "",
                        onValueChange = {},
                        label = { Text("Silinecek Kategori") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        kategoriList.forEachIndexed { idx, k ->
                            DropdownMenuItem(
                                text = { Text(k.kategori_ad) },
                                onClick = {
                                    selectedIndex = idx
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (yeniKategoriAd.isNotBlank()) {
                    onEkle(yeniKategoriAd.trim())
                    onDismiss()
                }
            }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (kategoriList.isNotEmpty()) {
                    onSil(kategoriList[selectedIndex].id)
                }
                onDismiss()
            }) {
                Text("Sil")
            }
        }
    )
}
