package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.copkutusu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.navOptions
import androidx.xr.compose.testing.toDp
import com.example.isletimsistemicalismasijetpackcompose.anaekran.UygulamaItem
import com.example.isletimsistemicalismasijetpackcompose.anaekran.entity.Const
import com.example.isletimsistemicalismasijetpackcompose.anaekran.model.UygulamalarModel
import com.example.isletimsistemicalismasijetpackcompose.anaekran.repository.UygulamalarRepository

@Composable
fun CopKutusu(modifier: Modifier = Modifier){
    val context = LocalContext.current
    val const = Const(context)
    val repository = UygulamalarRepository(context)
    val copUygulamalar = remember { mutableStateListOf<UygulamalarModel>().apply { addAll(const.getCopUygulamaListesi()) } }
    val dropDownShow = remember { mutableStateOf(false) }
    val selectedUygulama = remember { mutableStateOf<UygulamalarModel?>(null) }
    val dropDownOffset = remember { mutableStateOf(Offset.Zero) } // DropDown pozisyonu

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Bir satırda 3 tane göster
            modifier = Modifier.padding(8.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(copUygulamalar) { uygulama ->
                Box(
                    Modifier.onGloballyPositioned { coordinates ->
                        if (selectedUygulama.value == uygulama) {
                            // Seçilen itemin konumunu al
                            dropDownOffset.value = coordinates.positionInRoot()
                        }
                    }
                ) {
                    UygulamaItem(navController = null,
                        uygulama,
                        onLongClick = {
                            selectedUygulama.value = uygulama
                            dropDownShow.value = true
                        }
                    )
                }
            }
        }

        // DropdownMenu doğru konumda gösteriliyor
        DropdownMenu(
            onDismissRequest = { dropDownShow.value = false },
            expanded = dropDownShow.value,
            offset = DpOffset(dropDownOffset.value.x.toDp(), dropDownOffset.value.y.toDp())
        ) {
            DropdownMenuItem(
                text = { Text("Çöp Kutusuna Taşı") },
                onClick = {
                    selectedUygulama.value?.let {
                        repository.copKutusundanCikar(it)
                        copUygulamalar.remove(it)
                    }
                    dropDownShow.value = false
                }
            )
        }
    }
}