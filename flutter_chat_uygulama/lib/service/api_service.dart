import 'package:dio/dio.dart';

import '../model/konusulan_kisi.dart';
import '../model/kullanici.dart';
import '../model/mesaj.dart';
import '../model/simple_response.dart';
import 'dio_client.dart';



class ApiService {
  final Dio _dio;

  ApiService({Dio? dio}) : _dio = dio ?? DioClient.dio;

  // POST: kullanici-kayit.php (FormUrlEncoded)
  Future<SimpleResponse> kullaniciKayit({
    required String ad,
    required String numara,
  }) async {
    final res = await _dio.post(
      'kullanici-kayit.php',
      data: {
        'ad': ad,
        'numara': numara,
      },
    );
    return SimpleResponse.fromJson(_asMap(res.data));
  }

  // POST: mesaj-gonder.php (FormUrlEncoded) + opsiyonel base64_img
  Future<SimpleResponse> mesajGonder({
    required int gonderenId,
    required int aliciId,
    required String mesajText,
    required int resimVar,
    String? base64Img,
  }) async {
    final data = <String, dynamic>{
      'gonderen_id': gonderenId,
      'alici_id': aliciId,
      'mesaj_text': mesajText,
      'resim_var': resimVar,
    };

    if (base64Img != null) {
      data['base64_img'] = base64Img;
    }

    final res = await _dio.post('mesaj-gonder.php', data: data);
    return SimpleResponse.fromJson(_asMap(res.data));
  }

  // GET: mesajlari-getir.php
  Future<MesajListResponse> mesajlariGetir({
    required int gonderenId,
    required int aliciId,
  }) async {
    final res = await _dio.get(
      'mesajlari-getir.php',
      queryParameters: {
        'gonderen_id': gonderenId,
        'alici_id': aliciId,
      },
    );
    return MesajListResponse.fromJson(_asMap(res.data));
  }

  Future<KonusulanKisiListResponse> konusulanKisiler({
    required int kullaniciId,
  }) async {
    final res = await _dio.get(
      'konusulan-kullanicilar.php',
      queryParameters: {'kullanici_id': kullaniciId},
    );
    return KonusulanKisiListResponse.fromJson(_asMap(res.data));
  }

  Future<KullaniciListResponse> kullanicilariGetir() async {
    final res = await _dio.get('kullanicilari-getir.php');
    return KullaniciListResponse.fromJson(_asMap(res.data));
  }

  Future<SimpleResponse> testConnection() async {
    final res = await _dio.get('test-connection.php');
    return SimpleResponse.fromJson(_asMap(res.data));
  }
  
  Map<String, dynamic> _asMap(dynamic data) {
    if (data is Map<String, dynamic>) return data;
    if (data is Map) return Map<String, dynamic>.from(data);
    throw Exception('Beklenmeyen response tipi: ${data.runtimeType}');
  }
}
