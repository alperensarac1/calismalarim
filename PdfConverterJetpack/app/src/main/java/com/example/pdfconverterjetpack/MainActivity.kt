package com.example.pdfconverterjetpack


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.pdfconverterjetpack.screen.MainScreen
import com.example.pdfconverterjetpack.util.FileUtils
import com.example.pdfconverterjetpack.viewmodel.MainViewModel
import java.io.File

class MainActivity : ComponentActivity() {

    private var pendingJobType by mutableStateOf<String?>(null)

    // Test için sabit user id
    private val userId = 1

    // ViewModel referansını launcher içinde kullanmak için tutacağız
    private var currentViewModel: MainViewModel? = null

    /**
     * Tek dosya seçici:
     * jpg_to_pdf, pdf_to_word, word_to_pdf
     */
    private val singleFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            val file: File? = FileUtils.copyUriToFile(this, uri)
            val jobType = pendingJobType

            if (file != null && !jobType.isNullOrBlank()) {
                currentViewModel?.createSingleFileJob(
                    userId = userId,
                    jobType = jobType,
                    file = file
                )
            }
        }

    /**
     * Çoklu dosya seçici:
     * pdf_merge
     */
    private val multiFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isEmpty()) return@registerForActivityResult

            val files = FileUtils.copyUrisToFiles(this, uris)

            if (files.isNotEmpty()) {
                currentViewModel?.createMultiFileJob(
                    userId = userId,
                    jobType = "pdf_merge",
                    files = files
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()

                // Launcher callback'lerinde kullanmak için saklıyoruz
                currentViewModel = viewModel

                MainScreen(
                    navController = navController,
                    viewModel = viewModel,
                    onPickJpgToPdf = {
                        pendingJobType = "jpg_to_pdf"
                        singleFilePicker.launch(arrayOf("image/*"))
                    },
                    onPickPdfToWord = {
                        pendingJobType = "pdf_to_word"
                        singleFilePicker.launch(arrayOf("application/pdf"))
                    },
                    onPickWordToPdf = {
                        pendingJobType = "word_to_pdf"
                        singleFilePicker.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                "application/msword",
                                "*/*"
                            )
                        )
                    },
                    onPickMergePdf = {
                        multiFilePicker.launch(arrayOf("application/pdf"))
                    },
                    onOpenUrl = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    }
                )
            }
        }
}