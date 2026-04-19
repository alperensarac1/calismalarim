package com.example.resimarkaplankaldirmajetpack


import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resimarkaplankaldirmajetpack.editor.EditorViewModel
import com.example.resimarkaplankaldirmajetpack.ui.ZoomableImageEditor

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.loadImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                val state by viewModel.uiState.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pickImageLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isProcessing
                            ) {
                                Text("Fotoğraf Seç")
                            }

                            Button(
                                onClick = { viewModel.undo() },
                                modifier = Modifier.weight(1f),
                                enabled = state.canUndo && !state.isProcessing
                            ) {
                                Text("Geri Al")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.reset() },
                                modifier = Modifier.weight(1f),
                                enabled = state.workingBitmap != null && !state.isProcessing
                            ) {
                                Text("Sıfırla")
                            }

                            Button(
                                onClick = { viewModel.saveImage() },
                                modifier = Modifier.weight(1f),
                                enabled = state.workingBitmap != null && !state.isProcessing
                            ) {
                                Text("Kaydet")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = state.infoText,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tolerans: ${state.tolerance.toInt()}",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Slider(
                            value = state.tolerance,
                            onValueChange = { viewModel.onToleranceChange(it) },
                            valueRange = 0f..255f,
                            enabled = state.workingBitmap != null
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ZoomableImageEditor(
                            bitmap = state.workingBitmap,
                            onImageTap = { x, y ->
                                viewModel.onImageTapped(x, y)
                            },
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}