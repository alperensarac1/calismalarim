package com.example.dosyapaylasimkotlin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dosyapaylasimkotlin.model.LinkResponse
import com.example.dosyapaylasimkotlin.model.UploadResponse
import com.example.dosyapaylasimkotlin.service.RetrofitClient

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream

class MainActivity : AppCompatActivity() {

    private lateinit var tvSelectedFile: TextView
    private lateinit var btnPickFile: Button
    private lateinit var btnUpload: Button
    private lateinit var progressUpload: ProgressBar
    private lateinit var tvUploadResult: TextView
    private lateinit var btnCopyLink: Button
    private var lastDownloadUrl: String? = null

    private lateinit var etCode: EditText
    private lateinit var btnCheck: Button
    private lateinit var btnDownload: Button
    private lateinit var tvCodeResult: TextView

    private var pickedUri: Uri? = null
    private var pickedDisplayName: String? = null
    private var pickedSize: Long = -1

    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                pickedUri = uri
                queryMeta(uri)
                tvSelectedFile.text = "Seçili dosya: ${pickedDisplayName ?: uri.lastPathSegment}"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        btnPickFile = findViewById(R.id.btnPickFile)
        btnUpload = findViewById(R.id.btnUpload)
        progressUpload = findViewById(R.id.progressUpload)
        tvUploadResult = findViewById(R.id.tvUploadResult)

        etCode = findViewById(R.id.etCode)
        btnCheck = findViewById(R.id.btnCheck)
        btnDownload = findViewById(R.id.btnDownload)
        tvCodeResult = findViewById(R.id.tvCodeResult)

        btnPickFile.setOnClickListener { openPicker() }
        btnUpload.setOnClickListener { uploadPicked() }
        btnCheck.setOnClickListener { checkCode() }
        btnDownload.setOnClickListener { downloadByCode() }
        btnCopyLink = findViewById(R.id.btnCopyLink)
        btnCopyLink.setOnClickListener {
            val url = lastDownloadUrl
            if (!url.isNullOrEmpty()) {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("download link", url)
                cm.setPrimaryClip(clip)
                Toast.makeText(this, "Link panoya kopyalandı", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Henüz bir link yok", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun openPicker() {
        // SAF (Storage Access Framework)
        pickFileLauncher.launch(arrayOf("*/*"))
    }

    private fun queryMeta(uri: Uri) {
        contentResolver.query(uri, null, null, null, null)?.use { c ->
            val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
            if (c.moveToFirst()) {
                pickedDisplayName = if (nameIdx >= 0) c.getString(nameIdx) else null
                pickedSize = if (sizeIdx >= 0) c.getLong(sizeIdx) else -1
            }
        }
    }

    private fun uploadPicked() {
        val uri = pickedUri
        if (uri == null) {
            Toast.makeText(this, "Önce dosya seçin", Toast.LENGTH_SHORT).show()
            return
        }

        progressUpload.visibility = View.VISIBLE
        tvUploadResult.text = ""

        // İçeriği oku
        val bytes: ByteArray = contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
            ?: run {
                progressUpload.visibility = View.GONE
                Toast.makeText(this, "Dosya açılamadı", Toast.LENGTH_SHORT).show()
                return
            }

        val guessType = contentResolver.getType(uri) ?: "application/octet-stream"
        val reqBody = RequestBody.create(guessType.toMediaTypeOrNull(), bytes)
        val fileName = pickedDisplayName ?: "file"

        val part = MultipartBody.Part.createFormData("file", fileName, reqBody)

        RetrofitClient.api.uploadFile(part).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                progressUpload.visibility = View.GONE

                val body = response.body()
                if (response.isSuccessful && body != null) {
                    if (body.ok) {
                        lastDownloadUrl = body.download_url
                        btnCopyLink.visibility = View.VISIBLE
                        tvUploadResult.setTextColor(0xFF2E7D32.toInt())
                        tvUploadResult.text =
                            "Yüklendi! Kod: ${body.code}\nİndirme: ${body.download_url}\nBilgi: ${body.info_url}\nGeçerlilik: ${body.expires_at}"
                        // Kodu inputa yaz
                        etCode.setText(body.code ?: "")
                    } else {
                        tvUploadResult.setTextColor(0xFFB00020.toInt())
                        tvUploadResult.text = "Hata: ${body.error ?: "Bilinmeyen"}"
                        btnCopyLink.visibility = View.GONE
                        lastDownloadUrl = null

                    }
                } else {
                    tvUploadResult.setTextColor(0xFFB00020.toInt())
                    tvUploadResult.text = "Sunucu hatası: ${response.code()}"
                    btnCopyLink.visibility = View.GONE
                    lastDownloadUrl = null

                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                progressUpload.visibility = View.GONE
                tvUploadResult.setTextColor(0xFFB00020.toInt())
                tvUploadResult.text = "İstek hatası: ${t.message}"
            }
        })
    }

    private fun checkCode() {
        val code = etCode.text.toString().trim().uppercase()
        if (!code.matches(Regex("^[A-Z0-9]{6}$"))) {
            Toast.makeText(this, "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show()
            return
        }
        tvCodeResult.text = "Sorgulanıyor…"

        RetrofitClient.api.getLink(code).enqueue(object : Callback<LinkResponse> {
            override fun onResponse(call: Call<LinkResponse>, response: Response<LinkResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null && body.ok) {
                    if (body.expired == true) {
                        tvCodeResult.setTextColor(0xFFB06D00.toInt())
                        tvCodeResult.text = "Kod: ${body.code} — Süresi dolmuş veya pasif."
                    } else {
                        tvCodeResult.setTextColor(0xFF0D47A1.toInt())
                        tvCodeResult.text = "Kod: ${body.code}\nDosya: ${body.original_name}\nBoyut: ${body.size_bytes}\nSon Kullanım: ${body.expires_at}\nLink: ${body.download_url}"
                    }
                } else {
                    tvCodeResult.setTextColor(0xFFB00020.toInt())
                    val err = body?.error ?: "Sunucu hatası ${response.code()}"
                    tvCodeResult.text = "Hata: $err"
                }
            }

            override fun onFailure(call: Call<LinkResponse>, t: Throwable) {
                tvCodeResult.setTextColor(0xFFB00020.toInt())
                tvCodeResult.text = "İstek hatası: ${t.message}"
            }
        })
    }

    private fun downloadByCode() {
        val code = etCode.text.toString().trim().uppercase()
        if (!code.matches(Regex("^[A-Z0-9]{6}$"))) {
            Toast.makeText(this, "Kod 6 haneli olmalı", Toast.LENGTH_SHORT).show()
            return
        }
        val url = "https://alperensaracdeneme.com/api/download.php?code=$code"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "İndirme bağlantısı açılamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
