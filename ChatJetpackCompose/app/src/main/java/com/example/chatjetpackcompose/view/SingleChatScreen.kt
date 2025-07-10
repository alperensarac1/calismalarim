package com.example.chatjetpackcompose.view

import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.chatjetpackcompose.adapter.MessageBubble
import com.example.chatjetpackcompose.viewmodel.MesajlarViewModel
import com.example.chatkotlin.util.AppConfig
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChatScreen(
    aliciId: Int,
    aliciAd: String,
    viewModel: MesajlarViewModel = viewModel(),
    navController: NavController
) {
    val mesajlar by viewModel.mesajlar.collectAsState()
    val benimId  = AppConfig.kullaniciId
    val listState = rememberLazyListState()


    LaunchedEffect(aliciId) {
        viewModel.mesajlariYuklePeriyodik(benimId, aliciId)
    }


    LaunchedEffect(mesajlar.size) {
        listState.animateScrollToItem(max(0, mesajlar.lastIndex))
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(aliciAd) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                onSend = { metin, img64 ->
                    viewModel.mesajGonder(benimId, aliciId, metin, img64)
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(mesajlar, key = { it.id }) { msg ->
                MessageBubble(msg, benimId)
            }
        }//:LazyColumn
    }//:Scaffold
}
@Composable
fun ChatInputBar(
    onSend: (metin: String, imgBase64: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imgPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        previewUri = uri
    }

    Column(modifier) {
        previewUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "önizleme",
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { previewUri = null } // Kaldır
            )
            Spacer(Modifier.height(4.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { imgPicker.launch("image/*") }) {
                Icon(Icons.Default.Add, null)
            }

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mesaj yaz…") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isBlank() && previewUri == null) {
                            Toast.makeText(context, "Boş mesaj gönderilemez", Toast.LENGTH_SHORT).show()
                        }

                        val base64 = previewUri?.let { uri ->
                            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                ?.let { bytes -> Base64.encodeToString(bytes, Base64.NO_WRAP) }
                        }

                        onSend(text.trim(), base64)
                        text = ""
                        previewUri = null
                    }
                )
            )

            IconButton(onClick = {
                if (text.isBlank() && previewUri == null) {
                    Toast.makeText(context, "Boş mesaj gönderilemez", Toast.LENGTH_SHORT).show()
                }

                val base64 = previewUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?.let { bytes -> Base64.encodeToString(bytes, Base64.NO_WRAP) }
                }

                onSend(text.trim(), base64)
                text = ""
                previewUri = null
            }) {
                Icon(Icons.Default.Send, null)
            }
        }//:Row
    }//:Column
}
