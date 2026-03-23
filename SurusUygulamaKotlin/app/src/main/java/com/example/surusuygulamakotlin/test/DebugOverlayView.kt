package com.example.surusuygulamakotlin.test

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class DebugOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    private var imgW = 1
    private var imgH = 1
    private var rot = 0
    private var boxes: List<Pair<RectF, Float>> = emptyList()

    fun setDetections(
        imageW: Int,
        imageH: Int,
        rot: Int,
        boxes: List<Pair<RectF, Float>>
    ) {
        this.imgW = imageW.coerceAtLeast(1)
        this.imgH = imageH.coerceAtLeast(1)
        this.rot = rot
        this.boxes = boxes
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Basit mapping: Debug için yeterli. (rot'u istersen sonra düzeltiriz)
        val sx = width.toFloat() / imgW
        val sy = height.toFloat() / imgH

        for ((r, score) in boxes) {
            val left = r.left * sx
            val top = r.top * sy
            val right = r.right * sx
            val bottom = r.bottom * sy

            canvas.drawRect(left, top, right, bottom, paint)
        }
    }
}
