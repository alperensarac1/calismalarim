package com.example.isletimsistemicalismasijetpackcompose.uygulamalar.notdefteri

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.UygulamaGecisi

@Composable
fun NotDefteriAnasayfa(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: NotDefteriViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val notListesi by viewModel.notlar.observeAsState(emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(UygulamaGecisi.toNotDefteriEkle.name)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Not Ekle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Scaffold’un padding’ini uygula
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.notAra(it)
                },
                modifier = modifier.fillMaxWidth(),
                placeholder = { Text(text = "Ara...") },
                singleLine = true
            )

            Spacer(modifier = modifier.height(8.dp))

            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(notListesi) { not ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .clickable {
                                navController.navigate(UygulamaGecisi.toNotDefteriDetay.name+"/${not.id}")
                            },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = modifier.padding(8.dp)) {
                            Text(text = not.baslik, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = modifier.height(4.dp))
                            Text(text = not.notMetin, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
