package com.example.sozlukjetpack.view
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
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
import com.example.sozlukjetpack.viewmodel.BugunViewModel
import com.example.sozlukjetpack.viewmodel.EntryEkleViewModel

// ---- Entry Ekle ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEkleScreen(
    session: SessionManager,
    vm: EntryEkleViewModel = viewModel(),
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val addResult by vm.addResult.collectAsStateWithLifecycle() // Eğer StateFlow'a çevirdiysen collectAsStateWithLifecycle kullan

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Sonuç dinleme
    LaunchedEffect(addResult) {
        addResult?.let { res ->
            isLoading = false
            if (res.success) {
                Toast.makeText(context, "Entry eklendi", Toast.LENGTH_SHORT).show()
                onSaved() // navigateUp
            } else {
                Toast.makeText(context, res.message ?: "Hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Entry Ekle") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Geri") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Başlık") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("İçerik") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isBlank() || content.isBlank()) {
                        Toast.makeText(context, "Tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    val userId = session.getUserId()
                    vm.addEntry(userId, title, content)
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Kaydediliyor…")
                } else {
                    Text("Kaydet")
                }
            }
        }
    }
}
