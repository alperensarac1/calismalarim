package com.example.surusuygulamakotlin.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PlateBox(val rect: RectF, val score: Float)

class PlateDetectorHelper(context: Context) {

    private val interpreter: Interpreter
    private val inputW: Int
    private val inputH: Int

    // Output indexleri (shape’e göre bulunacak)
    private var boxesIdx = -1     // [1, N, 4]
    private var scoresIdx = -1    // [1, N]
    private var classesIdx = -1   // [1, N]
    private var countIdx = -1     // [1] veya [1,1]

    private var maxN = 10

    init {
        val modelBytes = context.assets.open("models/detect.tflite").readBytes()
        val bb = ByteBuffer.allocateDirect(modelBytes.size).order(ByteOrder.nativeOrder())
        bb.put(modelBytes)
        bb.rewind()

        interpreter = Interpreter(bb)

        val inShape = interpreter.getInputTensor(0).shape() // [1,H,W,3]
        inputH = inShape[1]
        inputW = inShape[2]

        // Output tensorlarını logla + indeksleri belirle
        for (i in 0 until interpreter.outputTensorCount) {
            val t = interpreter.getOutputTensor(i)
            val shape = t.shape()
            Log.d("TFLITE_OUT", "out[$i] shape=${shape.contentToString()} type=${t.dataType()}")

            // boxes: [1, N, 4]
            if (shape.size == 3 && shape[0] == 1 && shape[2] == 4) {
                boxesIdx = i
                maxN = shape[1]
            }

            // [1, N] tipinde olanlar scores/classes olabilir
            if (shape.size == 2 && shape[0] == 1) {
                // şimdilik aday, aşağıda yerleştiriyoruz
            }

            // count: [1] veya [1,1]
            if ((shape.size == 1 && shape[0] == 1) || (shape.size == 2 && shape[0] == 1 && shape[1] == 1)) {
                countIdx = i
            }
        }

        // scores/classes indexlerini bul (boxes N ile eşleşen [1,N] tensorları)
        val candidates = mutableListOf<Int>()
        for (i in 0 until interpreter.outputTensorCount) {
            val shape = interpreter.getOutputTensor(i).shape()
            if (shape.size == 2 && shape[0] == 1 && shape[1] == maxN) {
                candidates.add(i)
            }
        }

        // Genelde iki tane olur: scores ve classes
        // Hangisi hangisi önemli değil, classes kullanmıyoruz; scores lazım.
        // Çoğu modelde scores float, classes float/int olabilir ama Interpreter hepsini float kopyalayabilir.
        if (candidates.isNotEmpty()) {
            scoresIdx = candidates[0]
            if (candidates.size > 1) classesIdx = candidates[1]
        }

        Log.d("TFLITE_MAP", "boxesIdx=$boxesIdx scoresIdx=$scoresIdx classesIdx=$classesIdx countIdx=$countIdx maxN=$maxN")

        if (boxesIdx == -1 || scoresIdx == -1) {
            throw IllegalStateException("Model output formatı tanınamadı. boxesIdx=$boxesIdx scoresIdx=$scoresIdx")
        }
    }

    fun detect(bitmap: Bitmap, scoreThreshold: Float = 0.35f): List<PlateBox> {
        val resized = Bitmap.createScaledBitmap(bitmap, inputW, inputH, true)
        val input = bitmapToFloatInput(resized)

        val boxes = Array(1) { Array(maxN) { FloatArray(4) } } // [1,N,4]
        val scores = Array(1) { FloatArray(maxN) }             // [1,N]
        val classes = Array(1) { FloatArray(maxN) }            // [1,N] (opsiyonel)
        val count1 = FloatArray(1)                              // [1]
        val count2 = Array(1) { FloatArray(1) }                 // [1,1]

        val outputs = HashMap<Int, Any>()

        outputs[boxesIdx] = boxes
        outputs[scoresIdx] = scores

        if (classesIdx != -1) outputs[classesIdx] = classes
        if (countIdx != -1) {
            val shape = interpreter.getOutputTensor(countIdx).shape()
            outputs[countIdx] = if (shape.size == 2) count2 else count1
        }

        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)

        // count varsa onu kullan, yoksa maxN
        val n = if (countIdx != -1) {
            val shape = interpreter.getOutputTensor(countIdx).shape()
            val c = if (shape.size == 2) count2[0][0] else count1[0]
            c.toInt().coerceIn(0, maxN)
        } else {
            maxN
        }

        val result = ArrayList<PlateBox>(n)

        for (i in 0 until n) {
            val s = scores[0][i]
            if (s < scoreThreshold) continue

            val yMin = boxes[0][i][0]
            val xMin = boxes[0][i][1]
            val yMax = boxes[0][i][2]
            val xMax = boxes[0][i][3]

            val left = (xMin * bitmap.width).coerceIn(0f, bitmap.width.toFloat())
            val top = (yMin * bitmap.height).coerceIn(0f, bitmap.height.toFloat())
            val right = (xMax * bitmap.width).coerceIn(left + 1f, bitmap.width.toFloat())
            val bottom = (yMax * bitmap.height).coerceIn(top + 1f, bitmap.height.toFloat())

            result.add(PlateBox(RectF(left, top, right, bottom), s))
        }

        return result
    }

    private fun bitmapToFloatInput(bmp: Bitmap): ByteBuffer {
        val input = ByteBuffer.allocateDirect(1 * inputW * inputH * 3 * 4)
            .order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputW * inputH)
        bmp.getPixels(pixels, 0, inputW, 0, 0, inputW, inputH)

        for (px in pixels) {
            val r = ((px shr 16) and 0xFF) / 255f
            val g = ((px shr 8) and 0xFF) / 255f
            val b = (px and 0xFF) / 255f
            input.putFloat(r); input.putFloat(g); input.putFloat(b)
        }
        input.rewind()
        return input
    }

    fun close() {
        interpreter.close()
    }
}
