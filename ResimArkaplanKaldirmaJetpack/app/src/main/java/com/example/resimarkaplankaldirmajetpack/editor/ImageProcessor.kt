package com.example.resimarkaplankaldirmajetpack.editor

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt
import java.util.ArrayDeque

object ImageProcessor {

    fun removeConnectedRegionByColor(
        source: Bitmap,
        startX: Int,
        startY: Int,
        targetColor: Int,
        tolerance: Float
    ): Bitmap {
        val result = source.copy(Bitmap.Config.ARGB_8888, true)

        val width = result.width
        val height = result.height

        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)

        val visited = BooleanArray(width * height)
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(startX to startY)

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

            val distance = colorDistance(r, g, b, targetR, targetG, targetB)

            if (distance <= tolerance) {
                pixels[index] = Color.TRANSPARENT

                queue.add((x + 1) to y)
                queue.add((x - 1) to y)
                queue.add(x to (y + 1))
                queue.add(x to (y - 1))
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
}