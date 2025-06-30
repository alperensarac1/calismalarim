package com.example.isletimsistemicalismasijetpackcompose.anaekran

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.xr.compose.testing.toDp
import com.example.isletimsistemicalismasijetpackcompose.anaekran.entity.Const
import com.example.isletimsistemicalismasijetpackcompose.anaekran.model.UygulamalarModel
import com.example.isletimsistemicalismasijetpackcompose.anaekran.repository.UygulamalarRepository
import com.example.isletimsistemicalismasijetpackcompose.giris_ekrani.Screens
import kotlinx.coroutines.coroutineScope

@Composable
fun UygulamaEkraniScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val const = Const(context)
    val repository = UygulamalarRepository(context)

    // Listeyi mutableStateListOf ile yönetiyoruz
    val uygulamalar = remember { mutableStateListOf<UygulamalarModel>() }

    val dropDownShow = remember { mutableStateOf(false) }
    val selectedUygulama = remember { mutableStateOf<UygulamalarModel?>(null) }
    val dropDownOffset = remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        if (uygulamalar.isEmpty()) {  // Eğer zaten doluysa tekrar ekleme!
            Const.uygulamalariYukle()
            uygulamalar.addAll(const.getUygulamalarListesi().distinct())
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = modifier.padding(8.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uygulamalar) { uygulama ->
                Box(
                    Modifier.onGloballyPositioned { coordinates ->
                        if (selectedUygulama.value == uygulama) {
                            dropDownOffset.value = coordinates.positionInRoot()
                        }
                    }
                ) {
                    UygulamaItem(
                        navController,
                        uygulama,
                        onLongClick = {
                            if (uygulama.uygulamaAdi != "Çöp Kutusu") {
                                selectedUygulama.value = uygulama
                                dropDownShow.value = true
                            }
                        }
                    )
                }
            }
        }

        DropdownMenu(
            onDismissRequest = { dropDownShow.value = false },
            expanded = dropDownShow.value,
            offset = DpOffset(dropDownOffset.value.x.toDp(), dropDownOffset.value.y.toDp())
        ) {
            DropdownMenuItem(
                text = { Text("Çöp Kutusuna Taşı") },
                onClick = {
                    selectedUygulama.value?.let {
                        repository.copKutusunaTasi(it)
                        uygulamalar.remove(it)
                    }
                    dropDownShow.value = false
                }
            )
        }
    }
}


@Composable
fun UygulamaItem(
    navController: NavController? = null,
    uygulama: UygulamalarModel,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth() // Column'u genişlik olarak doldur
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        onLongClick()
                        Log.d("JetpackCompose", "Uzun basıldı!")
                    }
                )
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Ortalamak için eklendi
    ) {
        // Image doğru şekilde gösteriliyor ve ortalanıyor
        Image(
            painter = painterResource(
                id = LocalContext.current.resources.getIdentifier(
                    uygulama.uygulamaResimAdi, "drawable",
                    LocalContext.current.packageName
                )
            ),
            contentDescription = uygulama.uygulamaAdi,
            modifier = modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .align(Alignment.CenterHorizontally).clickable { navController!!.navigate(uygulama.gecisId) } // Ortalamak için eklendi
        )

        Spacer(modifier = modifier.height(8.dp))
        Text(
            text = uygulama.uygulamaAdi,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

