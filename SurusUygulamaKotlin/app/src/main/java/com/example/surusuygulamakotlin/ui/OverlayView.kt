package com.example.surusuygulamakotlin.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintBox = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.GREEN
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    private var imgW = 1
    private var imgH = 1
    private var vw = 1
    private var vh = 1

    @Volatile
    private var results: List<Pair<RectF, String?>> = emptyList()

    fun setResults(
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int,
        boxes: List<Pair<RectF, String?>>
    ) {
        imgW = imageWidth.coerceAtLeast(1)
        imgH = imageHeight.coerceAtLeast(1)
        vw = viewWidth.coerceAtLeast(1)
        vh = viewHeight.coerceAtLeast(1)
        results = boxes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val local = results
        if (local.isEmpty()) return

        val scaleX = vw.toFloat() / imgW
        val scaleY = vh.toFloat() / imgH
        val scale = maxOf(scaleX, scaleY)

        val dx = (vw - imgW * scale) / 2f
        val dy = (vh - imgH * scale) / 2f

        val sx = width.toFloat() / vw
        val sy = height.toFloat() / vh
        canvas.save()
        canvas.scale(sx, sy)

        for ((r, label) in local) {
            val mapped = RectF(
                r.left * scale + dx,
                r.top * scale + dy,
                r.right * scale + dx,
                r.bottom * scale + dy
            )

            canvas.drawRect(mapped, paintBox)

            label?.let {
                canvas.drawText(
                    it,
                    mapped.left.coerceAtLeast(0f),
                    (mapped.top - 10f).coerceAtLeast(40f),
                    paintText
                )
            }
        }

        canvas.restore()
    }
}
