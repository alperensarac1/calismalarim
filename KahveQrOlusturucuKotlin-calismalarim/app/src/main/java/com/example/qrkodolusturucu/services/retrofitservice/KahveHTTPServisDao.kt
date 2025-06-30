package com.example.qrkodolusturucu.services.retrofitservice

import android.util.Log
import com.example.qrkodolusturucu.services.Services
import com.example.qrkodolusturucu.model.CRUDCevap
import com.example.qrkodolusturucu.model.KodUretCevap
import com.example.qrkodolusturucu.model.UrunCevap
import com.example.qrkodolusturucu.model.UrunKategori
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "KahveHTTPServisDao"
class KahveHTTPServisDao:Services {
    var kdi = ApiUtils.getKahveHTTPServisDaoInterface()

    override suspend fun kullaniciEkle(kisiTel: String): CRUDCevap {
        return suspendCoroutine { continuation ->
            kdi.kullaniciEkle(kisiTel).enqueue(object : Callback<CRUDCevap> {
                override fun onResponse(call: Call<CRUDCevap>, response: Response<CRUDCevap>) {
                    if (response.isSuccessful && response.body() != null) {
                        continuation.resume(response.body()!!) // Başarılı ise cevabı döndürüyoruz
                    } else {
                        continuation.resume(CRUDCevap(0, "Yükleme Başarısız", 0, 0)) // Hata durumunda
                    }
                }

                override fun onFailure(call: Call<CRUDCevap>, t: Throwable) {
                    continuation.resume(CRUDCevap(0, "Yükleme Başarısız", 0, 0)) // API çağrısı başarısız
                }
            })
        }
    }


    override suspend fun tumKahveler(): UrunCevap {
        return try {
            kdi.tumKahveler()
        } catch (e: Exception) {
            UrunCevap(emptyList()) // Hata durumunda boş liste döndür
        }
    }
    override suspend fun kodUret(dogrulamaKodu: String, kisiTel: String): KodUretCevap {
        var kodUretCevap = KodUretCevap(0, "")

        try {
            val response = kdi.kodUret(dogrulamaKodu, kisiTel).await()

            // Eğer başarılı bir yanıt aldıysak, cevabı güncelliyoruz
            if (response != null) {
                println("Kod üretimi başarılı ${response.message}")
                kodUretCevap.success = response.success ?: 0
                kodUretCevap.message = response.message ?: "Bilinmeyen hata"
            } else {
                kodUretCevap.success = 0
                kodUretCevap.message = "Sunucu hatası: ${response}"
            }
        } catch (e: Exception) {
            // Eğer bir hata oluşursa, başarısız durumu belirtiyoruz.
            kodUretCevap.success = 0
            kodUretCevap.message = "Kod üretimi başarısız: ${e.message}"
        }

        return kodUretCevap
    }

    override suspend fun kahveWithKategoriId(urunKategori: UrunKategori): UrunCevap {
        return try {
            kdi.kahveWithKategoriId(urunKategori.kategoriKodu())
        } catch (e: Exception) {
            UrunCevap(emptyList()) // Hata durumunda boş liste döndür
        }
    }


    override suspend fun kodSil(dogrulamaKodu: String): KodUretCevap {
        var kodUretCevap = KodUretCevap(0,"")
        kdi.kodSil(dogrulamaKodu).enqueue(object : Callback<KodUretCevap>{
            override fun onResponse(call: Call<KodUretCevap>, response: Response<KodUretCevap>) {
                if (response != null){
                    kodUretCevap.success = response!!.body()!!.success
                    kodUretCevap.message = response!!.body()!!.message
                }
            }

            override fun onFailure(call: Call<KodUretCevap>, t: Throwable) {
                kodUretCevap.success = 0
                kodUretCevap.message = "Kod silme başarısız kahveservisdao 90"
            }

        })
        return kodUretCevap
    }

}