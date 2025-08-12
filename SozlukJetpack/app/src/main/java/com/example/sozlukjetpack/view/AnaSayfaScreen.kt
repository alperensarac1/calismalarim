package com.example.sozlukjetpack.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sozlukjetpack.util.EntriesUiState
import com.example.sozlukjetpack.viewmodel.AnaSayfaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnasayfaScreen(
    vm: AnaSayfaViewModel = viewModel(),
    onNavigateEntryEkle: () -> Unit,
    onNavigateEntryDetay: (entryId: Int) -> Unit,
    onNavigateBugun: () -> Unit,
    onNavigateProfil: () -> Unit,
) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val search by vm.searchQuery.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle(initialValue = EntriesUiState())

    LaunchedEffect(Unit) { vm.loadMostCommentedEntriesToday() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Gündem") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onNavigateEntryEkle) { Text("Entry Ekle") }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, label = { Text("Gündem") }, icon = {})
                NavigationBarItem(selected = false, onClick = onNavigateBugun, label = { Text("Bugün") }, icon = {})
                NavigationBarItem(selected = false, onClick = onNavigateProfil, label = { Text("Profil") }, icon = {})
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            SearchField(value = search, onValueChange = vm::setSearchQuery, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            if (ui.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (!ui.error.isNullOrBlank()) {
                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(entries) { e ->
                        EntryRow(entry = e, onClick = { onNavigateEntryDetay(e.id) })
                    }
                }
            }
        }
    }
}
