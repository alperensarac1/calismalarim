package com.example.pdfconverterjetpack.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pdfconverterjetpack.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel,
    onPickJpgToPdf: () -> Unit,
    onPickPdfToWord: () -> Unit,
    onPickWordToPdf: () -> Unit,
    onPickMergePdf: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "PDF Dönüştürücü")
                }
            )
        }
    ) { innerPadding ->
        MainScreenContent(
            paddingValues = innerPadding,
            isLoading = uiState.isLoading,
            status = uiState.currentJobStatus,
            message = uiState.message,
            errorText = uiState.errorText,
            resultFileUrl = uiState.resultFileUrl,
            onPickJpgToPdf = onPickJpgToPdf,
            onPickPdfToWord = onPickPdfToWord,
            onPickWordToPdf = onPickWordToPdf,
            onPickMergePdf = onPickMergePdf,
            onOpenResult = {
                uiState.resultFileUrl?.let(onOpenUrl)
            },
            onGoHistory = {
                // Şimdilik history ekranı sonraki adımda bağlanacak.
                // Bu aşamada route hazırlığı bırakıyoruz.
                navController.navigate("history")
            }
        )
    }
}

@Composable
private fun MainScreenContent(
    paddingValues: PaddingValues,
    isLoading: Boolean,
    status: String?,
    message: String?,
    errorText: String?,
    resultFileUrl: String?,
    onPickJpgToPdf: () -> Unit,
    onPickPdfToWord: () -> Unit,
    onPickWordToPdf: () -> Unit,
    onPickMergePdf: () -> Unit,
    onOpenResult: () -> Unit,
    onGoHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPickJpgToPdf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "JPG to PDF")
        }

        Button(
            onClick = onPickPdfToWord,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "PDF to Word")
        }

        Button(
            onClick = onPickWordToPdf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Word to PDF")
        }

        Button(
            onClick = onPickMergePdf,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "PDF Birleştir")
        }

        Button(
            onClick = onGoHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Geçmiş İşlemler")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Durum",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                if (isLoading || status == "waiting" || status == "processing") {
                    CircularProgressIndicator()
                }

                Text(
                    text = buildStatusText(
                        status = status,
                        message = message,
                        errorText = errorText,
                        resultFileUrl = resultFileUrl
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )

                if (!resultFileUrl.isNullOrBlank()) {
                    Button(
                        onClick = onOpenResult,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Sonucu Aç")
                    }
                }
            }
        }
    }
}

private fun buildStatusText(
    status: String?,
    message: String?,
    errorText: String?,
    resultFileUrl: String?
): String {
    return when {
        !errorText.isNullOrBlank() -> {
            "Hata: $errorText"
        }

        !resultFileUrl.isNullOrBlank() && status == "done" -> {
            "İşlem tamamlandı. Sonuç hazır."
        }

        status == "waiting" -> {
            "İş sıraya alındı, worker bekleniyor..."
        }

        status == "processing" -> {
            "Dönüştürme işlemi devam ediyor..."
        }

        !message.isNullOrBlank() -> {
            message
        }

        else -> {
            "Hazır"
        }
    }
}