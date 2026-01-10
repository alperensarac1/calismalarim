import 'package:dio/dio.dart';
import 'package:flutter_adisyon_uygulama/service/response/kategori_sil_response.dart';
import 'package:flutter_adisyon_uygulama/service/response/urun_sil_response.dart';

import '../model/kategori.dart';
import '../model/masa.dart';
import '../model/masa_urun.dart';
import '../model/urun.dart';

// Modellerin importları sende nasıl klasörlendiyse ona göre düzenle:
// import 'models/masa.dart';
// import 'models/urun.dart';
// import 'models/kategori.dart';
// import 'models/masa_urun.dart';
// import 'responses/kategori_sil_response.dart';
// import 'responses/urun_sil_response.dart';

class AdisyonApi {
  final Dio _dio;

  AdisyonApi(this._dio);

  // GET masa_listesi.php
  Future<List<Masa>> getMasalar() async {
    final res = await _dio.get('masa_listesi.php');
    final list = (res.data as List).cast<Map<String, dynamic>>();
    return list.map(Masa.fromJson).toList();
  }

  // GET masa_urunleri.php?masa_id=..
  Future<List<MasaUrun>> getMasaUrunleri(int masaId) async {
    final res = await _dio.get('masa_urunleri.php', queryParameters: {
      'masa_id': masaId,
    });
    final list = (res.data as List).cast<Map<String, dynamic>>();
    return list.map(MasaUrun.fromJson).toList();
  }

  // POST masa_sil.php (form)
  Future<dynamic> masaSil(int masaId) async {
    final res = await _dio.post(
      'masa_sil.php',
      data: {'masa_id': masaId},
    );
    return res.data;
  }

  // POST urun_ekle.php (form)
  Future<dynamic> urunEkleAdmin({
    required String urunAd,
    required double urunFiyat,
    required int urunKategori,
    required int urunAdet,
    required String urunResimBase64,
  }) async {
    final res = await _dio.post(
      'urun_ekle.php',
      data: {
        'urun_ad': urunAd,
        'urun_fiyat': urunFiyat,
        'urun_kategori': urunKategori,
        'urun_adet': urunAdet,
        'urun_resim': urunResimBase64,
      },
    );
    return res.data;
  }

  // POST urun_sil.php (form) => UrunSilResponse
  Future<UrunSilResponse> urunSil(String urunAd) async {
    final res = await _dio.post(
      'urun_sil.php',
      data: {'urun_ad': urunAd},
    );
    return UrunSilResponse.fromJson(
      (res.data as Map).cast<String, dynamic>(),
    );
  }

  // GET masa_urun_ekle.php?masa_id=..&urun_id=..&adet=..
  Future<dynamic> urunEkleMasaya({
    required int masaId,
    required int urunId,
    required int adet,
  }) async {
    final res = await _dio.get(
      'masa_urun_ekle.php',
      queryParameters: {
        'masa_id': masaId,
        'urun_id': urunId,
        'adet': adet,
      },
    );
    return res.data;
  }

  // POST masa_birlestir.php (form)
  Future<dynamic> masaBirlestir({
    required int anaMasaId,
    required int birlestirilecekMasaId,
  }) async {
    final res = await _dio.post(
      'masa_birlestir.php',
      data: {
        'ana_masa_id': anaMasaId,
        'birlestirilecek_masa_id': birlestirilecekMasaId,
      },
    );
    return res.data;
  }

  // POST masa_ekle.php (form) (senin Kotlin'de param yok ama FormUrlEncoded idi)
  Future<dynamic> masaEkle() async {
    final res = await _dio.post('masa_ekle.php', data: {});
    return res.data;
  }

  // GET urunleri_getir.php
  Future<List<Urun>> getUrunler() async {
    final res = await _dio.get('urunleri_getir.php');
    final list = (res.data as List).cast<Map<String, dynamic>>();
    return list.map(Urun.fromJson).toList();
  }

  // POST masa_odeme.php (form)
  Future<dynamic> masaOde(int masaId) async {
    final res = await _dio.post(
      'masa_odeme.php',
      data: {'masa_id': masaId},
    );
    return res.data;
  }

  // GET masa_toplam_fiyat.php?masa_id=.. => {"toplam_fiyat": 123.0}
  Future<double> getToplamFiyat(int masaId) async {
    final res = await _dio.get(
      'masa_toplam_fiyat.php',
      queryParameters: {'masa_id': masaId},
    );

    final map = (res.data as Map).cast<String, dynamic>();
    final v = map['toplam_fiyat'];
    if (v == null) return 0.0;
    return (v as num).toDouble();
  }

  // POST urun_cikar.php (form)
  Future<dynamic> urunCikar({
    required int masaId,
    required int urunId,
  }) async {
    final res = await _dio.post(
      'urun_cikar.php',
      data: {
        'masa_id': masaId,
        'urun_id': urunId,
      },
    );
    return res.data;
  }

  // GET kategorileri_getir.php => ArrayList<Kategori>
  Future<List<Kategori>> getKategoriler() async {
    final res = await _dio.get('kategorileri_getir.php');
    final list = (res.data as List).cast<Map<String, dynamic>>();
    return list.map(Kategori.fromJson).toList();
  }

  // GET masa_getir.php?masa_id=..
  Future<Masa> masaGetir(int masaId) async {
    final res = await _dio.get('masa_getir.php', queryParameters: {
      'masa_id': masaId,
    });
    return Masa.fromJson((res.data as Map).cast<String, dynamic>());
  }

  // POST kategori_ekle.php (form)
  Future<dynamic> kategoriEkle(String kategoriAd) async {
    final res = await _dio.post(
      'kategori_ekle.php',
      data: {'kategori_ad': kategoriAd},
    );
    return res.data;
  }

  // POST kategori_sil.php (form) => KategoriSilResponse
  Future<KategoriSilResponse> kategoriSil(int id) async {
    final res = await _dio.post(
      'kategori_sil.php',
      data: {'kategori_id': id},
    );
    return KategoriSilResponse.fromJson(
      (res.data as Map).cast<String, dynamic>(),
    );
  }
}
