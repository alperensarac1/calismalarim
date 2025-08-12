package com.example.sozlukjetpack.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sozlukjetpack.util.EntriesUiState
import com.example.sozlukjetpack.util.SessionManager
import com.example.sozlukjetpack.viewmodel.ProfilViewModel

// ---- Profil ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    userId: Int,
    session: SessionManager,
    vm: ProfilViewModel = viewModel(),
    onNavigateGundem: () -> Unit,
    onNavigateBugun: () -> Unit,
    onNavigateEntryDetay: (entryId: Int) -> Unit,
    onLoggedOut: () -> Unit
) {
    val context = LocalContext.current
    val entries by vm.entries.collectAsStateWithLifecycle()
    val search by vm.searchQuery.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle(initialValue = EntriesUiState())
    val deleteRes by vm.deleteResult.collectAsStateWithLifecycle()

    LaunchedEffect(userId) { vm.loadUserEntries(userId) }

    LaunchedEffect(deleteRes) {
        deleteRes?.let { res ->
            Toast.makeText(context, res.message ?: if (res.success) "Silindi" else "Silinemedi", Toast.LENGTH_SHORT).show()
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    session.clearSession()
                    showLogoutDialog = false
                    onLoggedOut()
                }) { Text("Evet") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("İptal") } },
            title = { Text("Çıkış Yap") },
            text = { Text("Oturumunuzu kapatmak istediğinizden emin misiniz?") }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(session.getUsername() ?: "Bilinmeyen Kullanıcı") },
                actions = {
                    TextButton(onClick = { showLogoutDialog = true }) { Text("Çıkış Yap") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = false, onClick = onNavigateGundem, label = { Text("Gündem") }, icon = {})
                NavigationBarItem(selected = false, onClick = onNavigateBugun, label = { Text("Bugün") }, icon = {})
                NavigationBarItem(selected = true, onClick = {}, label = { Text("Profil") }, icon = {})
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            SearchField(value = search, onValueChange = vm::setSearchQuery, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            if (ui.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (!ui.error.isNullOrBlank()) {
                Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(entries, key = { it.id }) { e ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f).clickable { onNavigateEntryDetay(e.id) }) {
                                    Text(e.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(6.dp))
                                    Text(e.content, maxLines = 2, style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(Modifier.width(12.dp))
                                OutlinedButton(onClick = { vm.deleteEntry(e.id, userId) }) { Text("Sil") }
                            }
                        }
                    }
                }
            }
        }
    }
}
