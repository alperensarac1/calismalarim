import 'package:flutter_adisyon_uygulama/service/response/kategori_sil_response.dart';
import 'package:flutter_adisyon_uygulama/service/response/urun_sil_response.dart';
import 'package:flutter_adisyon_uygulama/service/services.dart';

import '../model/kategori.dart';
import '../model/masa.dart';
import '../model/masa_urun.dart';
import '../model/urun.dart';
import 'adisyon_api.dart';

class AdisyonServisDao implements Services {
  final AdisyonApi _api;

  AdisyonServisDao(this._api);

  @override
  Future<List<Urun>> urunleriGetir() => _api.getUrunler();

  @override
  Future<dynamic> masaSil(int masaId) => _api.masaSil(masaId);

  // Kotlin’de burada CoroutineScope.launch ile “fire and forget” vardı.
  // Flutter’da genelde await edilir. Yine de istersen await etmeden çağırırsın.
  @override
  Future<void> urunEkleAdmin(String urunAd, double fiyat, int kategoriId, int adet, String base64) async {
    await _api.urunEkleAdmin(
      urunAd: urunAd,
      urunFiyat: fiyat,
      urunKategori: kategoriId,
      urunAdet: adet,
      urunResimBase64: base64,
    );
  }

  @override
  Future<dynamic> masaBirlestir(int anaMasaId, int birlestirilecekMasaId) =>
      _api.masaBirlestir(anaMasaId: anaMasaId, birlestirilecekMasaId: birlestirilecekMasaId);

  @override
  Future<dynamic> masaEkle() => _api.masaEkle();

  @override
  Future<List<Masa>> masalariGetir() => _api.getMasalar();

  @override
  Future<List<MasaUrun>> masaUrunleriniGetir(int masaId) => _api.getMasaUrunleri(masaId);

  @override
  Future<dynamic> urunEkleMasaya(int masaId, int urunId, int adet) =>
      _api.urunEkleMasaya(masaId: masaId, urunId: urunId, adet: adet);

  @override
  Future<dynamic> masaOdemeYap(int masaId) => _api.masaOde(masaId);

  @override
  Future<double> masaToplamFiyat(int masaId) => _api.getToplamFiyat(masaId);

  @override
  Future<dynamic> urunCikar(int masaId, int urunId) => _api.urunCikar(masaId: masaId, urunId: urunId);

  @override
  Future<List<Kategori>> kategorileriGetir() => _api.getKategoriler();

  @override
  Future<Masa> masaGetir(int masaId) => _api.masaGetir(masaId);

  @override
  Future<dynamic> kategoriEkle(String ad) => _api.kategoriEkle(ad);

  @override
  Future<KategoriSilResponse> kategoriSil(int id) => _api.kategoriSil(id);

  @override
  Future<UrunSilResponse> urunSil(String urunAd) => _api.urunSil(urunAd);
}
