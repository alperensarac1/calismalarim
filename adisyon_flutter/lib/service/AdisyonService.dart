// lib/service/AdisyonService.dart
import 'dart:convert';
import 'dart:io';

import 'package:http/http.dart' as http;
import '../model/Kategori.dart';
import '../model/Masa.dart';
import '../model/MasaUrun.dart';
import '../model/Urun.dart';

class AdisyonService {
  AdisyonService({this.baseUrl = 'https://alperensaracdeneme.com/adisyon/'});

  final String baseUrl;

  /* ─────────────────────── HTTP helpers ─────────────────────── */

  Future<dynamic> _get(String path, [Map<String, dynamic>? query]) async {
    final uri = Uri.parse(baseUrl + path).replace(queryParameters: query);
    print('▶️ GET $uri');

    final resp = await http.get(uri);

    if (resp.statusCode != 200) {
      throw HttpException('GET ${resp.statusCode}: ${resp.body}');
    }

    try {
      return resp.body.isEmpty ? null : jsonDecode(resp.body);
    } catch (e) {
      print('❌ JSON parse hatası: $e\n📦 Gelen body:\n${resp.body}');
      return null;
    }
  }

  Future<dynamic> _post(String path, Map<String, dynamic> body) async {
    final uri = Uri.parse(baseUrl + path);
    final resp = await http.post(
      uri,
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: body,
    );

    if (resp.statusCode != 200) {
      throw HttpException('POST ${resp.statusCode}: ${resp.body}');
    }

    try {
      return resp.body.isEmpty ? null : jsonDecode(resp.body);
    } catch (e) {
      print('❌ JSON parse hatası: $e\n📦 Gelen body:\n${resp.body}');
      return null;
    }
  }

  /* ─────────────────────── MASA ─────────────────────── */

  Future<List<Masa>> getMasalar() async {
    final data = await _get('masa_listesi.php');
    if (data is List) {
      return data.map((e) => Masa.fromJson(e)).toList();
    }
    print('⚠️ masa_listesi.php beklenmedik veri döndürdü: $data');
    return [];
  }

  Future<Masa> masaGetir(int id) async {
    final data = await _get('masa_getir.php', {'masa_id': id});
    if (data is Map<String, dynamic>) {
      return Masa.fromJson(data);
    }
    print('⚠️ masa_getir.php beklenmedik veri döndürdü: $data');
    throw StateError("Masa getirilemedi");
  }

  Future<void> masaEkle() => _post('masa_ekle.php', {});
  Future<void> masaSil(int id) => _post('masa_sil.php', {'masa_id': id});
  Future<void> masaOde(int id) => _post('masa_odeme.php', {'masa_id': id});

  Future<void> masaBirlestir(int ana, int diger) => _post(
    'masa_birlestir.php',
    {'ana_masa_id': ana, 'birlestirilecek_masa_id': diger},
  );

  Future<List<MasaUrun>> getMasaUrunleri(int id) async {
    final data = await _get('masa_urunleri.php', {'masa_id': id});
    if (data is List) {
      return data.map((e) => MasaUrun.fromJson(e)).toList();
    }
    print('⚠️ masa_urunleri.php beklenmedik veri döndürdü: $data');
    return [];
  }

  Future<void> masaUrunEkle(int masa, int urun, int adet) =>
      _get('masa_urun_ekle.php', {
        'masa_id': masa,
        'urun_id': urun,
        'adet': adet,
      });

  Future<void> urunCikar(int masa, int urun) =>
      _post('urun_cikar.php', {'masa_id': masa, 'urun_id': urun});

  Future<Map<String, double>> getToplamFiyat(int id) async {
    final data = await _get('masa_toplam_fiyat.php', {'masa_id': id});
    if (data is Map) {
      return data.map((k, v) => MapEntry(k.toString(), (v as num).toDouble()));
    }
    print('⚠️ masa_toplam_fiyat.php beklenmedik veri döndürdü: $data');
    return {};
  }

  /* ─────────────────────── URUN ─────────────────────── */

  Future<List<Urun>> getUrunler() async {
    final data = await _get('urunleri_getir.php');
    if (data is List) {
      return data.map((e) => Urun.fromJson(e)).toList();
    }
    print('⚠️ urunleri_getir.php beklenmedik veri döndürdü: $data');
    return [];
  }

  Future<void> urunEkle(
      String ad,
      double fiyat,
      int kategoriId,
      int adet,
      String resim64,
      ) =>
      _post('urun_ekle.php', {
        'urun_ad': ad,
        'urun_fiyat': fiyat,
        'urun_kategori': kategoriId,
        'urun_adet': adet,
        'urun_resim': resim64,
      });

  Future<Map<String, dynamic>> urunSil(String ad) async {
    final res = await _post('urun_sil.php', {'urun_ad': ad});
    if (res is Map<String, dynamic>) return res;
    throw StateError('urun_sil.php beklenmedik çıktı: $res');
  }

  /* ─────────────────────── KATEGORI ─────────────────────── */

  Future<List<Kategori>> getKategoriler() async {
    final data = await _get('kategorileri_getir.php');
    print("🔍 kategori response: $data");

    if (data is List) {
      return data.map((e) => Kategori.fromJson(e)).toList();
    } else {
      print('❌ kategori veri tipi: ${data.runtimeType}, içerik: $data');
      return [];
    }
  }

  Future<void> kategoriEkle(String ad) =>
      _post('kategori_ekle.php', {'kategori_ad': ad});

  Future<Map<String, dynamic>> kategoriSil(int id) async {
    final res = await _post('kategori_sil.php', {'kategori_id': id});
    if (res is Map<String, dynamic>) return res;
    throw StateError('kategori_sil.php beklenmedik çıktı: $res');
  }
}
