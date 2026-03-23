package com.example.surusuygulamakotlin.ml


import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class MlKitPlateRecognizer {

    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    /**
     * Bitmap -> plaka olabilecek stringleri döndürür
     */
    suspend fun recognize(bitmap: Bitmap): List<String> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        val found = mutableListOf<String>()

        for (block in result.textBlocks) {
            for (line in block.lines) {
                val raw = line.text.uppercase()
                val plate = normalizePlate(raw)
                if (isLikelyTurkishPlate(plate)) {
                    found.add(formatPlate(plate))
                }
            }
        }
        return found
    }

    private fun normalizePlate(text: String): String {
        return text.replace(Regex("[^A-Z0-9]"), "")
    }

    /**
     * TR plaka formatı için basit filtre:
     * 2 rakam + 1-3 harf + 2-4 rakam
     * Örn: 34ABC123, 06A45
     */
    private fun isLikelyTurkishPlate(t: String): Boolean {
        val regex = Regex("^[0-9]{2}[A-Z]{1,3}[0-9]{2,4}$")
        return regex.matches(t)
    }

    private fun formatPlate(t: String): String {
        // 34ABC123 -> 34 ABC 123
        val city = t.substring(0, 2)
        val rest = t.substring(2)

        val letters = rest.takeWhile { it.isLetter() }
        val numbers = rest.dropWhile { it.isLetter() }

        return "$city $letters $numbers"
    }
}
