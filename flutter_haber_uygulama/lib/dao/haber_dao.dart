// deo/haber_dao.dart
import 'package:dio/dio.dart';

import '../model/haber_model.dart';
import '../model/haber_response.dart';
import '../model/haber_turu_model.dart';
import '../model/yorum_insert_request.dart';
import '../model/yorum_model.dart';
import '../service/api_client.dart';
import '../service/api_response.dart';
class HaberDao {
  final Dio _dio;

  HaberDao({Dio? dio}) : _dio = dio ?? ApiClient.dio;

  // --- helpers --------------------------------------------------------------

  List<Map<String, dynamic>>? _unwrapList(dynamic raw) {
    // API bazen direkt liste, bazen { data: [...] } döndürüyor olabilir.
    final list = raw is List
        ? raw
        : (raw is Map && raw['data'] is List ? raw['data'] as List : null);

    if (list == null) return null;

    return list
        .where((e) => e is Map)
        .map((e) => (e as Map).cast<String, dynamic>())
        .toList();
  }

  Map<String, dynamic>? _unwrapMap(dynamic raw) {
    if (raw is Map<String, dynamic>) return raw;
    if (raw is Map) return raw.cast<String, dynamic>();
    return null;
  }

  // --- endpoints ------------------------------------------------------------

  /// ✅ Tüm haberler
  Future<List<HaberModel>?> getHaberler() async {
    try {
      final res = await _dio.get('haber_haberler-get.php');
      final list = _unwrapList(res.data);
      if (list == null) return null;
      return list.map(HaberModel.fromJson).toList();
    } catch (_) {
      return null;
    }
  }

  /// ✅ Son dakika
  Future<List<HaberModel>?> getSonDakikaHaberler() async {
    try {
      final res = await _dio.get('haber_haberler-sondakika-get.php');
      final list = _unwrapList(res.data);
      if (list == null) return null;
      return list.map(HaberModel.fromJson).toList();
    } catch (_) {
      return null;
    }
  }

  /// ✅ Gündem
  Future<List<HaberModel>?> getGundemHaberler() async {
    try {
      final res = await _dio.get('haber_haberler-gundem-get.php');
      final list = _unwrapList(res.data);
      if (list == null) return null;
      return list.map(HaberModel.fromJson).toList();
    } catch (_) {
      return null;
    }
  }

  /// ✅ Son 3 haber
  Future<List<HaberModel>?> getSon3Haber() async {
    try {
      final res = await _dio.get('haber_haberler-son3-get.php');
      final list = _unwrapList(res.data);
      if (list == null) return null;
      return list.map(HaberModel.fromJson).toList();
    } catch (_) {
      return null;
    }
  }

  /// ✅ Kategoriler
  Future<List<HaberTuruModel>?> getKategoriler() async {
    try {
      final res = await _dio.get('haber_haberturleri-get.php');
      final list = _unwrapList(res.data);
      if (list == null) return null;
      return list.map(HaberTuruModel.fromJson).toList();
    } catch (_) {
      return null;
    }
  }

  /// ✅ Yorumlar (param: haber_id)
  Future<List<YorumModel>?> getYorumlar(int haberId) async {
    try {
      final res = await _dio.get(
        'haber_yorumlar-get.php',
        queryParameters: {'haber_id': haberId},
      );

      final list = _unwrapList(res.data);
      if (list == null) return null;
      return list.map(YorumModel.fromJson).toList();
    } catch (_) {
      return null;
    }
  }

  /// ✅ Tek haber (id ile)
  Future<HaberModel?> getHaberById(int haberId) async {
    try {
      // bazı backendlere göre param adı id/haber_id olabilir
      final res = await _dio.get(
        'haber_getir.php',
        queryParameters: {'id': haberId},
      );

      final map = _unwrapMap(res.data);
      if (map == null) return null;

      // 1) { data: {...} } dönerse
      if (map['data'] is Map) {
        return HaberModel.fromJson((map['data'] as Map).cast<String, dynamic>());
      }

      // 2) direkt haber objesi dönerse
      if (map.containsKey('id') && map.containsKey('baslik')) {
        return HaberModel.fromJson(map);
      }

      return null;
    } catch (_) {
      return null;
    }
  }

  /// ⚠️ Yorum ekleme endpoint’i sende hangi dosya?
  /// Listede yoktu. Eğer varsa adını buraya yaz.
  Future<ApiResponse?> insertYorum(YorumInsertRequest yorum) async {
    try {
      final res = await _dio.post(
        'haber_yorumlar-insert.php', // ✅ sende farklıysa değiştir
        data: yorum.toJson(),
      );

      final map = _unwrapMap(res.data);
      if (map == null) return null;
      return ApiResponse.fromJson(map);
    } catch (_) {
      return null;
    }
  }
}
