package com.example.qrkodolusturucu.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrkodolusturucu.data.numara_kayit.NumaraKayitSp
import com.example.qrkodolusturucu.model.CRUDCevap
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucu.services.ServicesImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MainVM"
@HiltViewModel
class MainVM @Inject constructor(
    private val kullaniciServisDao: Services,
    private val numaraKayitSp: NumaraKayitSp
) : ViewModel() {

    private val _hediyeKahveLiveData = MutableLiveData<Int>()
    val hediyeKahveLiveData: LiveData<Int> get() = _hediyeKahveLiveData

    private val _kahveSayisiLiveData = MutableLiveData<Int>()
    val kahveSayisiLiveData: LiveData<Int> get() = _kahveSayisiLiveData

    private val _telefonNumarasiLiveData = MutableLiveData<String?>()
    val telefonNumarasiLiveData: LiveData<String?> get() = _telefonNumarasiLiveData

    init {
        startPeriodicKullaniciEkle()
    }

    fun kullaniciEkle(numara: String) {
        if (numara.isEmpty()) {
            Log.e(TAG, "Telefon numarası boş! Kullanıcı eklenemez.")
            return
        }

        viewModelScope.launch {
            try {
                val response = kullaniciServisDao.kullaniciEkle(numara)

                Log.d(TAG, "API Yanıtı: ${response.message}")
                Log.d(TAG, "API Success: ${response.success}")

                    _hediyeKahveLiveData.postValue(response.hediyeKahve)
                    _kahveSayisiLiveData.postValue(response.kahveSayisi)


            } catch (e: Exception) {
                Log.e(TAG, "API isteği başarısız: ${e.localizedMessage}")
            }
        }

    }



    fun checkTelefonNumarasi():Boolean {
        val savedPhoneNumber = numaraKayitSp.numaraGetir()
        if (savedPhoneNumber.isNullOrEmpty()) {
            Log.e(TAG, "Telefon numarası kaydedilmemiş!")
            return false
        }
        // Numarayı aldıktan sonra API çağrısını yap
        _telefonNumarasiLiveData.postValue(savedPhoneNumber)
        kullaniciEkle(savedPhoneNumber)
        return true
    }


    fun numarayiKaydet(numara: String) {
        numaraKayitSp.numaraKaydet(numara)
        Log.e(TAG, "Numara kaydedildi: $numara")  // Burada numaranın doğru kaydedildiğini logluyoruz.
        _telefonNumarasiLiveData.postValue(numara)
    }


    private fun startPeriodicKullaniciEkle() {
        viewModelScope.launch {
            // Başlangıçta numarayı kontrol et
            var numara = numaraKayitSp.numaraGetir()

            // Numara yoksa sürekli kontrol et
            while (true) {
                if (!numara.isNullOrEmpty()) {
                    // Numara varsa, kullanıcıyı ekle
                    kullaniciEkle(numara)
                } else {
                    Log.e(TAG, "Numara kaydedilmemiş!")
                }

                // 15 saniye bekle ve tekrar kontrol et
                delay(15000) // 15 saniye bekle
                numara = numaraKayitSp.numaraGetir() // Yeni numarayı kontrol et
            }
        }
    }


}
