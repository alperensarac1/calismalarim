package com.example.qrkodolusturucu.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrkodolusturucu.data.numara_kayit.NumaraKayitSp
import com.example.qrkodolusturucu.model.Urun
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucu.services.ServicesImpl
import com.example.qrkodolusturucu.usecases.DogrulamaKoduOlustur
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class QRKodOlusturVM @Inject constructor(
    private val kahveServisDao: Services,
    private val numaraKayitSp: NumaraKayitSp,
    private val dogrulamaKoduOlustur: DogrulamaKoduOlustur // Inject ile aldık
) : ViewModel() {

    private val _kalanSure = MutableLiveData<String>()
    val kalanSureLiveData: LiveData<String> get() = _kalanSure

    private val _qrKodLiveData = MutableLiveData<Bitmap>()
    val qrKodLiveData: LiveData<Bitmap> get() = _qrKodLiveData

    private val _dogrulamaKoduLiveData = MutableLiveData<String>()
    val dogrulamaKoduLiveData: LiveData<String> get() = _dogrulamaKoduLiveData

    private var countDownTimer: CountDownTimer? = null

    init {
        yeniKodUret()
    }

    fun yeniKodUret() {
        val yeniKod = dogrulamaKoduOlustur.randomIdOlustur()
        Log.d("QRKodOlusturVM", "Yeni doğrulama kodu üretildi: $yeniKod")
        _dogrulamaKoduLiveData.postValue(yeniKod)  // LiveData'yı güncelle
    }

    fun QRKodOlustur(dogrulamaKodu: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(dogrulamaKodu, BarcodeFormat.QR_CODE, 400, 400)
            _qrKodLiveData.postValue(bitmap)  // QR kodunu güncelle
            Log.d("QRKodOlusturVM", "QR kod başarıyla oluşturuldu.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("QRKodOlusturVM", "QR kod oluşturulurken hata oluştu: ${e.message}")
        }
    }

    fun koduOlustur(dogrulamaKodu: String) {
        viewModelScope.launch {
            try {
                val numara = numaraKayitSp.numaraGetir()

                if (numara.isNullOrEmpty()) {
                    Log.e("QRKodOlusturVM", "Telefon numarası boş olduğu için kod oluşturulmadı!")
                    return@launch
                }

                kahveServisDao.kodUret(dogrulamaKodu, numara)
                Log.d("QRKodOlusturVM", "Kod veritabanına eklendi: $dogrulamaKodu")

                countDownTimer?.cancel()
                countDownTimer = object : CountDownTimer(90_000, 1_000) {
                    override fun onTick(millisUntilFinished: Long) {
                        _kalanSure.postValue("${millisUntilFinished / 1000} saniye")
                    }

                    override fun onFinish() {
                        kodSil(dogrulamaKodu)
                        yeniKodUret() // Yeni kod oluştur
                    }
                }.start()

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("QRKodOlusturVM", "Kod oluşturulurken hata oluştu: ${e.message}")
            }
        }
    }

    private fun kodSil(dogrulamaKodu: String) {
        viewModelScope.launch {
            try {
                kahveServisDao.kodSil(dogrulamaKodu)
                Log.d("QRKodOlusturVM", "Kod silindi: $dogrulamaKodu")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("QRKodOlusturVM", "Kod silinirken hata oluştu: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}
