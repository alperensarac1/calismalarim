package com.example.surusuygulamakotlin.helper


import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type


class YuvToRgbConverter(context: Context) {
    private val rs: RenderScript = RenderScript.create(context)
    private val scriptYuvToRgb: ScriptIntrinsicYuvToRGB =
        ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

    private var yuvBuffer: ByteArray? = null
    private var inputAllocation: Allocation? = null
    private var outputAllocation: Allocation? = null

    fun yuvToRgb(image: Image, output: Bitmap) {
        if (image.format != ImageFormat.YUV_420_888) {
            throw IllegalArgumentException("Unsupported image format: ${image.format}")
        }

        val yuvBytes = imageToNv21(image)
        val inAlloc = inputAllocation ?: Allocation.createSized(rs, Element.U8(rs), yuvBytes.size).also {
            inputAllocation = it
        }
        val outAlloc = outputAllocation ?: Allocation.createFromBitmap(rs, output).also {
            outputAllocation = it
        }

        inAlloc.copyFrom(yuvBytes)
        scriptYuvToRgb.setInput(inAlloc)
        scriptYuvToRgb.forEach(outAlloc)
        outAlloc.copyTo(output)
    }

    private fun imageToNv21(image: Image): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = yuvBuffer?.takeIf { it.size == ySize + uSize + vSize }
            ?: ByteArray(ySize + uSize + vSize).also { yuvBuffer = it }

        yPlane.buffer.get(nv21, 0, ySize)

        // NV21: VU interleaved
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val rowStride = uPlane.rowStride
        val pixelStride = uPlane.pixelStride

        var offset = ySize
        val width = image.width
        val height = image.height
        val chromaHeight = height / 2
        val chromaWidth = width / 2

        val uRow = ByteArray(rowStride)
        val vRow = ByteArray(rowStride)

        for (row in 0 until chromaHeight) {
            uBuffer.position(row * rowStride)
            vBuffer.position(row * rowStride)

            uBuffer.get(uRow, 0, rowStride)
            vBuffer.get(vRow, 0, rowStride)

            var col = 0
            while (col < chromaWidth) {
                val uIndex = col * pixelStride
                val vIndex = col * pixelStride
                nv21[offset++] = vRow[vIndex]
                nv21[offset++] = uRow[uIndex]
                col++
            }
        }

        return nv21
    }

    fun release() {
        inputAllocation?.destroy()
        outputAllocation?.destroy()
        rs.destroy()
    }
}
