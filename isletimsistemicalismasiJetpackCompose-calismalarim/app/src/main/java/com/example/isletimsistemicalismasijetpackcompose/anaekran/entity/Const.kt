package com.example.isletimsistemicalismasijetpackcompose.anaekran.entity

import android.content.Context
import android.content.SharedPreferences
import com.example.isletimsistemicalismasijetpackcompose.anaekran.model.UygulamalarModel
import com.example.isletimsistemicalismasijetpackcompose.uygulamalar.UygulamaGecisi
import java.io.Serializable

class Const(context: Context): Serializable {

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
        val copKutusu = UygulamalarModel("Çöp Kutusu", "ic_delete", "toCopKutusu", false)
        val telefonRehberi = UygulamalarModel("Rehber", "ic_person",
            UygulamaGecisi.toRehber.name, sp.getBoolean("RehberCop", false))
        val hesapMakinesi = UygulamalarModel("Hesap Makinesi", "ic_hesapmakinesi", UygulamaGecisi.toHesapMakinesi.name, sp.getBoolean("HesapMakinesiCop", false))
        val mesajlasma = UygulamalarModel("Mesajlaşma", "ic_message", UygulamaGecisi.toMesajlasma.name,sp.getBoolean("MesajlaşmaCop", false))
        val telefon = UygulamalarModel("Telefon", "ic_phone", UygulamaGecisi.toTelefon.name, sp.getBoolean("TelefonCop", false))
        val tarayici = UygulamalarModel("Tarayıcı", "ic_browser", UygulamaGecisi.toTarayici.name, sp.getBoolean("TarayıcıCop", false))
        val alarm = UygulamalarModel("Alarm", "ic_alarm", UygulamaGecisi.toAlarm.name, sp.getBoolean("AlarmCop", false))
        val muzikCalar = UygulamalarModel("Müzik Çalar", "ic_music", UygulamaGecisi.toMuzikCalar.name, sp.getBoolean("MüzikÇalarCop", false))
        val kamera = UygulamalarModel("Kamera", "ic_camera",UygulamaGecisi.toKamera.name, sp.getBoolean("KameraCop", false))
        val galeri = UygulamalarModel("Galeri", "ic_gallery",UygulamaGecisi.toGaleri.name, sp.getBoolean("GaleriCop", false))
        val takvim = UygulamalarModel("Takvim", "ic_calendar", UygulamaGecisi.toTakvim.name, sp.getBoolean("TakvimCop", false))
        val qrKodOkuyucu = UygulamalarModel("QR Okuyucu", "ic_qr", UygulamaGecisi.toQRKodOkuyucu.name, sp.getBoolean("QROkuyucuCop", false))
        val notDefteri = UygulamalarModel("Not Defteri", "ic_notes", UygulamaGecisi.toNotDefteri.name, sp.getBoolean("NotDefteriCop", false))

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
