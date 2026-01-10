import 'package:flutter_adisyon_uygulama/service/response/kategori_sil_response.dart';
import 'package:flutter_adisyon_uygulama/service/response/urun_sil_response.dart';

import '../model/kategori.dart';
import '../model/masa.dart';
import '../model/masa_urun.dart';
import '../model/urun.dart';

abstract class Services {
  Future<void> urunEkleAdmin(String urunAd, double fiyat, int kategoriId, int adet, String base64);

  Future<dynamic> masaSil(int masaId);
  Future<dynamic> masaEkle();
  Future<List<Masa>> masalariGetir();
  Future<List<Urun>> urunleriGetir();
  Future<List<MasaUrun>> masaUrunleriniGetir(int masaId);

  Future<dynamic> urunEkleMasaya(int masaId, int urunId, int adet);
  Future<dynamic> masaOdemeYap(int masaId);
  Future<double> masaToplamFiyat(int masaId);

  Future<dynamic> urunCikar(int masaId, int urunId);
  Future<List<Kategori>> kategorileriGetir();

  Future<Masa> masaGetir(int masaId);
  Future<dynamic> masaBirlestir(int anaMasaId, int birlestirilecekMasaId);

  Future<dynamic> kategoriEkle(String ad);
  Future<KategoriSilResponse> kategoriSil(int id);

  Future<UrunSilResponse> urunSil(String urunAd);
}
