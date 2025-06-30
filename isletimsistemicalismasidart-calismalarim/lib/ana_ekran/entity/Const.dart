import 'package:shared_preferences/shared_preferences.dart';

import '../model/UygulamalarModel.dart';

class Const {
  static final List<UygulamalarModel> uygulamalarListesi = [];
  static final List<UygulamalarModel> uygulamaListesi2 = [];
  static final List<UygulamalarModel> copUygulamaListesi = [];

  static Future<void> uygulamalariYukle() async {
    uygulamaListesi2.clear();
    copUygulamaListesi.clear();

    for (var uygulama in uygulamalarListesi) {
      if (!uygulama.copKutusundaMi) {
        uygulamaListesi2.add(uygulama);
      } else {
        copUygulamaListesi.add(uygulama);
      }
    }
  }

  static Future<void> initialize() async {
    final SharedPreferences sp = await SharedPreferences.getInstance();

    var copKutusu = UygulamalarModel("Çöp Kutusu", "ic_delete", "cop_kutusu", false);
    var telefonRehberi = UygulamalarModel("Rehber", "ic_person", "rehber", sp.getBool("RehberCop") ?? false);
    var hesapMakinesi = UygulamalarModel("Hesap Makinesi", "ic_hesapmakinesi", "hesap_makinesi", sp.getBool("HesapMakinesiCop") ?? false);
    var mesajlasma = UygulamalarModel("Mesajlaşma", "ic_message", "mesajlasma", sp.getBool("MesajlaşmaCop") ?? false);
    var telefon = UygulamalarModel("Telefon", "ic_phone", "telefon", sp.getBool("TelefonCop") ?? false);
    var tarayici = UygulamalarModel("Tarayıcı", "ic_browser", "tarayici", sp.getBool("TarayıcıCop") ?? false);
    var alarm = UygulamalarModel("Alarm", "ic_alarm", "alarm", sp.getBool("AlarmCop") ?? false);
    var muzikCalar = UygulamalarModel("Müzik Çalar", "ic_music", "muzik_calar", sp.getBool("MüzikÇalarCop") ?? false);
    var kamera = UygulamalarModel("Kamera", "ic_camera", "kamera", sp.getBool("KameraCop") ?? false);
    var galeri = UygulamalarModel("Galeri", "ic_gallery", "galeri", sp.getBool("GaleriCop") ?? false);
    var takvim = UygulamalarModel("Takvim", "ic_calendar", "takvim", sp.getBool("TakvimCop") ?? false);
    var qrKodOkuyucu = UygulamalarModel("QR Okuyucu", "ic_qr", "qr_kod_okuyucu", sp.getBool("QROkuyucuCop") ?? false);
    var notDefteri = UygulamalarModel("Not Defteri", "ic_notes", "not_defteri", sp.getBool("NotDefteriCop") ?? false);

    uygulamalarListesi.addAll([
      copKutusu, telefonRehberi, notDefteri, hesapMakinesi, mesajlasma, telefon, alarm,
      kamera, tarayici, muzikCalar, galeri, takvim, qrKodOkuyucu
    ]);

    uygulamalariYukle();
  }

  static List<UygulamalarModel> getTumUygulamalarListesi() => uygulamalarListesi;
  static List<UygulamalarModel> getCopUygulamaListesi() => copUygulamaListesi;
  static List<UygulamalarModel> getUygulamalarListesi() => uygulamaListesi2;
}

