package com.example.dosyapaylasimjetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.dosyapaylasimjetpack.ui.theme.DosyaPaylasimJetpackTheme



import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast

import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization

import androidx.compose.ui.unit.dp
import com.example.dosyapaylasimjetpack.model.LinkResponse
import com.example.dosyapaylasimjetpack.model.UploadResponse
import com.example.dosyapaylasimjetpack.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PortalScreen()
            }
        }
    }
}

@Composable
fun PortalScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // UI state
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedSize by remember { mutableStateOf<Long?>(null) }

    var uploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf("") }
    var uploadOkColor by remember { mutableStateOf(MaterialTheme.colorScheme.primary) }
    var lastDownloadUrl by remember { mutableStateOf<String?>(null) }

    var code by remember { mutableStateOf("") }
    var checkMessage by remember { mutableStateOf("") }
    var checkColor by remember { mutableStateOf(MaterialTheme.colorScheme.primary) }

    // SAF picker
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedUri = uri
                val (name, size) = queryMeta(context, uri)
                selectedName = name
                selectedSize = size
            }
        }
    )

    // Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            "Dosya Yükleme & Kodla İndirme",
            style = MaterialTheme.typography.titleLarge
        )

        // --- 1) Dosya Yükle Kartı ---
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1) Dosya Yükle", style = MaterialTheme.typography.titleMedium)

                Text(
                    "Seçili dosya: ${selectedName ?: "(yok)"}" +
                            (selectedSize?.let { " — ${it} bayt" } ?: "")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { pickFileLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f)) {
                        Text("Dosya Seç")
                    }
                    Button(
                        onClick = {
                            if (selectedUri == null) {
                                Toast.makeText(context, "Önce dosya seçin", Toast.LENGTH_SHORT).show()
                            } else {
                                uploadFile(
                                    context = context,
                                    uri = selectedUri!!,
                                    displayName = selectedName ?: "file",
                                    onStart = {
                                        uploading = true
                                        uploadMessage = ""
                                        lastDownloadUrl = null
                                    },
                                    onResult = { ok, msg, downloadUrl, codeResp, infoUrl, expiresAt ->
                                        uploading = false
                                        uploadMessage = msg
                                        uploadOkColor = if (ok) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                        if (ok) {
                                            lastDownloadUrl = downloadUrl
                                            // kodu otomatik alanımıza yazalım
                                            code = codeResp ?: ""
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uploading
                    ) { Text("Yükle") }
                }

                if (uploading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                if (uploadMessage.isNotEmpty()) {
                    Text(uploadMessage, color = uploadOkColor)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = {
                            val url = lastDownloadUrl
                            if (!url.isNullOrEmpty()) {
                                // Compose clipboard
                                clipboard.setText(AnnotatedString(url))
                                Toast.makeText(context, "Link panoya kopyalandı", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Henüz bir link yok", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !lastDownloadUrl.isNullOrEmpty()
                    ) { Text("İndirme Linkini Kopyala") }

                    OutlinedButton(
                        onClick = {
                            lastDownloadUrl?.let { openExternal(context, it) }
                        },
                        enabled = !lastDownloadUrl.isNullOrEmpty()
                    ) { Text("Linki Aç") }
                }

                Text(
                    "Yüklenen dosyalar 14 gün sonra otomatik silinir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // --- 2) Kodu Gir & İndir Kartı ---
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("2) Kodu Gir & İndir", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase().take(6) },
                    label = { Text("6 Haneli Kod") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    supportingText = { Text("Örn: ABC123") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            val valid = Regex("^[A-Z0-9]{6}$").matches(code)
                            if (!valid) {
                                Toast.makeText(context, "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            checkMessage = "Sorgulanıyor…"
                            checkColor = MaterialTheme.colorScheme.primary

                            RetrofitClient.api.getLink(code).enqueue(object : Callback<LinkResponse> {
                                override fun onResponse(
                                    call: Call<LinkResponse>,
                                    response: Response<LinkResponse>
                                ) {
                                    val body = response.body()
                                    if (response.isSuccessful && body != null && body.ok == true) {
                                        if (body.expired == true) {
                                            checkColor = MaterialTheme.colorScheme.tertiary
                                            checkMessage = "Kod: ${body.code} — Süresi dolmuş veya pasif."
                                        } else {
                                            checkColor = MaterialTheme.colorScheme.primary
                                            checkMessage = buildString {
                                                appendLine("Kod: ${body.code}")
                                                appendLine("Dosya: ${body.original_name}")
                                                appendLine("Boyut: ${body.size_bytes}")
                                                appendLine("Son Kullanım: ${body.expires_at}")
                                                append("Link: ${body.download_url}")
                                            }
                                        }
                                    } else {
                                        checkColor = MaterialTheme.colorScheme.error
                                        val err = body?.error ?: "Sunucu hatası ${response.code()}"
                                        checkMessage = "Hata: $err"
                                    }
                                }

                                override fun onFailure(call: Call<LinkResponse>, t: Throwable) {
                                    checkColor = MaterialTheme.colorScheme.error
                                    checkMessage = "İstek hatası: ${t.message}"
                                }
                            })
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Linki Kontrol Et") }

                    Button(
                        onClick = {
                            val valid = Regex("^[A-Z0-9]{6}$").matches(code)
                            if (!valid) {
                                Toast.makeText(context, "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val url = "https://alperensaracdeneme.com/api/download.php?code=$code"
                            openExternal(context, url)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("İndir") }
                }

                if (checkMessage.isNotEmpty()) {
                    Text(checkMessage, color = checkColor)
                }
            }
        }
    }
}

// --- Yardımcılar ---

private fun openExternal(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "İndirme bağlantısı açılamadı", Toast.LENGTH_SHORT).show()
    }
}

private fun queryMeta(context: Context, uri: Uri): Pair<String?, Long?> {
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
        if (c.moveToFirst()) {
            val name = if (nameIdx >= 0) c.getString(nameIdx) else null
            val size = if (sizeIdx >= 0) c.getLong(sizeIdx) else null
            return name to size
        }
    }
    return null to null
}

private fun RequestBody.Companion.fromUri(context: Context, uri: Uri, displayName: String?): MultipartBody.Part? {
    // Basit yaklaşım: RAM'e oku. Büyük dosyalar için streaming RequestBody yazılmalı.
    val bytes = context.contentResolver.openInputStream(uri)?.use(InputStream::readBytes) ?: return null
    val guessType = context.contentResolver.getType(uri) ?: "application/octet-stream"
    val rb = RequestBody.create(guessType.toMediaTypeOrNull(), bytes)
    return MultipartBody.Part.createFormData("file", displayName ?: "file", rb)
}

private fun uploadFile(
    context: Context,
    uri: Uri,
    displayName: String,
    onStart: () -> Unit,
    onResult: (ok: Boolean, msg: String, downloadUrl: String?, code: String?, infoUrl: String?, expiresAt: String?) -> Unit
) {
    onStart()
    val part = RequestBody.fromUri(context, uri, displayName)
    if (part == null) {
        onResult(false, "Dosya açılamadı", null, null, null, null)
        return
    }

    RetrofitClient.api.uploadFile(part).enqueue(object : Callback<UploadResponse> {
        override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.ok == true) {
                    val msg = buildString {
                        appendLine("Yüklendi! Kod: ${body.code}")
                        appendLine("İndirme: ${body.download_url}")
                        appendLine("Bilgi: ${body.info_url}")
                        append("Geçerlilik: ${body.expires_at}")
                    }
                    onResult(true, msg, body.download_url, body.code, body.info_url, body.expires_at)
                } else {
                    onResult(false, "Hata: ${body.error ?: "Bilinmeyen"}", null, null, null, null)
                }
            } else {
                onResult(false, "Sunucu hatası: ${response.code()}", null, null, null, null)
            }
        }

        override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
            onResult(false, "İstek hatası: ${t.message}", null, null, null, null)
        }
    })
}
