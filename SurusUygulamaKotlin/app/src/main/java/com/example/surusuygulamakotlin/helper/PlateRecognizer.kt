package com.example.surusuygulamakotlin.helper

import android.graphics.Bitmap
import kotlin.random.Random

interface PlateRecognizer {
    fun recognize(bitmap: Bitmap): List<String>
}

/**
 * Test için: bazen plaka döndürür.
 * Pipeline ve upload akışını doğrulamak için.
 */
class FakePlateRecognizer : PlateRecognizer {
    private val samples = listOf("34 ABC 123", "06 XYZ 78", "35AA 999", "16 BBB 16")

    override fun recognize(bitmap: Bitmap): List<String> {
        // Her frame’de spam olmasın diye %20 ihtimalle döndür
        return if (Random.nextInt(100) < 20) listOf(samples.random()) else emptyList()
    }
}
