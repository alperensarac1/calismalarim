package com.example.pdfconverterkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import android.net.Uri

import android.view.View
import android.webkit.URLUtil
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.pdfconverterkotlin.ui.MainUiState
import com.example.pdfconverterkotlin.ui.MainViewModel
import com.example.pdfconverterkotlin.ui.jobs.JobsActivity
import com.example.pdfconverterkotlin.util.FileUtils

import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnOpenResult: Button

    // Basit test için sabit userId kullandık.
    // Sonra giriş sistemi gelince gerçek user id bağlanır.
    private val userId = 1

    // Kullanıcının hangi işlem için dosya seçtiğini tutuyoruz.
    private var pendingJobType: String? = null

    // Sonuç linkini burada tutacağız.
    private var latestResultUrl: String? = null

    /**
     * Tek dosya seçici:
     * jpg_to_pdf, pdf_to_word, word_to_pdf gibi işlemler burada kullanılacak.
     */
    private val singleFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult

            // Uri yetkisini kalıcı tutmak bazı cihazlarda işine yarayabilir.
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val file: File? = FileUtils.copyUriToFile(this, uri)
            val jobType = pendingJobType

            if (file == null || jobType.isNullOrBlank()) {
                tvStatus.text = "Dosya hazırlanamadı."
                return@registerForActivityResult
            }

            viewModel.createSingleFileJob(
                userId = userId,
                jobType = jobType,
                file = file
            )
        }

    /**
     * Çoklu dosya seçici:
     * pdf_merge işlemi için kullanılacak.
     */
    private val multiFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
            if (uris.isEmpty()) return@registerForActivityResult

            val files = FileUtils.copyUrisToFiles(this, uris)

            if (files.isEmpty()) {
                tvStatus.text = "Dosyalar hazırlanamadı."
                return@registerForActivityResult
            }

            viewModel.createMultiFileJob(
                userId = userId,
                jobType = "pdf_merge",
                files = files
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initViewModel()
        initClickListeners()
        observeUiState()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        btnOpenResult = findViewById(R.id.btnOpenResult)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    private fun initClickListeners() {
        findViewById<Button>(R.id.btnSelectJpgToPdf).setOnClickListener {
            pendingJobType = "jpg_to_pdf"

            // Görsel seçimi
            singleFilePicker.launch(arrayOf("image/*"))
        }

        findViewById<Button>(R.id.btnSelectPdfToWord).setOnClickListener {
            pendingJobType = "pdf_to_word"

            // PDF seçimi
            singleFilePicker.launch(arrayOf("application/pdf"))
        }
        findViewById<Button>(R.id.btnGoToHistory).setOnClickListener {
            startActivity(Intent(this, JobsActivity::class.java))
        }
        findViewById<Button>(R.id.btnSelectWordToPdf).setOnClickListener {
            pendingJobType = "word_to_pdf"

            // docx mime type bazı cihazlarda farklı davranabilir.
            // Bu yüzden geniş tutup sonra uzantı kontrolü de yapılabilir.
            singleFilePicker.launch(
                arrayOf(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/msword",
                    "*/*"
                )
            )
        }

        findViewById<Button>(R.id.btnSelectMergePdf).setOnClickListener {
            multiFilePicker.launch(arrayOf("application/pdf"))
        }

        btnOpenResult.setOnClickListener {
            latestResultUrl?.let { url ->
                openUrl(url)
            }
        }
    }

    private fun observeUiState() {
        viewModel.uiState.observe(this) { state ->
            progressBar.visibility = if (state.isLoading || isProcessingState(state.currentJobStatus)) {
                View.VISIBLE
            } else {
                View.GONE
            }

            tvStatus.text = buildStatusText(state)

            latestResultUrl = state.resultFileUrl
            btnOpenResult.visibility =
                if (!latestResultUrl.isNullOrBlank() && URLUtil.isValidUrl(latestResultUrl)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    /**
     * waiting / processing gibi durumlarda hala işlem devam ediyor kabul ediyoruz.
     */
    private fun isProcessingState(status: String?): Boolean {
        return status == "waiting" || status == "processing"
    }

    /**
     * Ekranda kullanıcıya daha anlaşılır bir metin gösterelim.
     */
    private fun buildStatusText(state: MainUiState): String {
        return when {
            !state.errorText.isNullOrBlank() -> {
                "Hata: ${state.errorText}"
            }

            !state.resultFileUrl.isNullOrBlank() && state.currentJobStatus == "done" -> {
                "İşlem tamamlandı.\nSonuç hazır."
            }

            state.currentJobStatus == "waiting" -> {
                "İş sıraya alındı, worker bekleniyor..."
            }

            state.currentJobStatus == "processing" -> {
                "Dönüştürme işlemi devam ediyor..."
            }

            !state.message.isNullOrBlank() -> {
                state.message
            }

            else -> {
                "Hazır"
            }
        }
    }

    /**
     * Sonuç dosyasını tarayıcı veya uygun uygulama ile açar.
     */
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}