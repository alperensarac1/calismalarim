package com.example.adisyonuygulamakotlin.services.retrofit


import com.example.adisyonuygulamakotlin.model.Kategori
import com.example.adisyonuygulamakotlin.model.Masa
import com.example.adisyonuygulamakotlin.model.MasaUrun
import com.example.adisyonuygulamakotlin.model.Urun
import com.example.adisyonuygulamakotlin.services.response.KategoriSilResponse
import com.example.adisyonuygulamakotlin.services.response.UrunSilResponse
import com.example.qrkodolusturucu.services.Services
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field


class AdisyonServisDao: Services {

    private val servis = ApiUtils.getAdisyonServisDaoInterface()


    override suspend fun urunleriGetir(): List<Urun> = servis.getUrunler()
    override suspend fun masaSil(masaId: Int): ResponseBody {
        return servis.masaSil(masaId)
    }

    override suspend fun urunEkle(urunAd: String, fiyat: Float, kategoriId: Int, adet: Int, base64: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = servis.urunEkle(
                    urunAd, fiyat, kategoriId, adet, base64
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    override suspend fun masaBirlestir(anaMasaId: Int, birlestirilecekMasaId: Int): ResponseBody {
        return servis.masaBirlestir(anaMasaId, birlestirilecekMasaId)
    }
    override suspend fun masaEkle(): ResponseBody {
        return servis.masaEkle()
    }

    override suspend fun masalariGetir(): List<Masa> {
        return servis.getMasalar()
    }

    override suspend fun masaUrunleriniGetir(masaId: Int): List<MasaUrun> {
        return servis.getMasaUrunleri(masaId)
    }

    override suspend fun urunEkle(masaId: Int, urunId: Int, adet: Int): ResponseBody {
        return servis.urunEkle(masaId, urunId, adet)
    }

    override suspend fun masaOdemeYap(masaId: Int): ResponseBody {
        return servis.masaOde(masaId)
    }

    override suspend fun masaToplamFiyat(masaId: Int): Float {
        return servis.getToplamFiyat(masaId)["toplam_fiyat"] ?: 0f
    }
    override suspend fun urunCikar(masaId: Int, urunId: Int): ResponseBody {
        return servis.urunCikar(masaId, urunId)
    }

    override suspend fun kategorileriGetir(): ArrayList<Kategori> {
        return servis.getKategoriler()
    }
    override suspend fun masaGetir(masaId: Int): Masa {
        return servis.masaGetir(masaId)
    }
    override suspend fun kategoriEkle(ad: String): ResponseBody =
        servis.kategoriEkle(ad)
    override suspend fun kategoriSil(id: Int): KategoriSilResponse =
        servis.kategoriSil(id)
    override suspend fun urunSil(urunAd: String): UrunSilResponse =
        servis.urunSil(urunAd)
}
