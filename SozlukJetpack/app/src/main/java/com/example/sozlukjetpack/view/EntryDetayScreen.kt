package com.example.sozlukjetpack.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.example.sozlukjetpack.model.Comment
import com.example.sozlukjetpack.util.SessionManager
import com.example.sozlukjetpack.viewmodel.EntryDetayViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetayScreen(
    entryId: Int,
    session: SessionManager,
    vm: EntryDetayViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val entry by vm.entry.collectAsStateWithLifecycle()
    val comments by vm.comments.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()

    var commentText by remember { mutableStateOf("") }
    var voteDialogFor by remember { mutableStateOf<Comment?>(null) }

    LaunchedEffect(entryId) {
        vm.loadEntry(entryId)
        vm.loadComments(entryId)
    }

    LaunchedEffect(ui.error) {
        ui.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(entry?.title ?: "Entry Detay") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Geri") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize()
        ) {
            // Entry içerik
            if (ui.loadingEntry) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else entry?.let { e ->
                Text(text = e.title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(text = e.content, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                val meta = listOfNotNull(e.username, e.created_at.takeIf { it.isNotBlank() }?.take(10)).joinToString(" • ")
                if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            Text("Yorumlar", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (ui.loadingComments) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (comments.isEmpty()) {
                    Text("Henüz yorum yok")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f, fill = false)) {
                        items(comments, key = { it.id }) { c ->
                            ElevatedCard(Modifier.fillMaxWidth().clickable { voteDialogFor = c }) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(c.username, style = MaterialTheme.typography.labelLarge)
                                    Spacer(Modifier.height(4.dp))
                                    Text(c.comment_text, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(6.dp))
                                    Text("👍${c.likes}  👎${c.dislikes}", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Yorum yaz") },
                keyboardOptions = KeyboardOptions.Default,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (commentText.isBlank()) return@Button
                    val userId = session.getUserId()
                    vm.addComment(entryId, userId, commentText)
                    commentText = ""
                },
                enabled = !ui.posting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (ui.posting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gönderiliyor…")
                } else Text("Gönder")
            }
        }
    }

    voteDialogFor?.let { c ->
        AlertDialog(
            onDismissRequest = { voteDialogFor = null },
            title = { Text("Yorumu Oyla") },
            text = { Text(c.comment_text) },
            confirmButton = {
                TextButton(onClick = {
                    vm.voteComment(entryId, c.id, session.getUserId(), true)
                    voteDialogFor = null
                }) { Text("👍 Beğen") }
            },
            dismissButton = {
                TextButton(onClick = {
                    vm.voteComment(entryId, c.id, session.getUserId(), false)
                    voteDialogFor = null
                }) { Text("👎 Beğenme") }
            }
        )
    }
}
