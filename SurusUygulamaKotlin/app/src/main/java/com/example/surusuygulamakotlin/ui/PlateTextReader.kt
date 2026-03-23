package com.example.surusuygulamakotlin.ui

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class PlateTextReader {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun read(plateCrop: Bitmap, onResult: (List<String>) -> Unit) {
        val img = InputImage.fromBitmap(plateCrop, 0)
        recognizer.process(img)
            .addOnSuccessListener { res ->
                val out = mutableListOf<String>()
                val lines = res.textBlocks.flatMap { it.lines }.map { it.text.uppercase() }

                for (ln in lines) {
                    val norm = ln.replace(Regex("[^A-Z0-9]"), "")
                    // TR plaka kabaca: 34ABC123, 06AB1234 vs.
                    if (Regex("^[0-9]{2}[A-Z]{1,3}[0-9]{2,4}$").matches(norm)) {
                        out.add(norm)
                    }
                }

                Log.e("PLATE_TEXT", "OCR candidates=$out raw='${res.text}'")
                onResult(out)
            }
            .addOnFailureListener { e ->
                Log.e("PLATE_TEXT", "OCR error: ${e.message}")
                onResult(emptyList())
            }
    }
}
