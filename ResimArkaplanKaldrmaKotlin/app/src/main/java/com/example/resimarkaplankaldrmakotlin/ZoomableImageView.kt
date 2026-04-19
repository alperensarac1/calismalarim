package com.example.resimarkaplankaldrmakotlin

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Çizilecek bitmap
    private var bitmap: Bitmap? = null

    // Bitmap'i ekranda nasıl göstereceğimizi tutan matrix
    private val drawMatrix = Matrix()

    // Matrix değerlerini okumak için yardımcı dizi
    private val matrixValues = FloatArray(9)

    // Ölçekleme ve gesture algılayıcılar
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    // Son dokunma noktası
    private val lastTouch = PointF()

    // Kullanıcı sürüklüyor mu?
    private var isDragging = false

    // Uzun basma ile büyüteç aktif mi?
    private var isMagnifierVisible = false

    // Kullanıcı şu anda uzun basma modunda mı?
    // Bu bilgi tek tıklama ile uzun basmayı ayırmak için önemli
    private var isLongPressMode = false

    // Büyütecin takip edeceği ekran koordinatı
    private var magnifierTouchX = 0f
    private var magnifierTouchY = 0f

    // Aktiviteye dönecek tek dokunma callback'i
    private var onImageTapListener: ((bitmapX: Int, bitmapY: Int) -> Unit)? = null

    // Minimum ve maksimum zoom
    private var minScale = 1f
    private var maxScale = 5f

    // -------- BÜYÜTEÇ AYARLARI --------

    // Büyüteç yarıçapı
    private val magnifierRadius = 170f

    // Büyüteç içeriği kaç kat büyüsün
    private val magnifierZoom = 2.5f

    // Büyütecin kenar boşluğu
    private val magnifierMargin = 28f

    // Büyüteç kenarlık boyası
    private val magnifierBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    // Büyüteç dış gölge / ikinci halka etkisi için
    private val magnifierOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(120, 0, 0, 0)
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }

    // Büyüteç içindeki hedef artısı
    private val magnifierCrossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    // İstersen büyüteç arka planı için hafif bir boya
    private val magnifierBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    fun setBitmap(newBitmap: Bitmap?) {
        val isFirstBitmap = bitmap == null
        bitmap = newBitmap

        if (isFirstBitmap) {
            resetZoom()
        } else {
            invalidate()
        }
    }

    fun getBitmap(): Bitmap? = bitmap

    fun setOnImageTapListener(listener: (bitmapX: Int, bitmapY: Int) -> Unit) {
        onImageTapListener = listener
    }

    /**
     * Görseli yeniden ekrana sığdırır ve ortalar.
     */
    fun resetZoom() {
        val bmp = bitmap ?: return
        if (width == 0 || height == 0) return

        drawMatrix.reset()

        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()
        val bmpWidth = bmp.width.toFloat()
        val bmpHeight = bmp.height.toFloat()

        // Görselin tamamı ekrana sığsın diye başlangıç ölçeği
        val scale = min(viewWidth / bmpWidth, viewHeight / bmpHeight)

        minScale = scale

        val dx = (viewWidth - bmpWidth * scale) / 2f
        val dy = (viewHeight - bmpHeight * scale) / 2f

        drawMatrix.postScale(scale, scale)
        drawMatrix.postTranslate(dx, dy)

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (bitmap != null) {
            resetZoom()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val bmp = bitmap ?: return

        // Önce resmi çiz
        canvas.drawBitmap(bmp, drawMatrix, null)

        // Sonra gerekiyorsa büyüteci çiz
        if (isMagnifierVisible) {
            drawMagnifier(canvas)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Önce detector'lara gönder
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouch.set(event.x, event.y)
                isDragging = false
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                // Çoklu dokunma varsa büyüteci kapatmak daha güvenli
                hideMagnifier()
            }

            MotionEvent.ACTION_MOVE -> {
                // Büyüteç açıksa parmak hareket ettikçe içerik güncellensin
                if (isMagnifierVisible) {
                    magnifierTouchX = event.x
                    magnifierTouchY = event.y
                    invalidate()
                }

                // İki parmak zoom yapılmıyorsa tek parmak sürükleme çalışsın
                if (!scaleDetector.isInProgress && event.pointerCount == 1 && !isLongPressMode) {
                    val dx = event.x - lastTouch.x
                    val dy = event.y - lastTouch.y

                    if (abs(dx) > 2f || abs(dy) > 2f) {
                        isDragging = true

                        drawMatrix.postTranslate(dx, dy)
                        fixTranslation()
                        invalidate()

                        lastTouch.set(event.x, event.y)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                // Parmağı kaldırınca büyüteç kapansın
                hideMagnifier()

                // Yeni dokunuş döngüsü için modları temizle
                isDragging = false
                isLongPressMode = false
            }

            MotionEvent.ACTION_CANCEL -> {
                hideMagnifier()
                isDragging = false
                isLongPressMode = false
            }
        }

        return true
    }

    /**
     * Ekrandaki dokunma noktasını bitmap koordinatına çevirir.
     */
    fun mapTouchToBitmap(touchX: Float, touchY: Float): Pair<Int, Int>? {
        val bmp = bitmap ?: return null

        val inverse = Matrix()
        if (!drawMatrix.invert(inverse)) return null

        val pts = floatArrayOf(touchX, touchY)
        inverse.mapPoints(pts)

        val bx = pts[0].toInt()
        val by = pts[1].toInt()

        if (bx !in 0 until bmp.width || by !in 0 until bmp.height) {
            return null
        }

        return Pair(bx, by)
    }

    /**
     * Görsel ekrandan tamamen kaçmasın diye çeviri sınırlarını düzeltir.
     */
    private fun fixTranslation() {
        val bmp = bitmap ?: return

        drawMatrix.getValues(matrixValues)

        val currentScaleX = matrixValues[Matrix.MSCALE_X]
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        val contentWidth = bmp.width * currentScaleX
        val contentHeight = bmp.height * currentScaleX

        var fixedX = transX
        var fixedY = transY

        // Yatay sınır
        if (contentWidth <= width) {
            fixedX = (width - contentWidth) / 2f
        } else {
            val minX = width - contentWidth
            val maxX = 0f
            fixedX = fixedX.coerceIn(minX, maxX)
        }

        // Dikey sınır
        if (contentHeight <= height) {
            fixedY = (height - contentHeight) / 2f
        } else {
            val minY = height - contentHeight
            val maxY = 0f
            fixedY = fixedY.coerceIn(minY, maxY)
        }

        matrixValues[Matrix.MTRANS_X] = fixedX
        matrixValues[Matrix.MTRANS_Y] = fixedY
        drawMatrix.setValues(matrixValues)
    }

    /**
     * Büyüteç görünümünü kapatır.
     */
    private fun hideMagnifier() {
        if (isMagnifierVisible) {
            isMagnifierVisible = false
            invalidate()
        }
    }

    /**
     * Uzun basılan noktaya göre büyüteci çizer.
     * Büyüteç ekranın sağ üst köşesinde sabit durur.
     */
    private fun drawMagnifier(canvas: Canvas) {
        val bmp = bitmap ?: return

        // Parmak altında hangi bitmap pikseli var?
        val mapped = mapTouchToBitmap(magnifierTouchX, magnifierTouchY) ?: return
        val bitmapX = mapped.first.toFloat()
        val bitmapY = mapped.second.toFloat()

        // Büyüteci sabit olarak sağ üstte göstereceğiz
        val cx = width - magnifierRadius - magnifierMargin
        val cy = magnifierRadius + magnifierMargin

        // Dairesel alan
        val circlePath = Path().apply {
            addCircle(cx, cy, magnifierRadius, Path.Direction.CW)
        }

        // Büyüteç içine çizilecek bitmap dönüşümü
        // Mantık:
        // 1) Dokunulan bitmap noktası merkeze gelsin
        // 2) Sonra büyüt
        // 3) Sonra büyüteç merkezine taşı
        val magnifierMatrix = Matrix().apply {
            postTranslate(-bitmapX, -bitmapY)
            postScale(magnifierZoom, magnifierZoom)
            postTranslate(cx, cy)
        }

        // Çizim alanını kaydet
        val saveCount = canvas.save()

        // Önce arka plan dairesi
        canvas.drawCircle(cx, cy, magnifierRadius, magnifierBackgroundPaint)

        // Daire içine kırparak büyütülmüş bitmap'i çiz
        canvas.clipPath(circlePath)
        canvas.drawBitmap(bmp, magnifierMatrix, null)

        // Kırpmayı geri al
        canvas.restoreToCount(saveCount)

        // Dış halkalar
        canvas.drawCircle(cx, cy, magnifierRadius, magnifierOuterPaint)
        canvas.drawCircle(cx, cy, magnifierRadius, magnifierBorderPaint)

        // Ortadaki kırmızı hedef işareti
        val crossHalf = 18f
        canvas.drawLine(cx - crossHalf, cy, cx + crossHalf, cy, magnifierCrossPaint)
        canvas.drawLine(cx, cy - crossHalf, cx, cy + crossHalf, magnifierCrossPaint)

        // İstersen parmak altındaki noktayı ana resimde de ufak bir işaretle gösterebilirsin
        // Şimdilik sade bırakıyoruz.
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            // Zoom başlarken büyüteci kapat
            hideMagnifier()
            isLongPressMode = false
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val bmp = bitmap ?: return false

            drawMatrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]

            var targetScale = currentScale * detector.scaleFactor
            targetScale = targetScale.coerceIn(minScale, maxScale)

            val factor = targetScale / currentScale

            drawMatrix.postScale(
                factor,
                factor,
                detector.focusX,
                detector.focusY
            )

            fixTranslation()
            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            // Uzun basma moduna geç
            isLongPressMode = true
            isDragging = false

            magnifierTouchX = e.x
            magnifierTouchY = e.y
            isMagnifierVisible = true
            invalidate()
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Uzun basma veya sürükleme olduysa bunu normal tap saymayız
            if (isDragging || isLongPressMode || isMagnifierVisible) {
                return true
            }

            val mapped = mapTouchToBitmap(e.x, e.y)
            if (mapped != null) {
                onImageTapListener?.invoke(mapped.first, mapped.second)
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            hideMagnifier()
            isLongPressMode = false

            drawMatrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]

            val newScale = if (currentScale < (minScale * 2f)) {
                min(currentScale * 2f, maxScale)
            } else {
                minScale
            }

            val factor = newScale / currentScale
            drawMatrix.postScale(factor, factor, e.x, e.y)
            fixTranslation()
            invalidate()
            return true
        }
    }
}