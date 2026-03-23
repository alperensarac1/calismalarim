package com.example.surusuygulamakotlin.ui

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MlKitOcr {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // ✅ await/suspend yok: callback
    fun readPlateTextAsync(plateCrop: Bitmap, onResult: (List<String>) -> Unit) {
        val img = InputImage.fromBitmap(plateCrop, 0)

        recognizer.process(img)
            .addOnSuccessListener { res ->
                val out = mutableListOf<String>()
                val lines = res.textBlocks.flatMap { it.lines }.map { it.text.uppercase() }

                for (ln in lines) {
                    val norm = ln.replace(Regex("[^A-Z0-9]"), "")
                    if (Regex("^[0-9]{2}[A-Z]{1,3}[0-9]{2,4}$").matches(norm)) {
                        out.add(norm)
                    }
                }
                onResult(out)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
