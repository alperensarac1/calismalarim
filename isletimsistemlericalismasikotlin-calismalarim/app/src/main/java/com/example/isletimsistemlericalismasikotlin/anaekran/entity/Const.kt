package com.example.isletimsistemlericalismasikotlin.anaekran.entity
import android.content.Context
import android.content.SharedPreferences
import com.example.isletimsistemlericalismasikotlin.R
import com.example.isletimsistemlericalismasikotlin.anaekran.model.UygulamalarModel
import java.io.Serializable

class Const(context: Context):Serializable {

    private val sp: SharedPreferences = context.getSharedPreferences("copKutusu", Context.MODE_PRIVATE)

    companion object {
        val uygulamalarListesi = ArrayList<UygulamalarModel>()
        val uygulamaListesi2 = ArrayList<UygulamalarModel>()
        val copUygulamaListesi = ArrayList<UygulamalarModel>()

        fun uygulamalariYukle() {
            uygulamaListesi2.clear()
            copUygulamaListesi.clear()

            uygulamalarListesi.forEach { uygulamalarModel ->
                if (!uygulamalarModel.copKutusundaMi) {
                    uygulamaListesi2.add(uygulamalarModel)
                } else {
                    copUygulamaListesi.add(uygulamalarModel)
                }
            }
        }
    }

    init {
        val copKutusu = UygulamalarModel("Çöp Kutusu", "ic_delete", 1, false)
        val telefonRehberi = UygulamalarModel("Rehber", "ic_person", R.id.toRehberFragment, sp.getBoolean("RehberCop", false))
        val hesapMakinesi = UygulamalarModel("Hesap Makinesi", "ic_hesapmakinesi", R.id.toHesapMakinesiFragment, sp.getBoolean("HesapMakinesiCop", false))
        val mesajlasma = UygulamalarModel("Mesajlaşma", "ic_message", R.id.toMesajlasmaFragment, sp.getBoolean("MesajlaşmaCop", false))
        val telefon = UygulamalarModel("Telefon", "ic_phone", R.id.toTelefonFragment, sp.getBoolean("TelefonCop", false))
        val tarayici = UygulamalarModel("Tarayıcı", "ic_browser", R.id.toTarayiciFragment, sp.getBoolean("TarayıcıCop", false))
        val alarm = UygulamalarModel("Alarm", "ic_alarm", R.id.toAlarmFragment, sp.getBoolean("AlarmCop", false))
        val muzikCalar = UygulamalarModel("Müzik Çalar", "ic_music", R.id.toMuzikCalarFragment, sp.getBoolean("MüzikÇalarCop", false))
        val kamera = UygulamalarModel("Kamera", "ic_camera", R.id.toKameraFragment, sp.getBoolean("KameraCop", false))
        val galeri = UygulamalarModel("Galeri", "ic_gallery", R.id.toGaleriFragment, sp.getBoolean("GaleriCop", false))
        val takvim = UygulamalarModel("Takvim", "ic_calendar", R.id.toTakvimFragment, sp.getBoolean("TakvimCop", false))
        val qrKodOkuyucu = UygulamalarModel("QR Okuyucu", "ic_qr", R.id.toQrKodOkuyucuFragment, sp.getBoolean("QROkuyucuCop", false))
        val notDefteri = UygulamalarModel("Not Defteri", "ic_notes", R.id.toNotDefteriFragment, sp.getBoolean("NotDefteriCop", false))

        uygulamalarListesi.addAll(
            listOf(
                copKutusu, telefonRehberi, notDefteri, hesapMakinesi, mesajlasma, telefon, alarm,
                kamera, tarayici, muzikCalar, galeri, takvim, qrKodOkuyucu
            )
        )

        uygulamalarListesi.forEach { uygulamalarModel ->
            if (uygulamalarModel.copKutusundaMi) {
                copUygulamaListesi.add(uygulamalarModel)
            } else {
                uygulamaListesi2.add(uygulamalarModel)
            }
        }
    }

    fun getTumUygulamalarListesi(): ArrayList<UygulamalarModel> = uygulamalarListesi
    fun getCopUygulamaListesi(): ArrayList<UygulamalarModel> = copUygulamaListesi
    fun getUygulamalarListesi(): ArrayList<UygulamalarModel> = uygulamaListesi2
}
