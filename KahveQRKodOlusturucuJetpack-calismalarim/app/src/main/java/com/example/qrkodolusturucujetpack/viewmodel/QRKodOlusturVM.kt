package com.example.qrkodolusturucujetpack.viewmodel

import android.graphics.Bitmap
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucujetpack.data.numara_kayit.NumaraKayitSp
import com.example.qrkodolusturucujetpack.usecases.DogrulamaKoduOlustur
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRKodOlusturVM @Inject constructor(
    private val kahveServisDao: Services,
    private val numaraKayitSp: NumaraKayitSp,
    private val dogrulamaKoduOlustur: DogrulamaKoduOlustur
) : ViewModel() {

    var kalanSure by mutableStateOf("90 saniye")
        private set

    var qrKodBitmap by mutableStateOf<Bitmap?>(null)
        private set

    var dogrulamaKodu by mutableStateOf("")
        private set

    private var countDownTimer: CountDownTimer? = null

    init {
        yeniKodUret()
    }

    fun yeniKodUret() {
        val yeniKod = dogrulamaKoduOlustur.randomIdOlustur()
        Log.d("QRKodOlusturVM", "Yeni doğrulama kodu üretildi: $yeniKod")
        dogrulamaKodu = yeniKod
    }

    fun QRKodOlustur(kod: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(kod, BarcodeFormat.QR_CODE, 400, 400)
            qrKodBitmap = bitmap
            Log.d("QRKodOlusturVM", "QR kod başarıyla oluşturuldu.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("QRKodOlusturVM", "QR kod oluşturulurken hata: ${e.message}")
        }
    }

    fun koduOlustur(kod: String) {
        viewModelScope.launch {
            try {
                val numara = numaraKayitSp.numaraGetir()
                if (numara.isNullOrEmpty()) {
                    Log.e("QRKodOlusturVM", "Telefon numarası boş olduğu için kod oluşturulmadı!")
                    return@launch
                }

                kahveServisDao.kodUret(kod, numara)
                Log.d("QRKodOlusturVM", "Kod veritabanına eklendi: $kod")

                countDownTimer?.cancel()
                countDownTimer = object : CountDownTimer(90_000, 1_000) {
                    override fun onTick(millisUntilFinished: Long) {
                        kalanSure = "${millisUntilFinished / 1000} saniye"
                    }

                    override fun onFinish() {
                        kodSil(kod)
                        yeniKodUret()
                    }
                }.start()

            } catch (e: Exception) {
                Log.e("QRKodOlusturVM", "Kod oluşturulurken hata: ${e.message}")
            }
        }
    }

    private fun kodSil(kod: String) {
        viewModelScope.launch {
            try {
                kahveServisDao.kodSil(kod)
                Log.d("QRKodOlusturVM", "Kod silindi: $kod")
            } catch (e: Exception) {
                Log.e("QRKodOlusturVM", "Kod silinirken hata: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
