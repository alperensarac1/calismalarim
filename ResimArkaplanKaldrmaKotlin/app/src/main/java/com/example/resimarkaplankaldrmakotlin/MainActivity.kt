package com.example.resimarkaplankaldrmakotlin

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.ArrayDeque
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ZoomableImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnUndo: Button
    private lateinit var btnReset: Button
    private lateinit var btnSave: Button
    private lateinit var tvInfo: TextView
    private lateinit var tvTolerance: TextView
    private lateinit var seekTolerance: SeekBar

    // İlk yüklenen görsel
    private var originalBitmap: Bitmap? = null

    // Kullanıcıya şu an gösterilen / aktif bitmap
    private var workingBitmap: Bitmap? = null

    // Tolerans artık gerçek bir UI state gibi davranacak
    private var tolerance: Int = 60

    // Aynı anda ağır işlem çakışmasın
    private var isProcessing = false

    // Son dokunmadan sonra canlı preview üretebilmek için gerekli state'ler
    private var hasActivePreview = false
    private var previewBaseBitmap: Bitmap? = null
    private var lastTappedX: Int = -1
    private var lastTappedY: Int = -1

    // Slider hızlı hareket ettirilirse eski işi iptal etmek için
    private var previewJob: Job? = null

    // Undo geçmişi
    private val undoStack = ArrayDeque<Bitmap>()
    private val maxUndoCount = 10

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                loadSelectedImage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnUndo = findViewById(R.id.btnUndo)
        btnReset = findViewById(R.id.btnReset)
        btnSave = findViewById(R.id.btnSave)
        tvInfo = findViewById(R.id.tvInfo)
        tvTolerance = findViewById(R.id.tvTolerance)
        seekTolerance = findViewById(R.id.seekTolerance)

        setupUI()
        updateUndoButtonState()
    }

    private fun setupUI() {
        btnSelectImage.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            pickImageLauncher.launch("image/*")
        }

        btnUndo.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            undoLastAction()
        }

        btnReset.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            resetImage()
        }

        btnSave.setOnClickListener {
            if (isProcessing) return@setOnClickListener
            commitActivePreviewIfNeeded()
            saveTransparentImage()
        }

        seekTolerance.progress = tolerance
        tvTolerance.text = "Tolerans: $tolerance"

        seekTolerance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tolerance = progress
                tvTolerance.text = "Tolerans: $tolerance"

                // Kullanıcının seçtiği son bölge varsa canlı olarak yeniden hesapla
                if (fromUser) {
                    updateLivePreviewForCurrentSelection()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // İstersen burada ayrıca "önizleme hazır" gibi metin gösterebiliriz
            }
        })

        imageView.setOnImageTapListener { bitmapX, bitmapY ->
            if (!isProcessing) {
                onBitmapTapped(bitmapX, bitmapY)
            }
        }
    }

    private fun loadSelectedImage(uri: Uri) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)?.use { input ->
                    val decoded = BitmapFactory.decodeStream(input)
                    decoded?.copy(Bitmap.Config.ARGB_8888, true)
                }
            }

            if (bitmap == null) {
                Toast.makeText(this@MainActivity, "Resim yüklenemedi", Toast.LENGTH_SHORT).show()
                return@launch
            }

            originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            workingBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

            clearUndoHistory()
            clearActivePreview()

            imageView.setBitmap(workingBitmap)
            tvInfo.text = "Fotoğraf yüklendi. Silmek istediğin bölgeye dokun. Tolerans değişince sonuç canlı güncellenecek."
            updateUndoButtonState()
        }
    }

    /**
     * Kullanıcı yeni bir noktaya dokunduğunda:
     * 1) varsa önceki aktif preview'ü kalıcı hale getir
     * 2) yeni işlem için base bitmap oluştur
     * 3) son dokunma state'ini kaydet
     * 4) canlı preview üret
     */
    private fun onBitmapTapped(bitmapX: Int, bitmapY: Int) {
        val currentBitmap = workingBitmap ?: return

        if (bitmapX !in 0 until currentBitmap.width || bitmapY !in 0 until currentBitmap.height) {
            Toast.makeText(this, "Geçerli bir noktaya dokunun", Toast.LENGTH_SHORT).show()
            return
        }

        // Önce varsa önceki preview'ü kalıcı kabul et
        commitActivePreviewIfNeeded()

        val base = workingBitmap ?: return

        // Undo için bu yeni işlemin başlangıç halini saklıyoruz
        saveStateForUndo(base)

        // Yeni aktif preview state'i
        previewBaseBitmap = base.copy(Bitmap.Config.ARGB_8888, true)
        lastTappedX = bitmapX
        lastTappedY = bitmapY
        hasActivePreview = true

        tvInfo.text = "Canlı önizleme hazırlanıyor..."

        renderPreviewFromActiveState()
    }

    /**
     * Tolerans değiştiğinde aynı seçim için canlı yeniden hesap yapar.
     */
    private fun updateLivePreviewForCurrentSelection() {
        if (!hasActivePreview) return
        renderPreviewFromActiveState()
    }

    /**
     * Aktif state'ten yeni preview bitmap üretir.
     * Eski işi iptal edip yenisini başlatıyoruz.
     */
    private fun renderPreviewFromActiveState() {
        val baseBitmap = previewBaseBitmap ?: return
        if (!hasActivePreview) return
        if (lastTappedX < 0 || lastTappedY < 0) return

        previewJob?.cancel()

        isProcessing = true
        updateUndoButtonState()

        val startX = lastTappedX
        val startY = lastTappedY
        val currentTolerance = tolerance

        previewJob = lifecycleScope.launch {
            val resultBitmap = withContext(Dispatchers.Default) {
                val targetColor = baseBitmap.getPixel(startX, startY)

                removeConnectedRegionByColor(
                    source = baseBitmap,
                    startX = startX,
                    startY = startY,
                    targetColor = targetColor,
                    tolerance = currentTolerance
                )
            }

            workingBitmap = resultBitmap
            imageView.setBitmap(workingBitmap)

            isProcessing = false
            updateUndoButtonState()
            tvInfo.text = "Canlı önizleme aktif. Tolerans: $currentTolerance"
        }
    }

    /**
     * Aktif preview artık kalıcı kabul edilir.
     * Burada ekstra bitmap üretmiyoruz.
     * Çünkü workingBitmap zaten preview sonucu olmuş durumda.
     */
    private fun commitActivePreviewIfNeeded() {
        if (!hasActivePreview) return

        previewBaseBitmap = null
        hasActivePreview = false
        lastTappedX = -1
        lastTappedY = -1
    }

    private fun clearActivePreview() {
        previewJob?.cancel()
        previewBaseBitmap = null
        hasActivePreview = false
        lastTappedX = -1
        lastTappedY = -1
    }

    private fun saveStateForUndo(bitmap: Bitmap) {
        val snapshot = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        if (undoStack.size >= maxUndoCount) {
            undoStack.removeFirst()
        }

        undoStack.addLast(snapshot)
        updateUndoButtonState()
    }

    private fun undoLastAction() {
        previewJob?.cancel()

        if (undoStack.isEmpty()) {
            Toast.makeText(this, "Geri alınacak işlem yok", Toast.LENGTH_SHORT).show()
            return
        }

        val previousBitmap = undoStack.removeLast()
        workingBitmap = previousBitmap
        imageView.setBitmap(workingBitmap)

        clearActivePreview()

        isProcessing = false
        tvInfo.text = "Son işlem geri alındı."
        updateUndoButtonState()
    }

    private fun clearUndoHistory() {
        undoStack.clear()
        updateUndoButtonState()
    }

    private fun updateUndoButtonState() {
        btnUndo.isEnabled = undoStack.isNotEmpty() && !isProcessing
    }

    /**
     * Bağlı bölge silme algoritması
     */
    private fun removeConnectedRegionByColor(
        source: Bitmap,
        startX: Int,
        startY: Int,
        targetColor: Int,
        tolerance: Int
    ): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)

        val width = result.width
        val height = result.height

        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)

        val visited = BooleanArray(width * height)
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(Pair(startX, startY))

        val targetR = Color.red(targetColor)
        val targetG = Color.green(targetColor)
        val targetB = Color.blue(targetColor)

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()

            if (x < 0 || x >= width || y < 0 || y >= height) continue

            val index = y * width + x
            if (visited[index]) continue
            visited[index] = true

            val pixel = pixels[index]
            if (Color.alpha(pixel) == 0) continue

            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            val distance = colorDistance(
                r1 = r, g1 = g, b1 = b,
                r2 = targetR, g2 = targetG, b2 = targetB
            )

            if (distance <= tolerance) {
                pixels[index] = Color.TRANSPARENT

                queue.add(Pair(x + 1, y))
                queue.add(Pair(x - 1, y))
                queue.add(Pair(x, y + 1))
                queue.add(Pair(x, y - 1))
            }
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun colorDistance(
        r1: Int, g1: Int, b1: Int,
        r2: Int, g2: Int, b2: Int
    ): Double {
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()
        return sqrt(dr * dr + dg * dg + db * db)
    }

    private fun resetImage() {
        previewJob?.cancel()

        val original = originalBitmap ?: return

        workingBitmap = original.copy(Bitmap.Config.ARGB_8888, true)
        imageView.setBitmap(workingBitmap)

        clearUndoHistory()
        clearActivePreview()

        isProcessing = false
        tvInfo.text = "Görsel sıfırlandı. Baştan işlem yapabilirsin."
        updateUndoButtonState()
    }

    private fun saveTransparentImage() {
        val bitmap = workingBitmap ?: run {
            Toast.makeText(this, "Kaydedilecek görsel yok", Toast.LENGTH_SHORT).show()
            return
        }

        isProcessing = true
        updateUndoButtonState()
        tvInfo.text = "Görsel kaydediliyor..."

        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                saveBitmapAsPng(bitmap)
            }

            isProcessing = false
            updateUndoButtonState()

            if (success) {
                tvInfo.text = "Arka plansız görsel PNG olarak kaydedildi."
                Toast.makeText(this@MainActivity, "Kaydetme başarılı", Toast.LENGTH_SHORT).show()
            } else {
                tvInfo.text = "Kaydetme başarısız oldu."
                Toast.makeText(this@MainActivity, "Kaydetme başarısız", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapAsPng(bitmap: Bitmap): Boolean {
        return try {
            val fileName = "bg_removed_${System.currentTimeMillis()}.png"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ColorRemover")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ) ?: return false

            var outputStream: OutputStream? = null

            try {
                outputStream = contentResolver.openOutputStream(uri) ?: return false
                val result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, values, null, null)
                }

                result
            } finally {
                outputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}